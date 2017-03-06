package com.crazypig.httpserver.simple;

/**
 * 
 * @author CrazyPig
 * @since 2017-03-02
 *
 */
public enum HttpMethod {

	GET("GET"),
	POST("POST");
	
	// ...more
	
	public String name;
	
	private HttpMethod(String name) {
		this.name = name;
	}
	
}
