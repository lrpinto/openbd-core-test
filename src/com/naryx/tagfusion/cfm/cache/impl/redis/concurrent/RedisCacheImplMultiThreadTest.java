package com.naryx.tagfusion.cfm.cache.impl.redis.concurrent;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;

import com.naryx.tagfusion.cfm.cache.impl.RedisCacheImpl;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

import io.lettuce.core.RedisClient;

// TODO - This is too complex for a base test. Ignore for now.
public class RedisCacheImplMultiThreadTest {

	private static boolean isFinished;

	public static void main(String[] args) throws Exception {
		
		if(!isFinished ) {
			throw new Exception ("Not finsihed yet. Ignore for now");
		}

		final CountDownLatch c = new CountDownLatch(1);

		new Thread(new Runnable() {
			public void run() {
				try {
					c.await();

					RedisCacheImpl redisCache = null;
					cfStructData props = null;
					String server;

					// Read the server host URI from system properties into 'server'
					server = System.getProperty("server", "redis://127.0.0.1:6379");

					// Setup the Redis server properties
					props = new cfStructData();
					props.setData("type", "redis");
					props.setData("server", server);
					props.setData("waittimeseconds", 5);

					// Flush the server
					RedisClient redis = RedisClient.create(server);
					redis.connect().async().flushall();

					// Set an individual String region for this test
					String region = "testSet1";

					// Get a RedisCache instance

					redisCache = RedisCacheImpl.getInstance(region, props.getData("server").getString());

					// Set properties

					redisCache.setProperties(region, props);

					// Set a cache entry
					redisCache.set("foo", new cfStringData("bar"), -1, -1);

					// Get the cache entry
					cfData cfDataEntry = redisCache.get("foo");
					String entry = null;

					entry = cfDataEntry == null ? null : cfDataEntry.getString();

					assertTrue("bar".equals(entry));

				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (dataNotSupportedException e) {
					e.printStackTrace();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		}).start();

		c.countDown();

	}

}
