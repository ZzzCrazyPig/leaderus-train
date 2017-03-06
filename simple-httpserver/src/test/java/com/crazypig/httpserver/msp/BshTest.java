package com.crazypig.httpserver.msp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import bsh.EvalError;
import bsh.Interpreter;

public class BshTest {
	
	@Test
	public void simpleTest() throws IOException, EvalError, URISyntaxException {
		Interpreter ipter = new Interpreter();
		String filename = BshTest.class.getResource("example.bsh").getFile();
		ipter.set("user", "CrazyPig");
		ipter.set("Unknown", "Unknown");
		Object result = ipter.source(filename);
		assertEquals("Hello CrazyPig", result);
	}

}
