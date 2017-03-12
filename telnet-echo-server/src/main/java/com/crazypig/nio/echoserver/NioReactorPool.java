package com.crazypig.nio.echoserver;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NioReactorPool {
	
	private NioReactor[] reactors;
	private Lock lock;
	private int nextIndex;
	
	public NioReactorPool(int nReactor) throws IOException {
		
		reactors = new NioReactor[nReactor];
		for(int i = 0; i < nReactor; i++) {
			reactors[i] = new NioReactor();
			Thread t = new Thread(reactors[i], "NioReactor_" + i);
			t.start();
		}
		
		lock = new ReentrantLock();
		
	}
	
	public NioReactor getNextReactor() {
		lock.lock();
		try {
			NioReactor next = reactors[nextIndex];
			nextIndex++;
			if(nextIndex >= reactors.length) {
				nextIndex = 0;
			}
			return next;
		} finally {
			lock.unlock();
		}
	}

}
