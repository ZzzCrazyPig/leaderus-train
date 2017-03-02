package com.crazypig.httpserver.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.Socket;
import java.nio.charset.Charset;

public class RequestHandler implements Runnable {
	
	private Socket socket;
	
	public RequestHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		
		try {
			InputStream in = socket.getInputStream();
			LineNumberReader reader = new LineNumberReader(new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8"))));
			String requestLine = reader.readLine();
			// gen HttpRequest
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
