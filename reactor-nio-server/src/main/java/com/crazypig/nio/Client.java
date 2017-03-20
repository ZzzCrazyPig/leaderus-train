package com.crazypig.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
	
	private SocketChannel socketChannel;
	
	public Client(String host, int port) throws IOException {
	    SocketAddress sad = new InetSocketAddress(host, port);
	    SocketChannel sc = SocketChannel.open();
	    sc.connect(sad);
	    sc.configureBlocking(true);
	    this.socketChannel = sc;
	}
	
	public void testDownload() throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap("download\r\n".getBytes());
		ByteBuffer readBuffer = ByteBuffer.allocate(4096);
		int writed = this.socketChannel.write(buffer);
		System.out.println("writed : " + writed);
		int len = 0;
		while(len != -1) {
			len = socketChannel.read(readBuffer);
			System.out.println("read : " + len);
			readBuffer.rewind();
		}
	}
	
	public void close() throws IOException {
		this.socketChannel.close();
	}
	
	public static void main(String[] args) {
		try {
			Client client = new Client("127.0.0.1", 9000);
			client.testDownload();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
