package com.crazypig.nio.echoserver.simple;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NioSocketR implements Runnable {
	
	private SelectionKey selKey;
	private SocketChannel socketChannel;
	
	public NioSocketR(SelectionKey selKey) {
		this.selKey = selKey;
		this.socketChannel = (SocketChannel) selKey.channel();
	}

	@Override
	public void run() {
		try {
			read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void read() throws IOException {
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

}
