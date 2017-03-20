package com.crazypig.nio.pool;

import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ByteBufferPool {
	
	private TreeMap<Integer, Bucket> buckets;
	
	private final int bufferBaseCapacity; // buffer basic capacity
	private final int bucketSize; // the bucket size in this buffer pool
	private final int bucketCapacity; // capacity of one bucket
	
	private final boolean isDirect; // direct buffer pool [:true] or heap buffer pool [:false]
	
	private long size; // buffer pool total size [bytes]
	
	public ByteBufferPool(int bufferBaseCapacity, int bucketSize, int bucketCapacity) {
		this(false, bufferBaseCapacity, bucketSize, bucketCapacity);
	}
	
	public ByteBufferPool(boolean isDirect, int bufferBaseCapacity, int bucketSize, int bucketCapacity) {
		this.isDirect = isDirect;
		this.bufferBaseCapacity = bufferBaseCapacity;
		this.bucketSize = bucketSize;
		this.bucketCapacity = bucketCapacity;
		buckets = new TreeMap<Integer, Bucket>();
		
		for(int i = 1; i <= bucketSize; i++) {
			size = size + (bufferBaseCapacity * i) * bucketCapacity;
		}
		
	}
	
	public void init() {
		// init bucket
		for(int i = 1; i <= this.bucketSize; i++) {
			Bucket bucket = new Bucket(this.bucketCapacity, this.bufferBaseCapacity * i);
			bucket.init();
			buckets.put(bucket.getBufferCapacity(), bucket);
		}
	}
	
	public long size() {
		return size;
	}
	
	class Bucket {
		
		private final int bucketCapacity;
		private final int bufferCapacity;
		
		private final BlockingQueue<ByteBuffer> buffers;
		
		public Bucket(int bucketCapacity, int bufferCapacity) {
			this.bucketCapacity = bucketCapacity;
			this.bufferCapacity = bufferCapacity;
			this.buffers = new ArrayBlockingQueue<ByteBuffer>(bucketCapacity);
		}
		
		public void init() {
			if(isDirect) {
				for(int i = 0; i < bucketCapacity; i++) {
					buffers.add(ByteBuffer.allocateDirect(bufferCapacity));
				}
			} else {
				for(int i = 0; i < bucketCapacity; i++) {
					buffers.add(ByteBuffer.allocate(bufferCapacity));
				}
			}
		}
		
		public ByteBuffer allocate() {
			return buffers.poll();
		}
		
		public boolean recycle(ByteBuffer buffer) {
			return buffers.add(buffer);
		}
		
		public void destory() {
			if(isDirect) {
				ByteBuffer buffer = null;
				while((buffer = buffers.poll()) != null) {
					// destory direct buffer
					((sun.nio.ch.DirectBuffer)buffer).cleaner().clean();
				}
			} else {
				buffers.clear();
			}
		}

		public int getBucketCapacity() {
			return bucketCapacity;
		}

		public int getBufferCapacity() {
			return bufferCapacity;
		}

		public BlockingQueue<ByteBuffer> getBuffers() {
			return buffers;
		}

	}

	public ByteBuffer allocate(int capacity) {
		Bucket bucket = bucketFor(capacity);
		if(bucket != null) {
			return bucket.allocate();
		}
		return null;
	}
	
	public boolean recycle(ByteBuffer buffer) {
		if(buffer == null) 
			return false;
		if(buckets.containsKey(buffer.capacity())) {
			Bucket bucket = buckets.get(buffer.capacity());
			buffer.clear();
			return bucket.recycle(buffer);
		}
		return false;
	}
	
	public void destory() {
		for(Bucket bucket : buckets.values()) {
			bucket.destory();
		}
		buckets.clear();
	}
	
	/**
	 * locate to the bucket which suit the capacity
	 * @param capacity
	 * @return
	 */
	private Bucket bucketFor(int capacity) {
		Entry<Integer, Bucket> entry = buckets.higherEntry(capacity);
		return entry == null ? null : entry.getValue();
	}

	public boolean isDirect() {
		return isDirect;
	}
	
	public int getBucketSize() {
		return this.bucketSize;
	}
	
	public int getBucketCapacity() {
		return this.bucketCapacity;
	}
	
}
