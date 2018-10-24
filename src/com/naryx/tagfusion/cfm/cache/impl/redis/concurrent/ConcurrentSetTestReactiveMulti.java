package com.naryx.tagfusion.cfm.cache.impl.redis.concurrent;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;

// TODO - Add Reactor dependencies and fill in the sample code
public class ConcurrentSetTestReactiveMulti {

	public static void main(String[] args) throws Exception {

		// Flush the server
		RedisClient redis = RedisClient.create("redis://127.0.0.1:6379");

		RedisAsyncCommands<String, String> async = redis.connect().async();
		async.flushall();

		// Reactive commands for the connection in this cache instance
		RedisReactiveCommands<String, String> reactiveCommands;
		reactiveCommands = redis.connect().reactive();

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

				/*
				 * Sample code 
				 * Flux.just("Ben", "Michael", "Mark"). flatMap(key ->
				 * commands.get(key)). subscribe(value -> System.out.println("Got value: " +
				 * value));
				 */

				
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

				/*
				 * Sample code 
				 * Flux.just("Ben", "Michael", "Mark"). flatMap(key ->
				 * commands.get(key)). subscribe(value -> System.out.println("Got value: " +
				 * value));
				 */

				System.out.println(threadName + " finished");
			}
		}).start();

		c.countDown();

	}

}
