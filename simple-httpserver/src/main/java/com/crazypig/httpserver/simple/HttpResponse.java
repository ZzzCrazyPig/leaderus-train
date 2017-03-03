package com.crazypig.httpserver.simple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class HttpResponse {
	
	private OutputStream out;
	private HttpRequest request;
	
	public HttpResponse(OutputStream out, HttpRequest request) {
		this.out = out;
		this.request = request;
	}
	
	public void returnStaticResource() {
		String uri = request.getUri();
		if(uri == null) {
			serverErrorResponse(404, "File Not Found", "can' not find resouce : " + request.getUri());
			return ;
		}
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
				e.printStackTrace();
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
	
	public void processFileUpload() {
		System.out.println("start processFileUpload");
		InputStream in = request.getIn();
		int contentLength = Integer.parseInt(request.getHeaders().get("Content-Length"));
		String boundary = getBoundary();
		try {
//			byte[] buffer = new byte[8192];
//			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			
//			int offset = 0;
//			int len = -1;
//			while((len = in.read(buffer)) > 0) {
//				bytesOut.write(buffer, 0, len);
//				offset += len;
//				if(offset >= contentLength) {
//					break;
//				}
//			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			int offset = 0;
			FileOutputStream fout = null;
			while(offset < contentLength) {
				
				String line = reader.readLine();
				offset += line.length() + 2;
				if(line.equals("--" + boundary + "--")) { // end flag
					break ;
				}
				if(("--" + boundary).equals(line)) {
					if(fout != null) {
						fout.flush();
						fout.close();
						fout = null;
					}
				} else if(line.startsWith("Content-Disposition") && line.contains("filename")) {
					int index = line.indexOf("filename=");
					String fileName = line.substring(index + "filename=".length());
					fileName = fileName.substring(1, fileName.length() - 1);
					fout = new FileOutputStream(new File(SimpleHttpServer.WEB_ROOT, UUID.randomUUID().toString()));
				}
				if(fout != null) {
					fout.write((line + "\r\n").getBytes());
				}
				
			}
			System.out.println("FileUpload EntityBody: ");
//			FileOutputStream fout = new FileOutputStream(new File("c:/test.out"));
//			fout.write(bytesOut.toByteArray());
//			fout.flush();
//			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("end processFileUpload");
		serverOkResponse("OK");
	}
	
	private String getBoundary() {
		String contentType = request.getHeaders().get("Content-Type");
		int index = contentType.indexOf("boundary");
		return contentType.substring(index + "boundary=".length()).trim();
	}
	
	public void serverOkResponse(String message) {
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 200 OK \r\n")
			.append("Connection: close \r\n")
			.append("ContentType: text/html \r\n")
			.append("ContentLength: " + message.length() + "\r\n")
			.append("\r\n")
			.append(message);
		try {
			out.write(sb.toString().getBytes());
			out.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void serverErrorResponse(int statusCode, String statusMessage, String errMessage) {
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n")
			.append("Connection: close \r\n")
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
