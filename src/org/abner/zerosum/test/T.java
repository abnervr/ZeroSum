/* Copyright (c) 2014 G.I.C Consultoria e Comunicação Ltda */
package org.abner.zerosum.test;

public class T {

	public static void main(String[] args) {
		test("123123123");
		test("12/23/23");
		test("12:23");
		test("1.231.231,23");
		test("1,231,231.23");

	}

	private static boolean test(String value) {
		System.out.print(value + " ");
		boolean matches = value.matches("[0-9\\.,]+");
		System.out.print(matches + ", ");
		matches = value.matches("[0-9]\\z");
		System.out.print(matches + ", ");
		matches = value.matches("\\A[0-9]");
		System.out.print(matches + ", ");
		matches = value.matches("[\\.,]");
		System.out.println(matches);
		return matches;
	}
}
