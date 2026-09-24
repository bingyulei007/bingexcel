package com.bing.excel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.bing.excel.annotation.CellConfig;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.excel.core.ReaderCondition;
import com.bing.excel.core.impl.BingExcelImpl.SheetVo;

/**
 * Regression: the HSSF EOF branch must not close the POIFSFileSystem while
 * {@code HSSFEventFactory.processWorkbookEvents} is still iterating records.
 *
 * <p>Fixture: a hand-built POIFS whose "Workbook" stream has an extra substream appended
 * after the last sheet's EOF. POI only keeps reading past a level-zero EOF when the next
 * sid is a BOF (2057) — that is exactly how multi-sheet iteration continues — so the extra
 * substream has to start with a BOF. It uses {@code TYPE_CHART} on purpose: a worksheet BOF
 * would bump {@code sheetIndex} past {@code orderedBSRs} and blow up with an unrelated
 * ArrayIndexOutOfBoundsException, and chart records never reach {@code optRow}.
 *
 * <p>The substream is deliberately larger than one POIFS block. Whether the post-close read
 * hits the channel depends on whether the bytes sit in the already-buffered block, so a
 * 20-byte tail is silently served from the buffer and would not reproduce.
 *
 * <p>Only the File entry point is affected: {@code new POIFSFileSystem(File)} is file-backed
 * so {@code close()} really closes the channel, while the InputStream flavour is in-memory
 * and {@code close()} is a no-op.
 */
public class HssfTrailingBofTest {

  private static final int ROWS = 50;

  /** 每个填充 record 8 字节，数量取到远超一个 POIFS block（512B）。 */
  private static final int FILLERS = 400;

  /** BOFRecord: sid=0x0809, size=16, vers=BIFF8, dt=TYPE_CHART(0x0020)。 */
  private static final byte[] BOF_CHART = {
      (byte) 0x09, (byte) 0x08, (byte) 0x10, (byte) 0x00,
      (byte) 0x00, (byte) 0x06, (byte) 0x20, (byte) 0x00,
      (byte) 0xBB, (byte) 0x0D, (byte) 0xCC, (byte) 0x07,
      (byte) 0xC1, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00};

  /** MMSRecord: sid=0x00C1, size=2。不在 HSSFListenerAbstract 的分发链里，不产生行。 */
  private static final byte[] FILLER = {
      (byte) 0xC1, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00};

  /** EOFRecord: sid=0x000A, size=0。 */
  private static final byte[] EOF = {(byte) 0x0A, (byte) 0x00, (byte) 0x00, (byte) 0x00};

  private static byte[] workbookStream(int fillers) throws Exception {
    HSSFWorkbook wb = new HSSFWorkbook();
    Sheet sheet = wb.createSheet("s0");
    sheet.createRow(0).createCell(0).setCellValue("Name");
    for (int r = 1; r <= ROWS; r++) {
      sheet.createRow(r).createCell(0).setCellValue("r" + r);
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    wb.write(baos);
    wb.close();

    byte[] wbBytes;
    try (POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(baos.toByteArray()));
        DocumentInputStream in = fs.getRoot().createDocumentInputStream("Workbook")) {
      wbBytes = IOUtils.toByteArray(in);
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(wbBytes);
    if (fillers >= 0) {
      out.write(BOF_CHART);
      for (int i = 0; i < fillers; i++) {
        out.write(FILLER);
      }
      out.write(EOF);
    }
    return out.toByteArray();
  }

  private static File xlsFile(int fillers, String tag) throws Exception {
    POIFSFileSystem out = new POIFSFileSystem();
    out.getRoot().createDocument("Workbook",
        new ByteArrayInputStream(workbookStream(fillers)));
    File f = File.createTempFile("trailing-bof-" + tag, ".xls");
    f.deleteOnExit();
    try (OutputStream os = new FileOutputStream(f)) {
      out.writeFilesystem(os);
    }
    out.close();
    return f;
  }

  @Test
  public void readFile_trailingSubstreamAfterLastEof_doesNotThrow() throws Exception {
    BingExcel bing = BingExcelBuilder.builderInstance();
    SheetVo<Item> vo = bing.readFile(xlsFile(FILLERS, "sub"),
        new ReaderCondition<Item>(0, 1, Item.class));
    Assert.assertEquals(ROWS, vo.getObjectList().size());
    Assert.assertEquals("r1", vo.getObjectList().get(0).getName());
  }

  /** 对照：同样的装配流程，去掉尾部子流，验证 fixture 本身合法。 */
  @Test
  public void readFile_cleanFile_readsAllRows() throws Exception {
    BingExcel bing = BingExcelBuilder.builderInstance();
    SheetVo<Item> vo = bing.readFile(xlsFile(-1, "clean"),
        new ReaderCondition<Item>(0, 1, Item.class));
    Assert.assertEquals(ROWS, vo.getObjectList().size());
    Assert.assertEquals("r1", vo.getObjectList().get(0).getName());
  }

  public static class Item {
    @CellConfig(index = 0)
    private String name;

    public String getName() {
      return name;
    }
  }
}
