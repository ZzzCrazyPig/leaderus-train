package com.crazypig.httpserver.simple;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Http Request Handler
 * @author CrazyPig
 * @since 2017-03-02
 *
 */
public class RequestHandler implements Runnable {
	
	private Socket socket;
	
	public RequestHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		
		HttpResponse response = null;
		
		try {
			InputStream in = socket.getInputStream();
			HttpRequest request = new HttpRequest(in);
			response = new HttpResponse(socket.getOutputStream(), request);
			request.parseRequestLineAndHeaders();
			if(request.isEndParseHeader()) {
				if(request.isMultiPart()) {
					response.processFileUpload();
				} else {
					response.returnStaticResource();
				}
			}
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			if(response != null) {
				response.errorResponse(500, "Server Error", e.getMessage());
			}
		}
		
	}
	
}
