package com.crazypig.httpserver.simple;

public enum HttpMethod {

	GET("GET"),
	POST("POST");
	
	public String name;
	
	private HttpMethod(String name) {
		this.name = name;
	}
	
}
