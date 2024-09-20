package com.auros.utils;

public class Utils {

	public Utils() {
		// TODO Auto-generated constructor stub
	}

	public static int mod(int x, int y) {
		int div = x / y;
		int result = x % y;
		return result > 0 ? div + 1 : div;
	}

}
