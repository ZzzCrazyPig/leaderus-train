package com.crazypig.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import com.crazypig.nio.pool.ByteBufferPool;

public class NioReactorServer extends Thread {

	final Selector selector;
	final ServerSocketChannel serverSocketChannel;
	final ByteBufferPool bufferPool;
	
	public static void main(String[] args) {
		
		NioReactorServer server;
		try {
			server = new NioReactorServer(9000);
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public NioReactorServer(int bindPort) throws IOException {
		selector = Selector.open();
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		InetSocketAddress address = new InetSocketAddress(bindPort);
		serverSocketChannel.socket().bind(address);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("started at "+address);
		
		// init direct buffer pool
		bufferPool = new ByteBufferPool(true, 1024, 10, 1000);
		bufferPool.init();
	}
	
	public void run() {
		
		while (true) {
			Set<SelectionKey> selectedKeys = null;
			try {
				selector.select();
				selectedKeys = selector.selectedKeys();

			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			for (SelectionKey selectedKey : selectedKeys) {
				if (selectedKey.isAcceptable()) {
					new NIOAcceptor().run();
				} else {
					((IOHandler) selectedKey.attachment()).run();
				}
			}
			selectedKeys.clear();

		}
	}

	class NIOAcceptor {

		public void run() {
			try {
				SocketChannel socketChannel = serverSocketChannel.accept();
				new IOHandler(selector,socketChannel, bufferPool);
				System.out.println("Connection Accepted by Reactor " + Thread.currentThread().getName());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
}
