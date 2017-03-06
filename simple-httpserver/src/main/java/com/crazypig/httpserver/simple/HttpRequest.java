package com.crazypig.httpserver.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author CrazyPig
 * @since 2017-03-02
 *
 */
public class HttpRequest {
	
	private static final String DEFAULT_CHARSET = "UTF-8";
	
	private HttpMethod method;
	private String uri;
	private String httpVersion;
	/** store request header **/
	private Map<String, String> headers;
	/** store request parameter **/
	private Map<String, String> params;
	
	/** point to socket inputstream **/
	private final InputStream in;
	
	private boolean endParseHeader;
	
	public HttpRequest(InputStream in) throws IOException {
		this.in = in;
		headers = new LinkedHashMap<String, String>();
		params = new LinkedHashMap<String, String>();
	}
	
	/**
	 * parse http request request line and request header
	 * @throws IOException
	 */
	public void parseRequestLineAndHeaders() throws IOException {
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(in, Charset.forName(DEFAULT_CHARSET)));
		try {
			String line = null;
			String[] parts = null;
			StringBuffer sb = new StringBuffer();
			while((line = reader.readLine()) != null) {
				sb.append(line + "\r\n");
				if(reader.getLineNumber() == 1) {
					// parse request line
					String requestLine = line;
					parts = requestLine.split("\\s+", 3);
					method = HttpMethod.valueOf(parts[0]);
					if(parts[1].contains("?")) {
						int index = parts[1].indexOf("?");
						uri = parts[1].substring(0, index);
						// parse request param
						parseRequestParams(parts[1].substring(index + 1, parts[1].length()));
					} else {
						uri = parts[1];
					}
					httpVersion = parts[2];
				} else {
					if(line.isEmpty()) {
						endParseHeader = true;
						break;
					}
					String headerLine = line;
					parts = headerLine.split(":", 2);
					headers.put(parts[0].trim(), parts[1].trim());
				}
			}
			System.out.println("Parse one HTTP request :");
			System.out.println(sb.toString());
		} catch(IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public boolean isMultiPart() {
		
		if(method != HttpMethod.POST) {
			return false;
		}
		
		String contentType = headers.get("Content-Type");
		if(contentType != null && contentType.startsWith("multipart/form-data")) {
			return true;
		}
		
		return false;
		
	}
	
	public void parseRequestBody() throws IOException {
		
		if(method != HttpMethod.POST) {
			return ;
		}
		
		if(isMultiPart()) {
			return ;
		}
			
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(in, Charset.forName(DEFAULT_CHARSET)));
		try {
		String requestBodyLine = reader.readLine();
			if(requestBodyLine != null) {
				parseRequestParams(requestBodyLine);
			}
		} catch(IOException e) {
			throw e;
		}
	}
	
	/**
	 * parse request parameter
	 * @param paramLine
	 */
	private void parseRequestParams(String paramLine) {
		String[] kvParts = paramLine.split("&");
		for(String kvPart : kvParts) {
			String[] kv = kvPart.split("=");
			params.put(kv[0], kv[1]);
		}
	} 

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getHttpVersion() {
		return httpVersion;
	}

	public void setHttpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public InputStream getIn() {
		return in;
	}

	public boolean isEndParseHeader() {
		return endParseHeader;
	}

	public void setEndParseHeader(boolean endParseHeader) {
		this.endParseHeader = endParseHeader;
	}
	
}
