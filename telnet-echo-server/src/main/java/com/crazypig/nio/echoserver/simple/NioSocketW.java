package com.crazypig.nio.echoserver.simple;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NioSocketW implements Runnable {

	private SelectionKey selKey;
	private SocketChannel socketChannel;
	
	public NioSocketW(SelectionKey selKey) {
		this.selKey = selKey;
		this.socketChannel = (SocketChannel) selKey.channel();
	}
	
	@Override
	public void run() {
		try {
			write();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void write() throws IOException {
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

}
