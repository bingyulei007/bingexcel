package com.bing.other;

import org.junit.Test;

public class TestTest {
	@Test
	public void testme() {
		StringBuilder bd=new StringBuilder("adfasdf,");
		System.out.println(bd);
		StringBuilder builder = bd.replace(bd.length()-1, bd.length(), "");
		System.out.println(bd);
		System.out.println(builder);
	}
}
