package com.crazypig.httpserver.simple;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

public class BufferedReaderTest {
	
	@Test
	public void testSkip() throws IOException {
		
		System.out.println("/r/n".getBytes());
		
		String str = "0123456789";
		ByteArrayInputStream bin = new ByteArrayInputStream(str.getBytes());
		BufferedReader bufReader = new BufferedReader(new InputStreamReader(bin));
		long skip = bufReader.skip(2);
		assertEquals(2, skip);
		assertEquals("23456789", bufReader.readLine());
	}

}
