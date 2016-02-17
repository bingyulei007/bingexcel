package com.bing.excel;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ComparisonChain;

public class GuavalTEst {
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
		Person p1=new Person(1, "asdf", 12);
		Person p2=new Person(1, "asdf", 12);
		boolean b = p1.equals(p2);
		System.out.println(b);
		
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
