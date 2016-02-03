package com.jt.ycl.oms.common.util.excel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystemException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdom.IllegalDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 操作excel的类，需要poi3.8的jar包<br>maven地址,目前仅支持03版本
 *<p> &ltdependency&gt<br>
 *  &nbsp;&ltgroupId&gtorg.apache.poi&lt/groupId&gt<br>
 *   &nbsp;&ltartifactId&gtpoi&lt/artifactId&gt<br>
 *   &nbsp; &ltversion&gt3.8&lt/version&gt<br>
 *&lt/dependency&gt
 *</p>
 * @author shizhongtao
 *
 * 2015 2015-4-24 下午5:49:55 
 *
 * @author shizhongtao
 * 
 *         2015 2015-4-27 下午4:08:15
 */
public class ExcelHanderUtil {
	
	public static ExcelFieldConvertor convertor;
	
	private static Logger logger=LoggerFactory.getLogger(ExcelHanderUtil.class);
    /**
     * 把excel写到指定文件中
     * 
     * @param wb
     * @param path
     * @throws IOException
     */
    public static void writeWb03toFile(HSSFWorkbook wb, File file) throws IOException {

        FileOutputStream fileOut = new FileOutputStream(file);
        writeWb03toStrem(wb, fileOut);// 创建时候需要调用
        if (fileOut!=null) {
			fileOut.close();
		}
    }

    /**
     * 把excel写到指定路径
     * 
     * @param wb
     * @param path
     * @throws IOException
     */
    public static void writeWb03toPath(HSSFWorkbook wb, String path) throws IOException {

        FileOutputStream fileOut = new FileOutputStream(path);
        writeWb03toStrem(wb, fileOut);
        if (fileOut!=null) {
			fileOut.close();
		}
    }

    /**
     * 把excel写到输出流里面，
     * 
     * @param wb
     * @param fileOut
     * @throws IOException
     */
    public static void writeWb03toStrem(HSSFWorkbook wb, OutputStream fileOut) throws IOException {

        wb.write(fileOut);// 把Workbook对象输出到文件workbook.xls中
        // fileOut.close();
    }

    /**
     * 创建Sheet工作薄
     * 
     * @param wb
     * @param name
     *            唯一指定的sheet名称
     * @return 返回创建的HSSFSheet, 如果名字是null或者已经被占用抛出异常
     * @throws IllegalArgumentException
     *             if the name is null or invalid or workbook already contains a
     *             sheet with this name
     */
    public static HSSFSheet createSheet(HSSFWorkbook wb, String name) {
        HSSFSheet sheet = wb.createSheet(name);
        return sheet;
    }

    /**
     * 创建Sheet工作薄
     * 
     * @param wb
     * @return 返回创建的HSSFSheet
     */
    public static HSSFSheet createSheet(HSSFWorkbook wb) {
        return wb.createSheet();
    }

    /**
     * 插入一个对象到表格
     * 
     * @param <T>
     *            对象类型
     * @param sheet
     * @param obj
     *            带插入对象
     * @param arr
     *            key值的数组，用来规定插入顺序
     * @param rowIndex
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    public static <T> void writeLineToSheet(HSSFSheet sheet, Class<T> clz, T obj, String[] arr, int rowIndex) throws NoSuchFieldException,
            SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        HSSFRow row = sheet.createRow(rowIndex);

        writeLineToRow(getValuesByOrder(clz, obj, arr), row);
    }

    private static <T> Object[] getValuesByOrder(Class<T> clz, T obj, String[] arr) throws NoSuchFieldException, SecurityException,
            NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object[] values = new Object[arr.length];
        for (int i = 0; i < arr.length; i++) {
            String getMethodName = "get" + toUpFirst(arr[i]);
            Method method = clz.getDeclaredMethod(getMethodName);
            Object value = method.invoke(obj);
            if(convertor!=null){
            	value=convertor.marshal(arr[i],value);
            }
            values[i] = value;
        }
        return values;

    }

    /**
     * 插入一个map对象到表格
     * 
     * @param sheet
     * @param map
     *            带插入对象
     * @param arr
     *            key值的数组，用来规定插入顺序
     * @param rowIndex
     */
    public static void writeLineToSheet(HSSFSheet sheet, Map<String, Object> map, String[] arr, int rowIndex) {
        HSSFRow row = sheet.createRow(rowIndex);
        Object[] values = new Object[arr.length];
        for (int i = 0; i < arr.length; i++) {
            values[i] = map.get(arr[i]);
        }
        writeLineToRow(values, row);
    }

    /**
     * 写入一行数据到sheet表
     * 
     * @param sheet
     * @param arr
     *            String类型的数组
     * @param rowIndex
     *            行下标
     */
    public static void writeLineToSheet(HSSFSheet sheet, Iterable<Object> arr, int rowIndex) {
        HSSFRow row = sheet.createRow(rowIndex);
        writeLineToRow(arr, row);
    }

    /**
     * 写入一行数据到sheet表
     * 
     * @param sheet
     * @param arr
     *            String类型的数组
     * @param rowIndex
     *            行下标
     */
    public static void writeLineToSheet(HSSFSheet sheet, String[] arr, int rowIndex) {
        writeLineToSheet(null, sheet, arr, rowIndex, false);
    }

    /**
     * 写入一行数据到sheet表
     * 
     * @param wb
     *            表格
     * @param sheet
     * @param arr
     *            String类型的数组
     * @param rowIndex
     *            行下标
     * @param isBOLD
     *            是否加粗
     */
    public static void writeLineToSheet(HSSFWorkbook wb, HSSFSheet sheet, String[] arr, int rowIndex, boolean isBOLD) {

        HSSFRow row = sheet.createRow(rowIndex);

        HSSFCellStyle style = null;
        if (isBOLD) {
            sheet.getRow(rowIndex).setHeight((short) (23 * 20));
            style = wb.createCellStyle();
            // 设置这些样式
            style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            style.setBorderRight(HSSFCellStyle.BORDER_THIN);
            style.setBorderTop(HSSFCellStyle.BORDER_THIN);
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            // 生成一个字体
            HSSFFont font = wb.createFont();
            font.setColor(HSSFColor.BLACK.index);
            font.setFontHeightInPoints((short) 14);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            // 把字体应用到当前的样式
            style.setFont(font);
        }

        writeLineToRow(arr, row, style);

    }

    /**
     * 读取多个数据到sheet
     * 
     * @param iterable
     * @param arr
     * @param statrtIndex
     *            开始行数的下标，0为第一行
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    public static <T> void writeLinesToSheet(HSSFSheet sheet, Class<T> clazz, Iterable<T> iterable, String[] arr, int statrtIndex)
            throws NoSuchFieldException, SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        for (T t : iterable) {
            writeLineToSheet(sheet, clazz, t, arr, statrtIndex);
            statrtIndex++;
        }
    }

    /**
     * 写一组数据到单元格
     * 
     * @param obj
     * @param row
     * @throws IllegalArgumentException
     *             if columnIndex < 0 or greater than 255, the maximum number of
     *             columns supported by the Excel binary format (.xls)
     */
    public static void writeLineToRow(Object[] obj, HSSFRow row) {
        for (int i = 0; i < obj.length; i++) {
            if (null == obj[i]) {
                row.createCell(i);
            } else if (Double.class.isInstance(obj[i])) {
                row.createCell(i).setCellValue((double) obj[i]);
            } else if (Integer.class.isInstance(obj[i])) {
                row.createCell(i).setCellValue((int) obj[i]);
            } else if (Boolean.class.isInstance(obj[i])) {
                row.createCell(i).setCellValue((boolean) obj[i]);
            } else if (Date.class.isInstance(obj[i])) {
                HSSFCell cell = row.createCell(i);
                HSSFSheet sheet = row.getSheet();

                HSSFWorkbook wb = sheet.getWorkbook();
                HSSFDataFormat format = wb.createDataFormat();
                HSSFCellStyle cellStyle = wb.createCellStyle();
                cellStyle.setDataFormat(format.getFormat("m月d日"));

                cell.setCellStyle(cellStyle);
                cell.setCellValue((Date) obj[i]);
            } else if (Calendar.class.isInstance(obj[i])) {
                row.createCell(i).setCellValue((Calendar) obj[i]);
            } else if (String.class.isInstance(obj[i])) {
                row.createCell(i).setCellValue((String) obj[i]);
            } 
            else {
                row.createCell(i).setCellValue(obj[i].toString());
            }
        }
    }

    /**
     * @param arr
     * @param row
     * @throws IllegalArgumentException
     *             if columnIndex < 0 or greater than 255, the maximum number of
     *             columns supported by the Excel binary format (.xls)
     */
    public static void writeLineToRow(String[] arr, HSSFRow row, HSSFCellStyle style) {
        for (int i = 0; i < arr.length; i++) {
            HSSFRichTextString richString = new HSSFRichTextString(arr[i]);
            HSSFCell cell = row.createCell(i);
            if (style != null) {
                cell.setCellStyle(style);
            }

            cell.setCellValue(richString);
        }
    }

    /**
     * 校验excel表头是不是和传入数组相同（顺序必须也一样）
     * 
     * @param arr
     * @return
     */
    public static boolean validOrder(String[] arr, HSSFSheet sheet) {
        return validOrder(arr, sheet, 0);
    }
    /**
     * 校验excel表头是不是和传入数组相同（顺序必须也一样）
     * 
     * @param arr
     * @return
     */
    public static boolean validOrder(String[] arr, XSSFSheet sheet) {
    	return validOrder(arr, sheet, 0);
    }
    /**
     * 校验excel表头是不是和传入数组相同（顺序必须也一样）
     * 
     * @param arr
     * @return
     * @throws IOException 
     */
    public static boolean validOrder(String[] arr, File file) throws IOException {
    	 if (!file.isFile()) {
             throw new IllegalArgumentException("文件不能读取");
         }
         FileInputStream is;
		try {
			is = new FileInputStream(file);
		} catch (Exception e) {
			  throw new IllegalArgumentException("文件不能读取");
		}
		boolean  b=validOrder(arr, is);
		if(is!=null){
			try {
				is.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
    	return b;
    }
    public static boolean validOrder(String[] arr, InputStream input) throws IOException {
    
    	BufferedInputStream bis=new BufferedInputStream(input);
    	if(POIXMLDocument.hasOOXMLHeader(bis)){
    		XSSFWorkbook wbs = new XSSFWorkbook(bis);
    		return validOrder(arr, wbs.getSheetAt(0));
    	}else if(POIFSFileSystem.hasPOIFSHeader(bis)){
    		HSSFWorkbook wbs = new HSSFWorkbook(bis);
    		return validOrder(arr, wbs.getSheetAt(0));
    	}else{
    		throw new IllegalArgumentException("文件不能读取");
    		
    	}
    	
    }

    public static void writeLineToRow(Iterable<Object> obj, HSSFRow row) {
        int i = 0;
        for (Object object : obj) {
            if (Double.class.isInstance(object)) {
                row.createCell(i).setCellValue((double) object);
            } else if (Integer.class.isInstance(object)) {
                row.createCell(i).setCellValue((int) object);
            } else if (Boolean.class.isInstance(object)) {
                row.createCell(i).setCellValue((boolean) object);
            } else if (Date.class.isInstance(object)) {
                HSSFCell cell = row.createCell(i);
                HSSFWorkbook wb = row.getSheet().getWorkbook();
                HSSFDataFormat format = wb.createDataFormat();
                HSSFCellStyle cellStyle = wb.createCellStyle();
                cellStyle.setDataFormat(format.getFormat("yyyy/m/d hh:mm:ss"));
                cell.setCellStyle(cellStyle);
                cell.setCellValue((Date) object);
            } else if (Calendar.class.isInstance(object)) {
                row.createCell(i).setCellValue((Calendar) object);
            } else if (String.class.isInstance(object)) {
                row.createCell(i).setCellValue((String) object);
            } else {
                row.createCell(i).setCellValue(object.toString());
            }
            i++;
        }
    }

    /**
     * 校验excel表头是不是和传入数组相同（顺序必须也一样）
     * 
     * @param arr
     * @return
     */
    public static boolean validOrder(String[] arr, HSSFSheet sheet, int rowIndex) {
        boolean re = true;
        HSSFRow row = sheet.getRow(rowIndex);
        int lastNum = row.getLastCellNum();
        if (lastNum != arr.length) {
            re = false;
            return re;
        }
        if (null != row) {
            for (int k = 0; k < row.getLastCellNum(); k++) {
                HSSFCell cell = row.getCell(k);
                String val = getCellValue(cell);
                if (!arr[k].equals(val)) {
                    re = false;
                    break;
                }
            }
        }
        return re;
    }
    public static boolean validOrder(String[] arr, XSSFSheet sheet, int rowIndex) {
    	boolean re = true;
    	XSSFRow row = sheet.getRow(rowIndex);
    	int lastNum = row.getLastCellNum();
    	if (lastNum != arr.length) {
    		re = false;
    		return re;
    	}
    	if (null != row) {
    		for (int k = 0; k < row.getLastCellNum(); k++) {
    			XSSFCell cell = row.getCell(k);
    			String val = getCellValue(cell);
    			if (!arr[k].equals(val)) {
    				re = false;
    				break;
    			}
    		}
    	}
    	return re;
    }

    /**
     * 读取excel到实体类,默认从第一个sheet开始读取
     * 
     * @param is
     *            excel输入流
     * @param colunms
     *            excel的字段对应的实体类的属性名称，以excel顺序传入
     * @param classType
     * @param fromRow
     *            从第几行开始读取
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws ParseException
     * @throws IOException
     */
    public static <T> List<T> readInputStreamToEntity(InputStream is, String[] colunms, Class<T> classType, int fromRow)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, InvocationTargetException, ParseException, IOException {
    	if(POIXMLDocument.hasOOXMLHeader(is)){
    		 XSSFWorkbook wbs = new XSSFWorkbook(is);
    		
    	        return readHSSFWorkbookToEntity(wbs, colunms, classType, fromRow);
    	}else if(POIFSFileSystem.hasPOIFSHeader(is)){
    		 HSSFWorkbook wbs = new HSSFWorkbook(is);
 	        return readHSSFWorkbookToEntity(wbs, colunms, classType, fromRow);
    	}else{
    		throw new FileSystemException("excel", "unknow", "未知文件类型");
    		
    	}
       
    }
   

    /**
     * @param wbs
     * @param colunms
     * @param classType
     * @param fromRow
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws ParseException
     * @throws IOException
     */
    public static <T> List<T> readHSSFWorkbookToEntity(HSSFWorkbook wbs, String[] colunms, Class<T> classType, int fromRow)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, InvocationTargetException, ParseException, IOException {

        HSSFSheet childSheet = wbs.getSheetAt(0);
        return readSheetToEntity(childSheet, colunms, classType, fromRow);
    }
    /**
     * 读取10版本excel
     * @param wbs
     * @param colunms
     * @param classType
     * @param fromRow
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws ParseException
     * @throws IOException
     */
    public static <T> List<T> readHSSFWorkbookToEntity(XSSFWorkbook wbs, String[] colunms, Class<T> classType, int fromRow) throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, InvocationTargetException, ParseException
    		 {
    	
    	XSSFSheet childSheet=wbs.getSheetAt(0);
    	return readSheetToEntity(childSheet, colunms, classType, fromRow);
    }

    public static <T> List<T> readSheetToEntity(HSSFSheet childSheet, String[] colunms, Class<T> classType, int fromRow)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, InvocationTargetException, ParseException, IOException {
        List<T> list = new ArrayList<T>();
        for (int j = fromRow; j <= childSheet.getLastRowNum(); j++) {
            T obj = classType.newInstance();
            HSSFRow row = childSheet.getRow(j);
            if (null != row) {
                for (int k = 0; k < colunms.length; k++) {
                    HSSFCell cell = row.getCell(k);
                    String val;
                    if(StringUtils.isBlank(colunms[k])){
    					continue;
    				}
					try {
						val = getCellValue(cell);
					} catch (Exception e) {
						logger.error("字段转换失败！");
						e.printStackTrace();
						throw new IllegalDataException(String.format("数据转换出错，位于第%d行，第%d列", j+1,k+1));
					}

                    setFieldValue(colunms[k], classType, obj, val);
                }
            }
            list.add(obj);

        }

        return list;

    }
    public static <T> List<T> readSheetToEntity(XSSFSheet childSheet, String[] colunms, Class<T> classType, int fromRow) throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, InvocationTargetException, ParseException
    		 {
    	List<T> list = new ArrayList<T>();
    	for (int j = fromRow; j <= childSheet.getLastRowNum(); j++) {
    		logger.debug("读取第{}行",j);
    		T obj = classType.newInstance();
    		XSSFRow row = childSheet.getRow(j);
    		if (null != row) {
    			
    			for (int k = 0; k < colunms.length; k++) {
    				logger.debug("读取第{}行,第{}列",j,k);
    				if(StringUtils.isBlank(colunms[k])){
    					continue;
    				}
    				XSSFCell cell = row.getCell(k);
    				try {
						String val = getCellValue(cell);
						setFieldValue(colunms[k], classType, obj, val);
					} catch (Exception e) {
						
						e.printStackTrace();
						throw new IllegalDataException(String.format("数据转换出错，位于第%d行，第%d列", j+1,k+1));
					}
    			}
    		}
    		list.add(obj);
    		
    	}
    	
    	return list;
    	
    }

    /**
     * @param file
     *            excel类型的文件
     * @param colunms
     * @param classType
     * @param fromRow
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws ParseException
     * @throws IOException
     */
    public static <T> List<T> readExcelToEntity(File file, String[] colunms, Class<T> classType, int fromRow) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException,
            InvocationTargetException, ParseException, IOException {
        if (!file.isFile()) {
            throw new IllegalArgumentException("文件不能读取");
        }
        FileInputStream is = new FileInputStream(file);
        BufferedInputStream bis=new BufferedInputStream(is);
        List<T> list = readInputStreamToEntity(bis, colunms, classType, fromRow);
        if(bis!=null){
        	try {
				bis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        return list;
    }

    /**
     * 更改obj对应的属性值，目前支持几个常用基本的属性类型
     * 
     * @param filedName
     * @param type
     * @param obj
     * @param val
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws ParseException
     * @throws InstantiationException
     */
    private static void setFieldValue(String filedName, Class<?> type, Object obj, String val) throws NoSuchMethodException, SecurityException,
            NoSuchFieldException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException, InstantiationException {
        Field field;
		try {
			field = type.getDeclaredField(filedName);
		} catch (Exception e) {
			logger.warn("未发现定义字段",e);
			field=null;
		}
		if(null==field){
			logger.info("未序列化{}字段",filedName);
			return;
		}
        String fistletter = filedName.substring(0, 1).toUpperCase();
        String setMethodName = "set" + fistletter + filedName.substring(1);
        Method setMethod = type.getDeclaredMethod(setMethodName, new Class[] { field.getType() });
        if (field.getType().isAssignableFrom(String.class)) {
            setMethod.invoke(obj, val);
        } else if (field.getType().isAssignableFrom(int.class)) {
            setMethod.invoke(obj, parseInt(val));
        } else if (field.getType().isAssignableFrom(double.class)) {
            setMethod.invoke(obj, parseDouble(val));
        } else if (field.getType().isAssignableFrom(Date.class)) {

            setMethod.invoke(obj, parseDate(val, "yyyy-MM-dd HH:mm:ss"));

        } else if (field.getType().isAssignableFrom(boolean.class)) {

            setMethod.invoke(obj, parseBoolean(val));
        }else if(field.getType().isEnum()){
        	setMethod.invoke(obj,paresEnum((Class<? extends Enum>) field.getType(),val));
        }
        else 
        {

            setMethod.invoke(obj, new Object[] { field.getType().getConstructor(String.class).newInstance(val) });
        }

    }

    /**
     * 返回String类型的cell值
     * 
     * @param cell
     * @return
     */
    private static String getCellValue(HSSFCell cell) {
        String value = "";
        if (null != cell) {
            switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC: // 数字
                if (HSSFDateUtil.isCellDateFormatted(cell)|| isCellDateFormatted(cell)) {
                    value = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(cell.getDateCellValue());
                } else {
                    value = String.valueOf(cell.getNumericCellValue());
                }
                break;
            case HSSFCell.CELL_TYPE_STRING: // 字符串
                value = cell.getStringCellValue().trim();
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN: // Boolean
                value = String.valueOf(cell.getBooleanCellValue());
                break;
            case HSSFCell.CELL_TYPE_BLANK: // 空值
                break;
            case HSSFCell.CELL_TYPE_ERROR: // 故障
                break;

            default:
               logger.debug("未知类型   ");
                break;
            }
            return value;
        } else {
            return "";
        }

    }
    public static boolean isCellDateFormatted(Cell cell) {
        if (cell == null) return false;
        boolean bDate = false;

        double d = cell.getNumericCellValue();
        if ( DateUtil.isValidExcelDate(d) ) {
            CellStyle style = cell.getCellStyle();
            if(style==null) return false;
            int format= style.getDataFormat();
           
            String f = style.getDataFormatString();
            if(f!=null&&f.indexOf("月")>0){
            bDate = true;
            }else
            if(format == 14 || format == 31 || format == 57 || format == 58){  
                //日期  
               // sdf = new SimpleDateFormat("yyyy-MM-dd");  
            	 bDate = true;
            }else if (format == 20 || format == 32) {  
                //时间  
              //  sdf = new SimpleDateFormat("HH:mm"); 
            	 bDate = true;
            }
        }
        return bDate;
    }
    /**
     * 返回String类型的cell值
     * 
     * @param cell
     * @return
     */
    private static String getCellValue(XSSFCell cell) {
    	String value = "";
    	if (null != cell) {
    		switch (cell.getCellType()) {
    		case Cell.CELL_TYPE_NUMERIC: // 数字
    			if (HSSFDateUtil.isCellDateFormatted(cell)||isCellDateFormatted(cell)) {
    				value = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(cell.getDateCellValue());
    			} else {
    				value = String.valueOf(cell.getNumericCellValue());
    				value=value.replaceAll("\\.0$", "");
    					
    			}
    			break;
    		case Cell.CELL_TYPE_STRING: // 字符串
    			value = cell.getStringCellValue().trim();
    			break;
    		case Cell.CELL_TYPE_BOOLEAN: // Boolean
    			value = String.valueOf(cell.getBooleanCellValue());
    			break;
    		case Cell.CELL_TYPE_BLANK: // 空值
    			break;
    		case Cell.CELL_TYPE_ERROR: // 故障
    			break;
    			
    		default:
    			break;
    		}
    		return value;
    	} else {
    		return "";
    	}
    	
    }

    private static int parseInt(String str) {

        str = str.trim();
        if (str.equals("")) {
            return 0;
        }
        Double d = new Double(str);
        Long lnum = Math.round(d);
        return lnum.intValue();
    }

    private static double parseDouble(String str) {

        str = str.trim();
        if (str.equals("")) {
            return 0;
        }
        Double d = new Double(str);

        return d;
    }

    private static boolean parseBoolean(String str) {

        str = str.trim();
        if (str.equals("")) {
            return false;
        }
        Boolean d = new Boolean(str);

        return d;
    }
    private static Enum<?> paresEnum(Class<? extends Enum> enumType,String str) {
    	
    	 for(Enum<?> item : enumType.getEnumConstants()) {
             if(item.toString().equalsIgnoreCase(str)) {
                 return item;
             }
         }
    	 return null;
    }

    private static Date parseDate(String str, String format) throws ParseException {
        if (str == null || str.trim().equals("")) {
            return null;
        }
        SimpleDateFormat simpFormat = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = simpFormat.parse(str);
        } catch (Exception e) {
            simpFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                date = simpFormat.parse(str);
            } catch (ParseException e1) {
                simpFormat = new SimpleDateFormat("yyyy/MM/dd");
                try {
                    date = simpFormat.parse(str);
                } catch (ParseException e2) {
                    simpFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                    try {
                        date = simpFormat.parse(str);
                    } catch (ParseException e3) {
                        simpFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        date = simpFormat.parse(str);
                    }
                }
            }

        }
        return date;
    }

    private static String toUpFirst(String str) {
        char[] arr = str.toCharArray();
        if (97 <= arr[0] && 122 >= arr[0]) {
            arr[0] -= 32;
            return String.valueOf(arr);
        } else {
            return str;
        }

    }
}
