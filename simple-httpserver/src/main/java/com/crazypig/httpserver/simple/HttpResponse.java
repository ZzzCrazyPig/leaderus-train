package com.crazypig.httpserver.simple;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.crazypig.httpserver.utils.ByteUtil;

/**
 * 
 * @author CrazyPig
 * @since 2017-03-02
 *
 */
public class HttpResponse {
	
	public static final long MAX_UPLOAD_FILES_SIZE = 8 * 1024 * 1024; // 8M
	
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
		InputStream in = request.getIn();
		int contentLength = Integer.parseInt(request.getHeaders().get("Content-Length"));
		String boundary = getBoundary();
		byte[] target = ("--" + boundary).getBytes();
		try {
			byte[] buffer = null;
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			int readBytes = 0;
			byte[] tmpBuffer = new byte[8192];
			while(readBytes < contentLength) {
				int len = in.read(tmpBuffer);
				bytesOut.write(tmpBuffer, 0, len);
				readBytes += len;
			}
			buffer = bytesOut.toByteArray();
			
			int position = 0, index = -1;
			final byte[] terminalBytes = new byte[] {13, 10};
			List<String> fileNames = new ArrayList<String>();
			List<int[]> indexes = new ArrayList<int[]>();
			int startIndex = -1, endIndex = -1;
			long fileTotalSize = 0L;
			
			while(position < contentLength) {
				index = ByteUtil.indexOf(buffer, terminalBytes, position);
				String line = new String(buffer, position, index - position);
				position = index + terminalBytes.length;
				if(line.equals("--" + boundary + "--")) { // End Flag : --${boundary}--
					break ;
				}
				if(("--" + boundary).equals(line)) { // boundary Flag : ${boundary}
					continue;
				}
				if(line.startsWith("Content-Disposition") && line.contains("filename")) {
					int fileNameIdx = line.indexOf("filename=");
					String fileName = line.substring(fileNameIdx + "filename=".length());
					fileName = fileName.substring(1, fileName.length() - 1);
					fileNames.add(fileName);
					// read content-type line
					index = ByteUtil.indexOf(buffer, terminalBytes, position);
					line = new String(buffer, position, index - position);
					position = index + terminalBytes.length;
					// read empty line
					position = position + terminalBytes.length;
					startIndex = position;
					// find next boundary
					endIndex = ByteUtil.indexOf(buffer, target, startIndex);
					// calculate upload files total size
					fileTotalSize += (endIndex - startIndex + 1);
					int[] idx = new int[2];
					idx[0] = startIndex;
					idx[1] = endIndex - terminalBytes.length;
					indexes.add(idx);
					position = endIndex;
					continue;
				}
			}
			
			if(fileTotalSize > MAX_UPLOAD_FILES_SIZE) {
				serverOkResponse("Files Too Big, Files's total size is larger than max allow size [ " + (MAX_UPLOAD_FILES_SIZE / 1024 / 1024) + " M ]");
				return ;
			}
			
			// write upload files to special dir
			for(int i = 0, n = fileNames.size(); i < n; i++) {
				String name = fileNames.get(i);
				int[] idx = indexes.get(i);
				int offset = idx[0];
				int len = idx[1] - idx[0];
				FileOutputStream fout = new FileOutputStream(new File(SimpleHttpServer.WEB_ROOT + File.separator + "uploads", name));
				fout.write(buffer, offset, len);
				fout.flush();
				fout.close();
			}
			
			serverOkResponse("OK");
			
		} catch (IOException e) {
			e.printStackTrace();
			serverErrorResponse(500, "Server Error", e.getMessage());
		}
	}
	
	/**
	 * get multipart upload file boundary
	 * @return
	 */
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
