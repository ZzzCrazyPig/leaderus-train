package com.crazypig.nio.echoserver.simple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioEchoServer {
	
	private static final int DEFAULT_PORT = 9000;
	private static final String DEFAUTL_HOSTNAME = "127.0.0.1";
	
	private int port = DEFAULT_PORT;
	private String hostname = DEFAUTL_HOSTNAME;
	
	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	
	public void startup() throws IOException {
		
		selector = Selector.open();
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		InetSocketAddress endpoint = new InetSocketAddress(hostname, port);
		serverSocketChannel.socket().bind(endpoint);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		while(true) {
			selector.select();
			Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
			while(iter.hasNext()) {
				SelectionKey selKey = iter.next();
				iter.remove();
				if(selKey.isAcceptable()) { // 客户端连接事件
					accept(selKey);
				} else if(selKey.isReadable()) { // 读事件
					System.out.println("received read event");
					read(selKey);
				} else if(selKey.isWritable()) { // 写事件
					System.out.println("received write event");
					write(selKey);
				}
 			}
		}
		
	}
	
	private void accept(SelectionKey selKey) throws IOException {
		ServerSocketChannel serverChannel = (ServerSocketChannel) selKey.channel();
		SocketChannel socketChannel = serverChannel.accept();
		socketChannel.configureBlocking(false);
		socketChannel.register(selector, SelectionKey.OP_READ);
		socketChannel.write(ByteBuffer.wrap("Welcome Leader.us Power Man Java Course ... \r\n".getBytes()));
	}
	
	private void read(SelectionKey selKey) throws IOException {
		SocketChannel socketChannel = (SocketChannel) selKey.channel();
		ByteBuffer readBuffer = ByteBuffer.allocate(100);
		int readBytes = socketChannel.read(readBuffer);
		if(readBytes < 0) {
			socketChannel.close();
			return ;
		}
		int writeBufferSize = socketChannel.socket().getSendBufferSize();
		int dataLength = writeBufferSize * 50 + 2;
		ByteBuffer writeBuffer = ByteBuffer.allocate(dataLength);
		for(int i = 0; i < writeBuffer.capacity() - 2; i++) {
			writeBuffer.put((byte) ('a' + i % 25));
		}
		writeBuffer.put("\r\n".getBytes());
		writeBuffer.flip();
		System.out.println("send a huge block data " + dataLength);
		int writed = socketChannel.write(writeBuffer);
		System.out.println("NioSocketR : " + "writed : " + writed);
		if(writeBuffer.hasRemaining()) {
			System.out.println("NioSocketR : " + "not write finished, remains " + writeBuffer.remaining());
			writeBuffer.compact();
			selKey.attach(writeBuffer);
			selKey.interestOps(selKey.interestOps() | SelectionKey.OP_WRITE);
		} else {
			System.out.println("NioSocketR : " + "write finished");
			selKey.attach(null);
			selKey.interestOps(selKey.interestOps() & ~SelectionKey.OP_WRITE);
		}
	}
	
	private void write(SelectionKey selKey) throws IOException {
		SocketChannel socketChannel = (SocketChannel) selKey.channel();
		ByteBuffer writeBuffer = (ByteBuffer) selKey.attachment();
		if(writeBuffer != null) {
			writeBuffer.flip();
			int writed = socketChannel.write(writeBuffer);
			System.out.println("writed : " + writed);
			if(writeBuffer.hasRemaining()) {
				System.out.println("not write finished, remains " + writeBuffer.remaining());
				writeBuffer.compact();
				selKey.attach(writeBuffer);
				selKey.interestOps(selKey.interestOps() | SelectionKey.OP_WRITE);
			} else {
				System.out.println("write finished");
				selKey.attach(null);
				selKey.interestOps(selKey.interestOps() & ~SelectionKey.OP_WRITE);
			}
		}
	}
	
	public static void main(String[] args) {
		
		NioEchoServer server = new NioEchoServer();
		try {
			server.startup();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		while(true) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
