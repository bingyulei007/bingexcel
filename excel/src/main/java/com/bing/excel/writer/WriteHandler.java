package com.bing.excel.writer;

import java.util.List;

import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListLine;

/**
 * 目前的三个实现不是线程安全的
 *
 * @author shizhongtao
 */
public interface WriteHandler {

    /**
     *
     */
    public abstract void writeLine(ListLine line);

    public abstract void writeHeader(List<CellKV<String>> listStr);

    void writeHeader(ListLine listLine);

    public abstract String createSheet(String name);
	/**
	 * 设置数据的有效性
	 */
    public abstract void setDataValidationList(short firstRow, short endRow, short firstCol, short endCol, String[] validationStr);

    /**
     * 将缓冲数据写出并关闭内部资源（Workbook、OutputStream）。
     * 正常路径下由调用方在写完所有数据后调用；写出过程中抛异常时，
     * 调用方应在 finally 中调用 {@link #close()} 释放资源。
     */
    public abstract void flush();

    /**
     * 只释放内部资源（Workbook、OutputStream 等），不执行写出。
     * 用于写出过程异常时的兜底清理，避免文件句柄/SXSSF 临时文件泄漏。
     * flush() 成功后再调 close() 为 no-op。
     */
    public abstract void close();

}
