package com.crazypig.nio.pool;

import java.nio.ByteBuffer;

public class ByteBufferPoolTest {
	
	public static void main(String[] args) {
		
		ByteBufferPool pool = new ByteBufferPool(true, 1024, 10, 1024);
		System.out.println("pool size : " + pool.size() + " bytes");
		pool.init();
		ByteBuffer buffer = pool.allocate(1025);
		System.out.println(buffer.capacity());
		pool.recycle(buffer);
		
		buffer = pool.allocate(10241);
		System.out.println(buffer);
		
		pool.destory();
		System.out.println("after destory");
	}

}
