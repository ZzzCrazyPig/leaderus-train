package com.crazypig.nio.echoserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioEchoServer {
	
	private static final int DEFAULT_PORT = 9000;
	private static final String DEFAUTL_HOSTNAME = "127.0.0.1";
	private static final int DEFAULT_REACTOR_SIZE = Runtime.getRuntime().availableProcessors();
	
	private int port = DEFAULT_PORT;
	private String hostname = DEFAUTL_HOSTNAME;
	
	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	private int reactorSize = DEFAULT_REACTOR_SIZE;
	private NioReactorPool reactorPool;
	
	public void startup() throws IOException {
		
		reactorPool = new NioReactorPool(reactorSize);
		
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
				}
 			}
		}
		
	}
	
	private void accept(SelectionKey selKey) throws IOException {
		ServerSocketChannel serverChannel = (ServerSocketChannel) selKey.channel();
		SocketChannel socketChannel = serverChannel.accept();
		socketChannel.configureBlocking(false);
		NioReactor reactor = reactorPool.getNextReactor();
		reactor.postRegister(socketChannel);
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
