package com.crazypig.httpserver.simple;

import java.io.IOException;

public class Main {
	
	public static void main(String[] args) {
		
		try {
			new SimpleHttpServer().startup();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
