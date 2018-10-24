package com.naryx.tagfusion.cfm.cache.impl.redis.concurrent;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;

public class ConcurrentSetTestSyncMulti {

	public static void main(String[] args) throws Exception {

		// Flush the server
		RedisClient redis = RedisClient.create( "redis://127.0.0.1:6379");
		RedisCommands<String, String> connection = redis.connect().sync();
		connection.flushall();
		
		
		
		final CountDownLatch c = new CountDownLatch(1);
		new Thread(new Runnable() {
			public void run() {
				try {
					c.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				String threadName = Thread.currentThread().getName();
				System.out.println(threadName + " started, ts=" + Instant.now());
				connection.multi();
				connection.set("a", threadName );
				connection.set("a1", threadName);
				connection.exec();
				System.out.println(threadName + " finished");
				
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				try {
					c.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				String threadName = Thread.currentThread().getName();
				System.out.println(threadName + " started, ts=" + Instant.now());
				connection.multi();
				connection.set("a", threadName );
				connection.set("a1", threadName);
				connection.exec();
				System.out.println(threadName + " finished");
			}
		}).start();

		c.countDown();

	}

}
