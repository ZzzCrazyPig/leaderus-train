package com.crazypig.httpserver.simple;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HttpResponse {
	
	private OutputStream out;
	private HttpRequest request;
	
	public HttpResponse(OutputStream out, HttpRequest request) {
		this.out = out;
		this.request = request;
	}
	
	public void returnStaticResource() {
		String uri = request.getUri();
		System.out.println("request static resource : " + uri);
		File resFile = new File(SimpleHttpServer.WEB_ROOT, uri);
		if(resFile.exists() && resFile.isFile()) {
			// find static resouce
			FileInputStream fin = null;
			try {
				fin = new FileInputStream(resFile);
				byte[] buffer = new byte[1024];
				int len = -1;
				while((len = fin.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				out.flush();
			} catch (IOException e) {
				serverErrorResponse(500, "Server Error", e.getMessage());
			} finally {
				if(fin != null) {
					try {
						fin.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			serverErrorResponse(404, "File Not Found", "can' not find resouce : " + request.getUri());
		}
	}
	
	
	public void serverErrorResponse(int statusCode, String statusMessage, String errMessage) {
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n")
			.append("ContentType: text/html \r\n")
			.append("ContentLength: " + errMessage.length() + "\r\n")
			.append("\r\n")
			.append(errMessage);
		try {
			out.write(sb.toString().getBytes());
			out.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}
