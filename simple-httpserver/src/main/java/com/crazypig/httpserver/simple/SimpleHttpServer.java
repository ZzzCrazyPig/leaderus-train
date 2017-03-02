package com.crazypig.httpserver.simple;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleHttpServer {

	private static final String DEFAULT_BIND_IP = "127.0.0.1";
	private static final int DEFAULT_PORT = 80;
	private static final int DEFAULT_BACKLOG = 1024;
	
	private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2; 
	
	private String bindIp = DEFAULT_BIND_IP;
	private int port = DEFAULT_PORT;
	private int backlog = DEFAULT_BACKLOG;
	private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
	
	private ExecutorService executor;
	
	// start up http server
	public void startup() throws IOException {
		
		executor = Executors.newFixedThreadPool(threadPoolSize);
		
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port, backlog, InetAddress.getByAddress(bindIp.getBytes()));
			while(true) {
				Socket socket = serverSocket.accept();
				dispatch(socket);
			}
		} catch(IOException e) {
			throw e;
		} finally {
			if(serverSocket != null) {
				serverSocket.close();
			}
		}
		
	}
	
	// one request one thread
	private void dispatch(Socket socket) throws IOException {
		executor.execute(new RequestHandler(socket));
	}

}
