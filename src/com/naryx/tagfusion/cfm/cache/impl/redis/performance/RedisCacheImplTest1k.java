package com.naryx.tagfusion.cfm.cache.impl.redis.performance;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.naryx.tagfusion.cfm.cache.impl.RedisCacheImpl;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;

import junit.rule.TestLogger;


@FixMethodOrder( MethodSorters.NAME_ASCENDING )
public class RedisCacheImplTest1k {

	private static final int COUNT = 1000;

	String key = "key-" + ( COUNT + 1 );
	String value = "value-" + ( COUNT + 1 );

	static RedisCacheImpl redisCache = null;
	static String region = null;
	static cfStructData props = null;

	static String server;

	@Rule
	public TestLogger logTestName = new TestLogger();


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// Read the server host URI from system properties into 'server'
		server = System.getProperty( "server", "redis://127.0.0.1:6379" );

		// Setup the Redis server properties
		props = new cfStructData();
		props.setData( "type", "redis" );
		props.setData( "server", server );
		props.setData( "waittimeseconds", 5 );

		// Set an individual region for this test
		region = "testPerformance:redisCacheImpl" + COUNT;

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set the properties
		redisCache.setProperties( region, props );

		PopulateKeysAndValues.init( COUNT, redisCache );

	}


	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		redisCache.shutdown();

		redisCache = null;
		region = null;
		props = null;
	}


	@Test
	public final void test1Set() throws Exception {
		redisCache.set( key, new cfStringData( value ), -1, -1 );
	}


	@Test
	public final void test2GetAllIds() throws Exception {
		cfArrayData ids = redisCache.getAllIds();
		assertTrue( "after: set(key), getAllIds() implies ids.size() == old: ids.sixe()+1",
				ids.size() == COUNT + 1 );
	}


	@Test
	public final void test3GetExistingKey() throws Exception {
		cfData retValue = redisCache.get( key );
		assertTrue( "after: set(existingKey, value), get(existingKey) implies retValue == value",
				value.equals( retValue.getString() ) );
	}


	@Test
	public final void test4DeleteExact() throws Exception {
		redisCache.delete( key, true );
		Thread.sleep( 2 );
	}


	@Test
	public final void test5GetNonExistingKey() throws Exception {
		cfData retValue = redisCache.get( key );
		assertTrue( "after: deleteExact(key), get(key) implies retValue == null",
				retValue == null );
	}


	@Test
	public final void test6DeleteAll() throws Exception {
		redisCache.deleteAll();
		Thread.sleep( 2 );
	}


	@Test
	public final void test7GetAllIds() throws Exception {
		cfArrayData ids = redisCache.getAllIds();
		assertTrue( "after: deleteAll(), getAllIds() implies ids.size() == 0",
				ids.size() == 0 );
	}

}
