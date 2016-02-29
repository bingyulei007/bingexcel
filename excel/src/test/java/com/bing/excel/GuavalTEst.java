package com.bing.excel;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Primitives;

public class GuavalTEst {
	private static LoadingCache<String, String> cacheFormCallable = null;

	@Test
	public void testOptional1() {
		String a = null;
		// Optional<String> possible = Optional.of(a);
		Optional<String> possible = Optional.fromNullable(a);
		System.out.println(possible.isPresent());
		// System.out.println(possible.get());
	}

	@Test
	public void testComparisonChain() {
		Person p1 = new Person(1, "asdf", 12);
		Person p2 = new Person(1, "asdf", 12);
		boolean b = p1.equals(p2);
		System.out.println(b);

	}

	@Test
	public void testStringFormat() {
		System.out.println(String.format("转换 %s为Double发生错误", "abc"));
		System.out.println(Double.MAX_VALUE);
	}

	@Test
	public void testCach() throws ExecutionException {
		cacheFormCallable = CacheBuilder.newBuilder().maximumSize(1000)
				.build(new CacheLoader() {

					@Override
					public Object load(Object key) throws Exception {

						return ((Long) new Date().getTime()).toString();
					}
				});
		System.out.println(cacheFormCallable.get("a"));
		System.out.println(cacheFormCallable.get("a"));
		System.out.println(cacheFormCallable.getUnchecked("b0"));
		System.out.println(cacheFormCallable.getUnchecked("b"));
	}

	@Test
	public void testStringim() {
		ImmutableSet<String> set = ImmutableSet.of("a", "b", "c", "d");
		for (String string : set) {
			System.out.println(string);
		}
	}
	@Test
	public void testtypenull() {
		Object[] arr={"a","b","",null,45};
		for (int i = 0; i < arr.length; i++) {
			Class depType = arr[i].getClass();
			System.out.println(depType.isPrimitive());
			System.out.println(depType);
		}
	}
	@Test
	public void testBox() {
		Class<Integer> wrap = Primitives.wrap(int.class);
		Class box = com.thoughtworks.xstream.core.util.Primitives.box(int.class);
		System.out.println(wrap);
		System.out.println(box);
	}

	static class Person implements Comparable<Person> {
		private int id;
		private String name;
		private int age;

		public Person(int id, String name, int age) {
			super();
			this.id = id;
			this.name = name;
			this.age = age;
		}

		public Person() {
			super();
		}

		public Person(String name, int age) {
			super();
			this.name = name;
			this.age = age;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		@Override
		public int compareTo(Person that) {
			return ComparisonChain.start().compare(id, that.id)
					.compare(age, that.age).compare(name, that.name).result();
		}

	}
}
