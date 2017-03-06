package com.crazypig.httpserver.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ByteUtilTest {
	
	@Test
	public void simpleTest() {
		
		String str = "abcdefg";
		String str1 = "ef";
		byte[] source = str.getBytes();
		byte[] target = str1.getBytes();
		printBytes(source);
		printBytes(target);
		int index = ByteUtil.indexOf(source, target);
		assertEquals(4, index);
		assertEquals("abcd", new String(source, 0, index));
		
	}
	
	private void printBytes(byte[] bytes) {
		for(int i = 0; i < bytes.length; i++) {
			System.out.print(bytes[i] + " ");
		}
		System.out.println();
	}

}
