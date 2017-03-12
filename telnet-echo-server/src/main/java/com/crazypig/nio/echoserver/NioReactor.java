package com.crazypig.nio.echoserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioReactor implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NioReactor.class);
	
	private BlockingQueue<SocketChannel> registerQueue;
	
	private Selector selector;
	
	public NioReactor() throws IOException {
		this.registerQueue = new ArrayBlockingQueue<>(1000);
		this.selector = Selector.open();
	}
	
	public void postRegister(SocketChannel socketChannel) {
		registerQueue.offer(socketChannel);
		this.selector.wakeup();
	}


	@Override
	public void run() {
		while(true) {
			try {
				selector.select();
				register();
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while(iter.hasNext()) {
					SelectionKey selKey = iter.next();
					iter.remove();
					if(selKey.isReadable()) {
						LOGGER.info("received read event");
						read(selKey);
					} else if(selKey.isWritable()) {
						LOGGER.info("received write event");
						write(selKey);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void register() throws IOException {
		SocketChannel socketChannel = null;
		if((socketChannel = registerQueue.poll()) != null) {
			LOGGER.info("register one socketChannel");
			socketChannel.register(selector, SelectionKey.OP_READ);
			socketChannel.write(ByteBuffer.wrap("Welcome Leader.us Power Man Java Course ... \r\n".getBytes()));
		}
	}
	
	private void read(SelectionKey selKey) throws IOException {
		SocketChannel socketChannel = (SocketChannel) selKey.channel();
		ByteBuffer readBuffer = ByteBuffer.allocate(100);
		int readBytes = socketChannel.read(readBuffer);
		if(readBytes < 0) {
			socketChannel.close();
			return ;
		}
		// 模拟发送响应报文到客户端
		int writeBufferSize = socketChannel.socket().getSendBufferSize();
		int dataLength = writeBufferSize * 50 + 2;
		ByteBuffer writeBuffer = ByteBuffer.allocate(dataLength);
		for(int i = 0; i < writeBuffer.capacity() - 2; i++) {
			writeBuffer.put((byte) ('a' + i % 25));
		}
		writeBuffer.put("\r\n".getBytes());
		writeBuffer.flip();
		LOGGER.info("send a huge block data " + dataLength);
		int writed = socketChannel.write(writeBuffer);
		LOGGER.info("writed : " + writed);
		if(writeBuffer.hasRemaining()) {
			LOGGER.info("not write finished, remains " + writeBuffer.remaining());
			writeBuffer.compact();
			selKey.attach(writeBuffer);
			selKey.interestOps(selKey.interestOps() | SelectionKey.OP_WRITE);
		} else {
			LOGGER.info("write finished");
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
			LOGGER.info("writed : " + writed);
			if(writeBuffer.hasRemaining()) {
				LOGGER.info("not write finished, remains " + writeBuffer.remaining());
				writeBuffer.compact();
				selKey.attach(writeBuffer);
				selKey.interestOps(selKey.interestOps() | SelectionKey.OP_WRITE);
			} else {
				LOGGER.info("write finished");
				selKey.attach(null);
				selKey.interestOps(selKey.interestOps() & ~SelectionKey.OP_WRITE);
			}
		}
	}
	
}