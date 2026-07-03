package com.bing.excel.reader.hssf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.eventusermodel.MissingRecordAwareHSSFListener;
import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.bing.excel.reader.ExcelReadListener;
import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListRow;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author shizhongtao
 *
 * date 2016-2-17
 * Description:
 */
public abstract class HSSFListenerAbstract implements HSSFListener {
	private POIFSFileSystem fs;

	private int lastRowNumber;
	private int lastColumnNumber;

	/** Should we output the formula, or the value it has? 输出表达式还是输出值 */
	private boolean outputFormulaValues = true;

	/** For parsing Formulas */
	private SheetRecordCollectingListener workbookBuildingListener;
	private HSSFWorkbook stubWorkbook;

	// Records we pick up as we process
	private SSTRecord sstRecord;
	private ExcelFormatTrackingHSSFListener formatListener;

	/** So we known which sheet we're on */
	private int sheetIndex = -1;
	private BoundSheetRecord[] orderedBSRs;
	@SuppressWarnings("unchecked")
	private ArrayList boundSheetRecords = new ArrayList();

	// For handling formulas with string results
	private int nextRow;
	private int nextColumn;
	private boolean outputNextStringRecord;

	private int curRow;
	private ListRow rowlist;
	@SuppressWarnings("unused")
	private String sheetName;
	private ExcelReadListener excelReader;
	private int maxReadLine = Integer.MAX_VALUE;
	private boolean startRead = true;
	private boolean ignoreNumFormat = false;

	private String aimSheetName = null;
	Set<Integer> aimSheetIndex;
	/**
	 * 开始读取exclesheet标识
	 */
	private boolean startReadSheet=false;

	public HSSFListenerAbstract(POIFSFileSystem fs,
			ExcelReadListener excelReader) throws SQLException {
		this(fs,excelReader,false);
	}
	public HSSFListenerAbstract(POIFSFileSystem fs,
			ExcelReadListener excelReader,boolean ignoreNumFormat) throws SQLException {
		this.fs = fs;
		this.curRow = 0;
		this.rowlist = new ListRow();
		this.excelReader = excelReader;
		this.ignoreNumFormat=ignoreNumFormat;
	}

	public HSSFListenerAbstract(String filename, ExcelReadListener excelReader)
			throws IOException, FileNotFoundException, SQLException {
		this(filename, excelReader, false);
	}

	public HSSFListenerAbstract(String filename, ExcelReadListener excelReader,
			boolean ignoreNumFormat) throws IOException, FileNotFoundException, SQLException {
		FileInputStream fis = new FileInputStream(filename);
		try {
			this.fs = new POIFSFileSystem(fis);
		} catch (IOException e) {
			fis.close();
			throw e;
		}
		this.curRow = 0;
		this.rowlist = new ListRow();
		this.excelReader = excelReader;
		this.ignoreNumFormat = ignoreNumFormat;
	}

	// excel记录行操作方法，以sheet索引，行索引和行元素列表为参数，对sheet的一行元素进行操作，元素为String类型
	public abstract void optRows(int sheetIndex, int curRow,
			ListRow rowlist) ;

	/**
	 * 遍历 excel 文件
	 */
	protected void process() throws IOException {
		MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(
				this);
		formatListener = new ExcelFormatTrackingHSSFListener(listener);
		formatListener.ignoreNumFormat(ignoreNumFormat);
		HSSFEventFactory factory = new HSSFEventFactory();
		HSSFRequest request = new HSSFRequest();

		if (outputFormulaValues) {
			request.addListenerForAllRecords(formatListener);
		} else {
			workbookBuildingListener = new SheetRecordCollectingListener(
					formatListener);
			request.addListenerForAllRecords(workbookBuildingListener);
		}

		try {
			factory.processWorkbookEvents(request, fs);
		} finally {
			closeFs();
		}
	}

	/**
	 * HSSFListener 监听方法，处理 Record
	 */
	@SuppressWarnings("unchecked")
	public void processRecord(Record record) {

		int thisRow = -1;
		int thisColumn = -1;
		String thisStr = null;
		String value = null;
		short sid = record.getSid();
		if (BoundSheetRecord.sid == sid) {
			boundSheetRecords.add(record);
		} else if(EOFRecord.sid==sid){
			if (sheetName != null && sheetIndex != -1) {
				if (startReadSheet) {
					excelReader.endSheet(sheetIndex, sheetName);
					startReadSheet=false;
				}
			}
			if(boundSheetRecords.size()==(sheetIndex+1)){
				excelReader.endWorkBook();
				closeFs();
			}
		}else if (BOFRecord.sid == sid) {
			BOFRecord br = (BOFRecord) record;
			if (br.getType() == BOFRecord.TYPE_WORKSHEET) {
				// Create sub workbook if required
				if (workbookBuildingListener != null && stubWorkbook == null) {
					stubWorkbook = workbookBuildingListener
							.getStubHSSFWorkbook();
				}

				// Works by ordering the BSRs by the location of
				// their BOFRecords, and then knowing that we
				// process BOFRecords in byte offset order
				sheetIndex++;
				if (orderedBSRs == null) {
					orderedBSRs = BoundSheetRecord
							.orderByBofPosition(boundSheetRecords);
				}
				sheetName = orderedBSRs[sheetIndex].getSheetname();
				startRead = true;

				if (aimSheetName != null) {
					if (!aimSheetName.equals(sheetName)) {
						startRead = false;
					}
				}
				if (aimSheetIndex != null) {
					if (aimSheetIndex.contains(sheetIndex)) {
						startRead = true;
					}else{
						startRead = false;
					}
				}
				if (startRead) {
					this.startReadSheet=true;
					excelReader.startSheet(sheetIndex, sheetName);
				}
			}else if(br.getType() == BOFRecord.TYPE_WORKBOOK){

			}
		} else if (!startRead) {
			return;
		}

		else if (SSTRecord.sid == sid) {
			sstRecord = (SSTRecord) record;
		} else if (BlankRecord.sid == sid) {
			BlankRecord brec = (BlankRecord) record;

			thisRow = brec.getRow();
			thisColumn = brec.getColumn();
			rowlist.add(new CellKV<String>(thisColumn, null));
		} else if (BoolErrRecord.sid == sid) {
			BoolErrRecord berec = (BoolErrRecord) record;

			curRow = thisRow = berec.getRow();
			thisColumn = berec.getColumn();
			String boolErrStr;
			if (berec.isBoolean()) {
				boolErrStr = String.valueOf(berec.getBooleanValue());
			} else if (berec.isError()) {
				switch (berec.getErrorValue()) {
					case 0x00: boolErrStr = "#NULL!"; break;
					case 0x07: boolErrStr = "#DIV/0!"; break;
					case 0x0F: boolErrStr = "#VALUE!"; break;
					case 0x17: boolErrStr = "#REF!"; break;
					case 0x1D: boolErrStr = "#NAME?"; break;
					case 0x24: boolErrStr = "#NUM!"; break;
					case 0x2A: boolErrStr = "#N/A"; break;
					default: boolErrStr = "#ERR(" + berec.getErrorValue() + ")"; break;
				}
			} else {
				boolErrStr = null;
			}
			if (boolErrStr != null) {
				rowlist.add(new CellKV<String>(thisColumn, boolErrStr));
			}

		} else if (FormulaRecord.sid == sid) {
			FormulaRecord frec = (FormulaRecord) record;

			thisRow = frec.getRow();
			thisColumn = frec.getColumn();

			if (outputFormulaValues) {
				if (Double.isNaN(frec.getValue())) {
					// Formula result is a string
					// This is stored in the next record
					outputNextStringRecord = true;
					nextRow = frec.getRow();
					nextColumn = frec.getColumn();
				} else {
					thisStr = formatListener.formatNumberDateCell(frec);
				}
			} else {
				thisStr = '"' + HSSFFormulaParser.toFormulaString(stubWorkbook,
						frec.getParsedExpression()) + '"';
			}
		} else if (StringRecord.sid == sid) {
			if (outputNextStringRecord) {
				// String for formula
				StringRecord srec = (StringRecord) record;
				thisStr = srec.getString();
				thisRow = nextRow;
				thisColumn = nextColumn;
				outputNextStringRecord = false;
			}

		} else if (LabelRecord.sid == sid) {
			LabelRecord lrec = (LabelRecord) record;

			curRow = thisRow = lrec.getRow();
			thisColumn = lrec.getColumn();
			value = lrec.getValue();
			if(!StringUtils.isEmpty(value)){
				rowlist.add(new CellKV<String>(thisColumn, value));
				}
		} else if (LabelSSTRecord.sid == sid) {
			LabelSSTRecord lsrec = (LabelSSTRecord) record;

			curRow = thisRow = lsrec.getRow();
			thisColumn = lsrec.getColumn();
			if (sstRecord == null) {
				//rowlist.add(new CellKV(thisColumn, ""));
			} else {
				value = sstRecord.getString(lsrec.getSSTIndex()).toString();
				if(!StringUtils.isEmpty(value)){
					rowlist.add(new CellKV<String>(thisColumn, value));
					}
			}
		} else if (NoteRecord.sid == sid) {
			NoteRecord nrec = (NoteRecord) record;

			thisRow = nrec.getRow();
			thisColumn = nrec.getColumn();
			thisStr = '"' + "(TODO)" + '"';
		} else if (NumberRecord.sid == sid) {
			NumberRecord numrec = (NumberRecord) record;

			curRow = thisRow = numrec.getRow();
			thisColumn = numrec.getColumn();
			value = formatListener.formatNumberDateCell(numrec);
			// Format
			if(!StringUtils.isEmpty(value)){
			rowlist.add(new CellKV<String>(thisColumn, value));
			}
		} else if (RKRecord.sid == sid) {
			RKRecord rkrec = (RKRecord) record;

			curRow = thisRow = rkrec.getRow();
			thisColumn = rkrec.getColumn();
			value = String.valueOf(rkrec.getRKNumber());
			if(!StringUtils.isEmpty(value)){
				rowlist.add(new CellKV<String>(thisColumn, value));
			}

		}

		// 遇到新行的操作
		if (thisRow != -1 && thisRow != lastRowNumber) {
			lastColumnNumber = -1;
		}

		// 空值的操作
		if (record instanceof MissingCellDummyRecord) {
			MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
			curRow = thisRow = mc.getRow();
			thisColumn = mc.getColumn();
			rowlist.add(new CellKV<String>(thisColumn, null));
		}
		// 如果str非空
			if(!StringUtils.isEmpty(thisStr)){
				rowlist.add(new CellKV<String>(thisColumn, thisStr));
				}

		// 更新行和列的值
		if (thisRow > -1)
			lastRowNumber = thisRow;
		if (thisColumn > -1)
			lastColumnNumber = thisColumn;

		// 行结束时的操作
		if (record instanceof LastCellOfRowDummyRecord) {

			// 行结束时， 调用 optRows() 方法
			lastColumnNumber = -1;


				optRows(sheetIndex, curRow, rowlist);

			rowlist = new ListRow();
			if (lastRowNumber >= maxReadLine) {
				startRead = false;
			}
		}
	}

	private void closeFs() {
		if (fs != null) {
			try {
				fs.close();
			} catch (IOException e) {
				// ignore
			}
			fs = null;
		}
	}

	public void setMaxReturnLine(int maxReadLine) {
		if (maxReadLine > 0) {
			this.maxReadLine = maxReadLine - 1;
		}
	}

	public boolean isOutputFormulaValues() {
		return outputFormulaValues;
	}

	public void setOutputFormulaValues(boolean outputFormulaValues) {
		this.outputFormulaValues = outputFormulaValues;
	}

	public void setAimSheetName(String aimSheetName) {
		this.aimSheetName = aimSheetName;
	}

	public void setAimSheetIndex(int[] aimSheetIndex) {

		Set<Integer> build = new HashSet<>();
		for (int i = 0; i < aimSheetIndex.length; i++) {
			if(aimSheetIndex[i]<0){
				throw new IllegalArgumentException("sheet 表的下标不能为负数");
			}else{
				build.add(aimSheetIndex[i]);
			}
		}
		Set<Integer> setSheets = Set.copyOf(build);
		this.aimSheetIndex = setSheets;
	}

}
