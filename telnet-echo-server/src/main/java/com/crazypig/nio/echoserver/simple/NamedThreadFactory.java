package com.crazypig.nio.echoserver.simple;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
	
	private String namePrefix;
	private static AtomicInteger number = new AtomicInteger(1);
	
	public NamedThreadFactory(String namePrefix) {
		this.namePrefix = namePrefix;
	}

	public Thread newThread(Runnable r) {
		Thread t = new Thread(r, namePrefix + number.getAndIncrement());
		return t;
	}
	

}
