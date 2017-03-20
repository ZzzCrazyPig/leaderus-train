package com.crazypig.nio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.crazypig.nio.pool.ByteBufferPool;

public class IOHandler implements Runnable {
	
	private SocketChannel socketChannel;
	private SelectionKey selectionKey;
	private ByteBuffer writeBuffer;
	private ByteBuffer readBuffer;
	private int lastMessagePos;
	
	private RandomAccessFile raf;
	private FileChannel fileChannel;
	private boolean downloading = false;
	private long bytesTransfered = 0L;
	private final ByteBufferPool bufferPool;
	
	public IOHandler(Selector selector, SocketChannel socketChannel, ByteBufferPool bufferPool) throws IOException {
		socketChannel.configureBlocking(false);
		this.socketChannel = socketChannel;
		this.bufferPool = bufferPool;
		selectionKey = socketChannel.register(selector, 0);
		selectionKey.interestOps(SelectionKey.OP_READ);
		writeBuffer = ByteBuffer.allocateDirect(1024*2);
		readBuffer = ByteBuffer.allocateDirect(10);
		// 绑定会话
		selectionKey.attach(this);
		writeBuffer.put("Welcome Leader.us Power Man Java Course ...\r\nTelnet>".getBytes());
		writeBuffer.flip();
		doWriteData();
	}

	@Override
	public void run() {
		try {
			if (selectionKey.isReadable()) {
				doReadData();
			} else if (selectionKey.isWritable()) {
				if(downloading) {
					doDownloadData();
				} else {
					doWriteData();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			selectionKey.cancel();
			try {
				socketChannel.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private void doWriteData() throws IOException {
		writeToChannel();
	}
	
	private void doReadData() throws IOException {
		socketChannel.read(readBuffer);
		int position = readBuffer.position();
		String readedLine = null;
		for(int i = lastMessagePos; i < position; i++) {
			if(readBuffer.get(i) == 13) { // /n
				byte[] lineBytes = new byte[i - lastMessagePos];
				readBuffer.position(lastMessagePos);
				readBuffer.get(lineBytes);
				lastMessagePos = i;
				readedLine = new String(lineBytes);
				System.out.println("received line ,lenth:" + readedLine.length() + " value: " + readedLine);
				break;
			}
		}
		if(readedLine != null) {
			selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_READ);
			processCommand(readedLine);
		} else if(!readBuffer.hasRemaining()) { // 考虑bytebuffer太小, 应该分配更大的bytebuffer
			ByteBuffer newBuffer = bufferPool.allocate(readBuffer.capacity() * 2);
			System.out.println("allocate larger read buffer , capacity : " + newBuffer.capacity());
			readBuffer.flip();
			newBuffer.put(readBuffer);
			bufferPool.recycle(readBuffer);
			readBuffer = newBuffer;
		}
		if(readBuffer.position() > readBuffer.capacity() / 2) { // 清理前面读过的废弃空间
			System.out.println("rewind read byte buffer ,get more space  " + readBuffer.position());
			readBuffer.limit(readBuffer.position());
			readBuffer.position(lastMessagePos);
			readBuffer.compact();
			lastMessagePos = 0;
		}
	}
	
	private void processCommand(String readedLine) throws IOException {
		if(readedLine.startsWith("dir")) {
			readedLine = "cmd /c " + readedLine;
			String result = LocalCmandUtil.callCmdAndgetResult(readedLine);
			writeBuffer.put(result.getBytes("GBK"));
			writeBuffer.put("\r\nTelent>".getBytes());
		} else if(readedLine.startsWith("download")) {
			File file = new File(System.getProperty("user.dir") + "/download.txt");
			raf = new RandomAccessFile(file, "rw"); 
			if(!file.exists()) {
				System.out.println("file : " + file.getAbsolutePath() + " dosen't exists, create it with size of 100M and fill it");
				// 模拟生成100M的文件, 填充数据
				long fileSize = 100 * 1024 * 1024;
				MappedByteBuffer out = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
				for(long i = 0; i < fileSize - 1; i++) {
					out.put((byte)'x');
				}
				out.put((byte)'z');
			}
			fileChannel = raf.getChannel();
			doDownloadData();
			return ;
		} else {
			for (int i = 0; i < writeBuffer.capacity()-10 ; i++) {
				writeBuffer.put((byte) ('a' + i % 25));
			}
			writeBuffer.put("\r\nTelnet>".getBytes());
		}
		writeBuffer.flip();
		writeToChannel();
	}
	
	private void doDownloadData() throws IOException {
		long size = fileChannel.size();
		long writed = fileChannel.transferTo(bytesTransfered, size - bytesTransfered, socketChannel);
		bytesTransfered += writed;
		if(bytesTransfered < fileChannel.size()) {
			// 还没有传输完成
			System.out.println("remaining " + (fileChannel.size() - bytesTransfered) + " bytes to transfer");
			downloading = true;
			selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
		} else {
			// 文件download完成, 重置相关变量
			downloading = false;
			fileChannel.close();
			raf.close();
			fileChannel = null;
			bytesTransfered = 0L;
			writeBuffer.put("\r\nTelnet>".getBytes());
			writeBuffer.flip();
			writeToChannel();
		}
	}
	
	private void writeToChannel() throws IOException {
		int writed = socketChannel.write(writeBuffer);
		System.out.println("writed " + writed);
		if (writeBuffer.hasRemaining()) {
			writeBuffer.compact();
			writeBuffer.flip();
			System.out.println("writed " + writed + " not write finished  so bind to session ,remains " + writeBuffer.remaining());
			selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
		} else {
			System.out.println(" block write finished ");
			writeBuffer.clear();
			selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE | SelectionKey.OP_READ);
		}
	}
	
}
