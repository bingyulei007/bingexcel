package com.bing.utils;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.attribute.IntegerSyntax;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.primitives.Booleans;
import com.google.common.primitives.Ints;

public class StringParseUtil {
	public static Logger logger = LoggerFactory
			.getLogger(StringParseUtil.class);

	/**
	 * Parses the string argument as a boolean. The {@code boolean} returned
	 * represents the value {@code true} if the string argument is not
	 * {@code null} and is equal, ignoring case, to the string {@code "true"},
	 * {@code "yes"},{@code "是"},{@code "a"} etc.
	 * <p>
	 * Example: {@code Boolean.parseBoolean("True")} returns {@code true}.<br>
	 * Example: {@code Boolean.parseBoolean("N")} returns {@code false}.
	 * Example: {@code Boolean.parseBoolean("No")} returns {@code false}.
	 * 
	 * @param s
	 *            the {@code String} containing the boolean representation to be
	 *            parsed
	 * @return the boolean represented by the string argument
	 */
	public static boolean parseBoolean(String s) {

		return ((!Strings.isNullOrEmpty(s) )&& toBoolean(s));
	}

	private static boolean toBoolean(String name) {
		if (name.equalsIgnoreCase("false") || name.equalsIgnoreCase("no")) {
			return false;
		}

		if (ArrayUtils.contains(new String[] { "否", "假", "N", "n", "0" }, name)) {
			return false;
		}

		return true;
	}

	static final Pattern FLOATING_POINT_PATTERN = fpPattern();
	static final Pattern FLOATING_POINT_PATTERN1 = Pattern
			.compile("0[xX](?:\\p{XDigit}++(?:\\.\\p{XDigit}*+)?|\\.\\p{XDigit}++)");
	static final Pattern CURRENCY_POINT_PATTERN = Pattern
			.compile("(?<=^[$￥])(?:\\d++(?:\\.\\d*+)?|\\.\\d++)$");
	static final Pattern FRACTION_POINT_PATTERN = Pattern
			.compile("^-?(?:0|[1-9]\\d*)/-?[1-9]\\d*$");
	static final Pattern PERCENT_POINT_PATTERN = Pattern
			.compile("^(?:[1-9]\\d*(?:\\.\\d*+)?|0?\\.\\d++)(?=%$)");

	private static Pattern fpPattern() {
		String decimal = "(?:\\d++(?:\\.\\d*+)?|\\.\\d++)";
		String completeDec = decimal + "(?:[eE][+-]?\\d++)?[fFdD]?";
		String hex = "(?:\\p{XDigit}++(?:\\.\\p{XDigit}*+)?|\\.\\p{XDigit}++)";
		String completeHex = "0[xX]" + hex + "[pP][+-]?\\d++[fFdD]?";
		String fpPattern = "[+-]?(?:NaN|Infinity|" + completeDec + "|"
				+ completeHex + ")";
		return Pattern.compile(fpPattern);
	}

	/**
	 * Parses the specified string as a double-precision floating point value.
	 * The ASCII character {@code '-'} (<code>'&#92;u002D'</code>) is recognized
	 * as the minus sign.
	 * 
	 * @param string
	 *            the string representation of a {@code double} value
	 * @return the floating point value represented by {@code string}, or
	 *         {@code null} if {@code string} has a length of zero or cannot be
	 *         parsed as a {@code double} value
	 */
	public static Double parseDouble(String string) {
		if (StringUtils.isBlank(string)) {
			return null;
		}
		if (FLOATING_POINT_PATTERN.matcher(string).matches()) {
			// TODO(user): could be potentially optimized, but only with
			// extensive testing
			try {
				return Double.parseDouble(string);
			} catch (Exception e) {
				// Double.parseDouble has changed specs several times, so fall
				// through
				logger.error(String.format("转换 %s为Double发生错误", string), e);
			}
		}
		// currency formater
		Matcher matcher = CURRENCY_POINT_PATTERN.matcher(string);
		if (matcher.find()) {
			try {
				return Double.parseDouble(matcher.group());
			} catch (Exception e) {
				logger.error(String.format("转换 %s为Double发生错误", string), e);
			}
		}
		// percent formater
		 matcher = PERCENT_POINT_PATTERN.matcher(string);
		if (matcher.find()) {
			try {
				char[] charArray = matcher.group().toCharArray();
				int pIndex=charArray.length;
				StringBuilder sb=new StringBuilder();
				for (int i=0;i<charArray.length;i++) {
					char temp=charArray[i];
					if(temp=='.'){
						pIndex=i;
					}else{
						sb.append(temp);
					}
				}
				pIndex-=2;
				while(pIndex<0){
					sb.insert(0,'0');
					pIndex++;
				}
				sb.insert(pIndex,'.');
				 return parseDouble(sb.toString());
			} catch (Exception e) {
				logger.error(String.format("转换 %s为Double发生错误", string), e);
			}
		}
		// fraction formater
		if (FRACTION_POINT_PATTERN.matcher(string).matches()) {
			try {
				return parseFraction2Double(string);
			} catch (Exception e) {
				logger.error(String.format("转换 %s为Double发生错误", string), e);
			}
		}

		if (DataTypeDetect.isDateType(string)) {
			try {
				Date date = convertYMDT2Date(string);
				return convertToDaouble(date);
			} catch (Exception e) {
				logger.error(String.format("转换 %s为Double发生错误", string), e);
			}
		}

		boolean b = FLOATING_POINT_PATTERN1.matcher(string).matches();
		if (b) {
			string += "p0";
			try {
				return Double.parseDouble(string);
			} catch (Exception e) {
				logger.error(String.format("转换 %s为Double发生错误", string), e);
			}
		}
		return null;
	}

	/**
	 * Parses the string argument as a signed decimal integer.
	 * 
	 * @param s
	 *            a {@code String} containing the {@code int} representation to
	 *            be parsed
	 * @return Long or null if can't to be parsed;
	 */
	public static Long parseInteger(String s) {

		return parseInteger(s, 10);
	}

	/**
	 * Parses the string argument as a signed {@code Date}.
	 * 
	 * @param s
	 *            a {@code String} containing the {@code int} representation to
	 *            be parsed
	 * @return  Date or null if can't be parsed
	 */
	public static Date parseDate(String s) {
		if (DataTypeDetect.isDateType(s)) {
			try {
				Date date = convertYMDT2Date(s);
				return date;
			} catch (Exception e) {
				logger.error(String.format("转换 %s为Date发生错误", s), e);
			}
		}
		if(DataTypeDetect.isNumType(s)){
			Double d = parseDouble(s);
			try {
				Date date=convertToDate(d);
				return date;
			} catch (Exception e) {
				logger.error(String.format("转换 %s为Date发生错误", s), e);
			}
		}
		return null;
	}

	/**
	 * 必须保证arg为日期类型，可用{@link DataTypeDetect#parseDouble(String)}
	 * 
	 * @param arg
	 * @return
	 * @throws ParseException
	 */
	public static Date convertYMDT2Date(String arg) throws ParseException {
		String temp;
		temp = arg.replaceAll("[/\\\\年月_]", "-");
		temp = temp.replaceAll("[日号]", " ");
		temp = temp.replaceAll("[点时分秒]", ":");

		SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		try {
			Date date = format.parse(temp);
			return date;
		} catch (ParseException e) {
			try {
				format.applyPattern("yy-MM-dd HH:mm");
				Date date = format.parse(temp);
				return date;
			} catch (ParseException e1) {
				format = new SimpleDateFormat("yy-MM-dd");
				Date date = format.parse(temp);
				return date;
			}
		}

	}

	public static Long parseInteger(String string, int radix) {
		if (Strings.isNullOrEmpty(string)) {
			return null;
		}
		if (radix == 10) {
			Double d = parseDouble(string);
			d.longValue();
		} else {
			// TODO the others neet to do
		}
		return null;
	}

	/**
	 * 只能用于excel数据类型
	 * 
	 * @author shizhongtao
	 * @param date
	 * @return
	 */
	static double convertToDaouble(Date date) {
		String startDate = "1899-12-31";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",
				Locale.SIMPLIFIED_CHINESE);
		Date firstDate;
		try {
			firstDate = format.parse(startDate);
		} catch (ParseException e) {
			throw new IllegalStateException("java 未知错误");
		}

		long diff = date.getTime() - firstDate.getTime();
		double re = -1;
		if (!(diff < (86400 * 1000))) {
			re = diff / (86400d * 1000d);
		}
		return re;
	}

	static Double parseFraction2Double(String string) {
		char[] array = string.toCharArray();
		int numerator = 0;
		int denominator = 0;
		StringBuilder bd = new StringBuilder();
		boolean limit = true;
		try {
			for (char c : array) {
				if (c != '/') {
					bd.append(c);

				} else {
					if (limit) {
						numerator = Integer.valueOf(bd.toString());
					}
					bd.setLength(0);
					limit = false;
				}
			}
			denominator = Integer.valueOf(bd.toString());
			if(numerator==0){
				return 0d;
			}
			if (denominator != 0) {
				return numerator / (double) denominator;
			}
		} catch (NumberFormatException e) {

		}
		return null;
	}

	/**
	 * @author shizhongtao
	 * @param date
	 * @return
	 */
	static long convertToLong(Date date) {
		return ((Double) convertToDaouble(date)).longValue();
	}

	/**
	 * @author shizhongtao
	 * @param d
	 * @return
	 */
	static Date convertToDate(double d) {
		String startDate = "1889-12-30";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",
				Locale.SIMPLIFIED_CHINESE);
		Date firstDate;
		try {
			firstDate = format.parse(startDate);
		} catch (ParseException e) {
			throw new IllegalStateException("java 未知错误");
		}
		if (d < 1d) {
			return firstDate;
		} else {
			try {
				firstDate = format.parse("1899-12-31");
			} catch (ParseException e) {
				throw new IllegalStateException("java 未知错误");
			}

			Calendar c = Calendar.getInstance();
			c.setTime(firstDate);
			c.set(Calendar.HOUR_OF_DAY, 0);

			long diff = ((Double) (d * 86400 * 1000)).longValue();
			long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数129600000
			long nh = 1000 * 60 * 60;// 一小时的毫秒数
			long nm = 1000 * 60;// 一分钟的毫秒数
			long ns = 1000;// 一秒钟的毫秒数
			int day = (int) (diff / nd);// 计算差多少天
			c.add(Calendar.DATE, day);
			long remaining = diff % nd;
			int hour = (int) (remaining / nh);// 计算差多少小时
			remaining %= nh;
			int min = (int) (remaining / nm);// 计算差多少分钟
			remaining %= nm;
			int sec = (int) (remaining / ns);// 计算差多少秒
			int milliSec = (int) (remaining % 1000);
			if (hour > 0) {
				c.roll(Calendar.HOUR_OF_DAY, hour);
			}
			c.set(Calendar.MINUTE, 0);

			if (min > 0) {
				c.roll(Calendar.MINUTE, min);
			}
			c.set(Calendar.SECOND, 0);

			if (sec > 0) {
				c.roll(Calendar.SECOND, sec);
			}
			c.set(Calendar.MILLISECOND, 0);
			if (milliSec > 0) {
				c.roll(Calendar.MILLISECOND, (int) milliSec);
			}
			return c.getTime();
		}
	}

	/**
	 * @author shizhongtao
	 * @param l
	 * @return
	 */
	static Date convertToDate(long l) {
		return convertToDate((double) l);
	}
}
