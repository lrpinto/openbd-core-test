package com.naryx.tagfusion.cfm.cache.impl.redis.performance;

import java.util.concurrent.ExecutionException;

import com.naryx.tagfusion.cfm.cache.CacheInterface;
import com.naryx.tagfusion.cfm.engine.cfStringData;

import io.lettuce.core.api.async.RedisAsyncCommands;


/**
 * Utility class to populate a Redis instance with a specified number of key and values.
 * 
 * @author Luisa Pinto
 */
public class PopulateKeysAndValues {

	private PopulateKeysAndValues() {};

	/**
	 * Number of entries.
	 */
	public static int COUNT;


	/**
	 * Populate a RedisCacheImpl instance with the given number of entries.
	 * 
	 * @param count
	 *          the given number of entries.
	 * @param redisCache
	 *          the RedisCacheImpl instance to populate.
	 */
	public static void init( int count, CacheInterface redisCache ) {

		COUNT = count;

		for ( int i = 0; i < COUNT; i++ ) {

			String key = "key-" + i;
			String value = "value-" + i;

			redisCache.set( key, new cfStringData( value ), -1, -1 );
		}

	}


	/**
	 * Populate a Redis server instance with the given number of entries.
	 * 
	 * @param count
	 *          the given number of entries.
	 * @param asyncCommands
	 *          an asynchronous and thread-safe Redis API.
	 * @param region
	 *          a name space for all entries.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static void init( int count, RedisAsyncCommands<String, String> asyncCommands, String region ) throws InterruptedException, ExecutionException {

		COUNT = count;

		for ( int i = 0; i < COUNT; i++ ) {

			String key = region + "#?#" + "key-" + i;
			String value = "value-" + i;

			io.lettuce.core.RedisFuture<String> future = asyncCommands.set( key, value );
			future.get();

		}
	}
}
