package com.bing.excel;

import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.bing.excel.GuavalTEst.Person;
import com.bing.utils.StringParseUtil;
import com.google.common.collect.Lists;

public class StringParesUtilTest {
	@Test
	public void testBooleanPares() {
		boolean parseBoolean = StringParseUtil.parseBoolean("");
		System.out.println(parseBoolean);
		boolean parseBoolean1 = StringParseUtil.parseBoolean("a");
		System.out.println(parseBoolean1);
		boolean parseBoolean2 = StringParseUtil.parseBoolean("n");
		System.out.println(parseBoolean2);
		boolean parseBoolean3 = StringParseUtil.parseBoolean("N");
		System.out.println(parseBoolean3);
		boolean parseBoolean4 = StringParseUtil.parseBoolean("no");
		System.out.println(parseBoolean4);
		System.out.println(StringParseUtil.parseBoolean("假"));
		System.out.println(StringParseUtil.parseBoolean("false"));
		System.out.println(StringParseUtil.parseBoolean("z"));
		System.out.println(StringParseUtil.parseBoolean("yes"));
		System.out.println(StringParseUtil.parseBoolean("真"));
		System.out.println(StringParseUtil.parseBoolean("true"));
		System.out.println(StringParseUtil.parseBoolean("y"));
		System.out.println(StringParseUtil.parseBoolean("你好"));

	}

	@Test
	public void testDoublePares() {
		System.out.println(StringParseUtil.parseDouble("假"));
		System.out.println(StringParseUtil.parseDouble("12.36.35"));
		System.out.println(StringParseUtil.parseDouble("12.56"));
		System.out.println(StringParseUtil.parseDouble("12"));
		System.out.println(StringParseUtil.parseDouble("0.36"));
		System.out.println(StringParseUtil.parseDouble(".36"));
		System.out.println(StringParseUtil.parseDouble(".36f"));
		System.out.println(StringParseUtil.parseDouble(".36d"));
		System.out.println(StringParseUtil.parseDouble("0x0.36"));
		System.out.println(StringParseUtil.parseDouble("0x.36p0"));
		System.out.println(StringParseUtil.parseDouble("0x.36p1"));
		DecimalFormat format = new DecimalFormat("#.00");
		System.out.println(format.format(StringParseUtil.parseDouble("211111111111111111111112.002")));

	}

	@Test
	public void testPares() {
		final Pattern FLOATING_POINT_PATTERN1 = Pattern
				.compile("(?<=^[$￥])(?:\\d++(?:\\.\\d*+)?|\\.\\d++)$");
		Matcher matcher = FLOATING_POINT_PATTERN1.matcher("$12.23");
		if (matcher.find()) {
			System.out.println(matcher.group());
		}

	}

	@Test
	public void testPares2() {
		final Pattern FLOATING_POINT_PATTERN2 = Pattern
				.compile("^[1-9]\\d{,9}/[1-9]\\d{,9}$");
		Matcher matcher = FLOATING_POINT_PATTERN2.matcher("12/23");
		if (matcher.find()) {
			
		}

	}

	

	@Test
	public void testInrPercent(){
	
		Pattern PERCENT_POINT_PATTERN = Pattern
				.compile("^(?:[1-9]\\d*(?:\\.\\d*+)?|0?\\.\\d++)(?=%$)");
		Matcher matcher = PERCENT_POINT_PATTERN.matcher(".23%");
		if (matcher.find()) {
			System.out.println(matcher.group());
		}
	}
	@Test
	public void testInr(){
//		System.out.println(StringParseUtil.parseDouble("0/-24"));
//		System.out.println(StringParseUtil.parseDate("0/-24"));
//		System.out.println(StringParseUtil.parseDate("23.35"));
//		System.out.println(StringParseUtil.parseDate("42277.99930555555"));
//		System.out.println(StringParseUtil.parseDate("42277.5"));
//		System.out.println(StringParseUtil.parseDouble("2015年10月1号 12点00分"));
//		System.out.println(StringParseUtil.parseDouble("2015年10月1号 23点59分"));
//		System.out.println(StringParseUtil.parseDouble("23.35%"));
		System.out.println(StringParseUtil.parseDouble(".35%"));
		System.out.println(StringParseUtil.parseDouble("0.35%"));
		System.out.println(StringParseUtil.parseDouble("1.1%"));
		System.out.println(StringParseUtil.parseDouble("300%"));
		System.out.println(StringParseUtil.parseDouble("31.00%"));
		System.out.println(StringParseUtil.parseDouble("31.%"));
	}
	
	@Test
	public void testMaplist(){
		/*Person person = new Person(023, "liming", 23);
		System.out.println(person);
		Map<List<Object>, Person> map=new HashMap<>();
		ArrayList<Object> list = Lists.newArrayList();
		list.add("a");
		list.add("b");
		list.add(false);
		list.add(34);
		list.add(Person.class);
		map.put(list, person);
		
		ArrayList<Object> listb = Lists.newArrayList();
		listb.add("a");
		listb.add("b");
		listb.add(false);
		listb.add(new Integer(34));
		listb.add(Person.class);
		System.out.println(map.get(list));
		System.out.println(map.get(listb));*/
		 final Constructor[] ctors = Person.class.getConstructors();
         if (ctors.length > 1) {
             Arrays.sort(ctors, new Comparator() {
                 public int compare(final Object o1, final Object o2) {
                     return ((Constructor)o2).getParameterTypes().length
                         - ((Constructor)o1).getParameterTypes().length;
                 }
             });
         }
		System.out.println(ctors);
	}
	
}
