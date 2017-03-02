package com.crazypig.httpserver.simple;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class RequestHandler implements Runnable {
	
	private Socket socket;
	
	public RequestHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		
		try {
			InputStream in = socket.getInputStream();
			HttpRequest request = new HttpRequest(in);
			request.parseRequestLineAndHeaders();
			if(request.isMultiPart()) {
				// TODO process file upload
			} else {
				request.parseRequestBody();
				// handle static resource
				HttpResponse response = new HttpResponse(socket.getOutputStream(), request);
				response.returnStaticResource();
			}
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
