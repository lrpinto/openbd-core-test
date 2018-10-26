package com.naryx.tagfusion.cfm.cache.impl.redis.concurrent;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;

import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.engine.cfStructData;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;

public class ConcurrentCreate10Regions {

	public static void main(String[] args) throws Exception {

		// Flush the server
		RedisClient redis = RedisClient.create("redis://127.0.0.1:6379");
		RedisAsyncCommands<String, String> connection = redis.connect().async();
		connection.flushall();

		final CountDownLatch c = new CountDownLatch(1);
		final Integer MAX_REGIONS = 10;

		for (int i = 0; i < MAX_REGIONS; i++) {
			new Thread(new Runnable() {
				public void run() {
					try {
						c.await();

						// Thread starts
						String threadName = Thread.currentThread().getName();
						System.out.println(threadName + " started, ts=" + Instant.now());

						// Name for this treahd's region
						String regionName = "region:" + threadName;

						// Read the server host URI from system properties into 'server'
						String server = System.getProperty("server", "redis://127.0.0.1:6379");

						// Setup the Redis server properties
						cfStructData props = new cfStructData();
						props.setData("type", "redis");
						props.setData("server", server);
						props.setData("waittimeseconds", 5);

						// Create a Redis cache
						CacheFactory.createCacheEngine(regionName, props);

						// Thread ends
						System.out.println(threadName + " finished");

						
					} 
					// If the current thread is interrupted while waiting
					catch (InterruptedException e) {
						e.printStackTrace();
					} catch (Exception e) {
						// If an error occurred when creating a cache region
						e.printStackTrace();
					} 

				}
			}).start();

		}

		c.countDown();

	}

}
