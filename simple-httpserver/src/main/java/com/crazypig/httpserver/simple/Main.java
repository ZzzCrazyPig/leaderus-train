package com.crazypig.httpserver.simple;

import java.io.IOException;

/**
 * 
 * @author CrazyPig
 * @since 2017-03-02
 *
 */
public class Main {
	
	public static void main(String[] args) {
		
		try {
			new SimpleHttpServer().startup();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
