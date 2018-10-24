package com.naryx.tagfusion.cfm.cache.impl.redis.correctness;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.naryx.tagfusion.cfm.cache.impl.RedisCacheImpl;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

import io.lettuce.core.RedisClient;
import junit.rule.TestLogger;


/**
 * Tests the RedisCacheImpl class for correctness in a non-deterministic way.
 * 
 * All tests are independent and use distinct String regions from each other.
 * 
 * The test runs on localhost by default (redis://127.0.0.1:6379), where a Redis server instance should have been previously started.
 * 
 * To run on a different server define the server URI as system property '-Dserver=redis//<host>:<port>'.
 * 
 * 
 * @author Luisa Pinto
 * @author CodeArcs, Inc.
 *
 */
public class RedisCacheImplTest {

	RedisCacheImpl redisCache = null;
	cfStructData props = null;
	static String server;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Rule
	public TestLogger logTestName = new TestLogger();


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// Read the server host URI from system properties into 'server'
		server = System.getProperty( "server", "redis://127.0.0.1:6379" );

		// Flush the server
		RedisClient redis = RedisClient.create( server );
		redis.connect().async().flushall();

	}


	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		// Flush the server
		RedisClient redis = RedisClient.create( server );
		redis.connect().async().flushall();

	}


	@Before
	public void setUp() throws Exception {

		// Read the server host URI from system properties into 'server'
		server = System.getProperty( "server", "redis://127.0.0.1:6379" );

		// Setup the Redis server properties
		props = new cfStructData();
		props.setData( "type", "redis" );
		props.setData( "server", server );
		props.setData( "waittimeseconds", 5 );

	}


	@After
	public void tearDown() throws Exception {
		if ( redisCache != null ) {
			redisCache.deleteAll();
			cfArrayData allIDs = redisCache.getAllIds();
			assertTrue( allIDs == null || allIDs.size() == 0 );
			redisCache.shutdown();
		}

		redisCache = null;
		props = null;
	}


	@Test
	public final void testDeleteAll() throws Exception {

		// Set an individual String region for this test
		String region = "testDeleteAll";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Set a cache entry
		redisCache.set( "foo", new cfStringData( "bar" ), -1, -1 );

		// Set another cache entry
		redisCache.set( "bar", new cfStringData( "foo" ), -1, -1 );

		// Delete all entries
		redisCache.deleteAll();

		// Get the cache entry for 'foo'
		cfData cfDataEntryFoo = redisCache.get( "foo" );
		String entryFoo = cfDataEntryFoo == null ? null : cfDataEntryFoo.getString();

		// Get the cache entry for 'bar'
		cfData cfDataEntryBar = redisCache.get( "bar" );
		String entryBar = cfDataEntryBar == null ? null : cfDataEntryBar.getString();

		assertTrue( entryFoo == null );
		assertTrue( entryBar == null );
	}


	@Test
	public final void testDeleteAllDoesNotMixRegions() throws Exception {
		// Set an individual String region for this test
		String region = "testDeleteAllOther";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Set a cache entry
		redisCache.set( "foo:other", new cfStringData( "bar:other" ), -1, -1 );

		// Set another cache entry
		redisCache.set( "bar:other", new cfStringData( "foo:other" ), -1, -1 );

		// Set an individual String region for this test
		String region1 = "testDeleteAll";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region1, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region1, props );

		// Set a cache entry
		redisCache.set( "foo", new cfStringData( "bar" ), -1, -1 );

		// Set another cache entry
		redisCache.set( "bar", new cfStringData( "foo" ), -1, -1 );

		// Delete all entries
		redisCache.deleteAll();
		Thread.sleep( 2 );

		// Get the cache entry for 'foo'
		cfData cfDataEntryFoo = redisCache.get( "foo" );
		String entryFoo = cfDataEntryFoo == null ? null : cfDataEntryFoo.getString();

		// Get the cache entry for 'bar'
		cfData cfDataEntryBar = redisCache.get( "bar" );
		String entryBar = cfDataEntryBar == null ? null : cfDataEntryBar.getString();

		assertTrue( entryFoo == null );
		assertTrue( entryBar == null );

		// Go back to the other String region

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Get the cache entry for 'foo:other'
		cfData cfDataEntryFooOther = redisCache.get( "foo:other" );
		String entryFooOther = cfDataEntryFooOther == null ? null : cfDataEntryFooOther.getString();

		// Get the cache entry for 'bar:other'
		cfData cfDataEntryBarOther = redisCache.get( "bar:other" );
		String entryBarOther = cfDataEntryBarOther == null ? null : cfDataEntryBarOther.getString();

		assertTrue( "bar:other".equals( entryFooOther ) );
		assertTrue( "foo:other".equals( entryBarOther ) );
	}


	@Test
	public final void testDeleteExact() throws Exception {
		// Set an individual String region for this test
		String region = "testDeleteExact";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Set a cache entry
		redisCache.set( "foo", new cfStringData( "bar" ), -1, -1 );

		// Set another cache entry
		redisCache.set( "bar", new cfStringData( "foo" ), -1, -1 );

		// Delete 'foo'
		redisCache.delete( "foo", true );

		// Get the cache entry for 'foo'
		cfData cfDataEntryFoo = redisCache.get( "foo" );
		String entryFoo = cfDataEntryFoo == null ? null : cfDataEntryFoo.getString();

		// Get the cache entry for 'bar'
		cfData cfDataEntryBar = redisCache.get( "bar" );
		String entryBar = cfDataEntryBar == null ? null : cfDataEntryBar.getString();

		assertTrue( entryFoo == null );
		assertTrue( "foo".equals( entryBar ) );

	}


	@Test
	public final void testDeleteExactDoesNotMixRegions() throws Exception {
		// Set individual String regions for this test
		String region = "testDeleteExactDoesNotMixRegions";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Set a cache entry
		redisCache.set( "foo", new cfStringData( "bar" ), -1, -1 );

		// Set another cache entry
		redisCache.set( "bar", new cfStringData( "foo" ), -1, -1 );

		// Delete 'bar'
		redisCache.delete( "bar", true );
		Thread.sleep( 2 );

		// Set another individual String regions for this test
		String region1 = "testDeleteExactDoesNotMixRegionsOther";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region1, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region1, props );

		// Set a cache entry
		redisCache.set( "foo", new cfStringData( "bar" ), -1, -1 );

		// Set another cache entry
		redisCache.set( "bar", new cfStringData( "foo" ), -1, -1 );

		// Delete 'foo'
		redisCache.delete( "foo", true );
		Thread.sleep( 2 );

		// Go back to other region

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Get the cache entry for 'foo'
		cfData cfDataEntryFoo = redisCache.get( "foo" );
		String entryFoo = cfDataEntryFoo == null ? null : cfDataEntryFoo.getString();

		// Get the cache entry for 'bar'
		cfData cfDataEntryBar = redisCache.get( "bar" );
		String entryBar = cfDataEntryBar == null ? null : cfDataEntryBar.getString();

		assertTrue( entryBar == null );
		assertTrue( "bar".equals( entryFoo ) );

		// Go back to the other region

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region1, props.getData( "server" ).getString() );

		// Get the cache entry for 'foo'
		cfData cfDataEntryFooOther = redisCache.get( "foo" );
		String entryFooOther = cfDataEntryFooOther == null ? null : cfDataEntryFooOther.getString();

		// Get the cache entry for 'bar'
		cfData cfDataEntryBarOther = redisCache.get( "bar" );
		String entryBarOther = cfDataEntryBarOther == null ? null : cfDataEntryBarOther.getString();

		assertTrue( entryFooOther == null );
		assertTrue( "foo".equals( entryBarOther ) );

		// Delete all keys
		redisCache.deleteAll();
		Thread.sleep( 2 );

		// Go back to the other region

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );
	}


	@Test
	public final void testDeleteNotExactDoesNotMixRegions() throws Exception {
		// Set an individual String region for this test
		String region = "testDeleteNotExact";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Set a cache entry
		redisCache.set( "foo:bar", new cfStringData( "bar" ), -1, -1 );

		// Set another cache entry
		redisCache.set( "foo:code", new cfStringData( "code" ), -1, -1 );

		// Delete keys starting with 'foo'
		redisCache.delete( "foo", false );
		Thread.sleep( 2 );

		// Get the cache entry for 'foo:bar'
		cfData cfDataEntryFooBar = redisCache.get( "foo:bar" );
		String entryFooBar = cfDataEntryFooBar == null ? null : cfDataEntryFooBar.getString();

		// Get the cache entry for 'foo:code'
		cfData cfDataEntryFoodCode = redisCache.get( "foo:code" );
		String entryFoodCode = cfDataEntryFoodCode == null ? null : cfDataEntryFoodCode.getString();

		assertTrue( entryFooBar == null );
		assertTrue( entryFoodCode == null );

		// Set an individual String region for this test
		String region1 = "testDeleteNotExactOther";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region1, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region1, props );

		// Set a cache entry
		redisCache.set( "foo:bar", new cfStringData( "bar" ), -1, -1 );

		// Set another cache entry
		redisCache.set( "foo:code", new cfStringData( "code" ), -1, -1 );

		// Get the cache entry for 'foo:bar'
		cfData cfDataEntryFooBarOther = redisCache.get( "foo:bar" );
		String entryFooBarOther = cfDataEntryFooBarOther == null ? null : cfDataEntryFooBarOther.getString();

		// Get the cache entry for 'foo:code'
		cfData cfDataEntryFoodCodeOther = redisCache.get( "foo:code" );
		String entryFoodCodeOther = cfDataEntryFoodCodeOther == null ? null : cfDataEntryFoodCodeOther.getString();

		assertTrue( "bar".equals( entryFooBarOther ) );
		assertTrue( "code".equals( entryFoodCodeOther ) );

	}


	@Test
	public final void testDeleteNotExact() throws Exception {
		// Set an individual String region for this test
		String region = "testDeleteNotExact";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Set a cache entry
		redisCache.set( "foo:bar", new cfStringData( "bar" ), -1, -1 );

		// Set another cache entry
		redisCache.set( "foo:code", new cfStringData( "code" ), -1, -1 );

		// Set a cache entry
		redisCache.set( "code:foo", new cfStringData( "foo" ), -1, -1 );

		// Set another cache entry
		redisCache.set( "code:foo:bar", new cfStringData( "foo:bar" ), -1, -1 );

		// Delete keys starting with 'foo'
		redisCache.delete( "foo", false );

		// Get the cache entry for 'foo:bar'
		cfData cfDataEntryFooBar = redisCache.get( "foo:bar" );
		String entryFooBar = cfDataEntryFooBar == null ? null : cfDataEntryFooBar.getString();

		// Get the cache entry for 'foo:code'
		cfData cfDataEntryFoodCode = redisCache.get( "foo:code" );
		String entryFoodCode = cfDataEntryFoodCode == null ? null : cfDataEntryFoodCode.getString();

		// Get the cache entry for 'foo:code'
		cfData cfDataEntryCodeFoo = redisCache.get( "code:foo" );
		String entryCodeFoo = cfDataEntryCodeFoo == null ? null : cfDataEntryCodeFoo.getString();

		// Get the cache entry for 'foo:code'
		cfData cfDataEntryCodeFooBar = redisCache.get( "code:foo:bar" );
		String entryCodeFooBar = cfDataEntryCodeFooBar == null ? null : cfDataEntryCodeFooBar.getString();

		assertTrue( entryFooBar == null );
		assertTrue( entryFoodCode == null );
		assertTrue( "foo".equals( entryCodeFoo ) );
		assertTrue( "foo:bar".equals( entryCodeFooBar ) );

	}


	@Test
	public final void testGet() throws Exception {
		// Set an individual String region for this test
		String region = "testGet";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Set a cache entry
		redisCache.set( "foo", new cfStringData( "bar" ), -1, -1 );

		// Get the cache entry
		cfData cfDataEntry = redisCache.get( "foo" );
		String entry = cfDataEntry == null ? null : cfDataEntry.getString();

		assertTrue( "bar".equals( entry ) );

		// Get the cache entry, for non-existing entry
		cfDataEntry = redisCache.get( "bar" );
		entry = cfDataEntry == null ? null : cfDataEntry.getString();

		assertTrue( entry == null );
	}


	@Test
	public final void testGetAllIds() throws Exception {
		// Set String region for this test
		String region = "testGetAllIds";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Set a cache entry
		redisCache.set( "foo", new cfStringData( "bar" ), -1, -1 );

		// Set another cache entry
		redisCache.set( "bar", new cfStringData( "foo" ), -1, -1 );

		// Get all IDs
		cfArrayData ids = redisCache.getAllIds();

		// Check that 'foo' and 'bar' are in the cfArrayData
		String idsStr = ids.createList( "," );
		assertTrue( idsStr.contains( "foo" ) );
		assertTrue( idsStr.contains( "bar" ) );

		String[] idsArray = idsStr.split( "," );
		assertTrue( idsArray.length == 2 );

	}


	@Test
	public final void testGetInstance() throws dataNotSupportedException {
		// Set String region for this test
		String region = "testGetInstance" + Thread.currentThread().getId();

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Check that it is not null
		assertTrue( redisCache != null );
	}


	@Test
	public final void testGetName() throws Exception {
		// Set an individual String region for this test
		String region = "testGetName";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set the properties
		redisCache.setProperties( region, props );

		// Get the name
		String name = redisCache.getName();

		assertTrue( "redis".equals( name ) );
	}


	@Test
	public final void testGetProperties() throws Exception {
		// Set an individual String region for this test
		String region = "testGetProperties";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Change the properties
		props.setData( "type", "redis" );
		props.setData( "server", server );
		props.setData( "waittimeseconds", 10 );

		// Set the properties
		redisCache.setProperties( region, props );

		// Get the properties
		cfStructData props1 = redisCache.getProperties();

		// Assert properties
		assertTrue( props.getData( "type" ).getString().equals( props1.getData( "type" ).getString() ) );
		assertTrue( props.getData( "server" ).getString().equals( props1.getData( "server" ).getString() ) );
		assertTrue( props.getData( "waittimeseconds" ).getString().equals( props1.getData( "waittimeseconds" ).getString() ) );

	}


	@Test
	public final void testGetStats() throws Exception {
		// Set an individual String region for this test
		String region = "testGetStats";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Get stats
		cfStructData stats = redisCache.getStats();

		// Check stats
		assertTrue( stats != null );
		assertTrue( stats.containsKey( "total_connections_received" ) );
		assertTrue( stats.containsKey( "total_commands_processed" ) );
		assertTrue( stats.containsKey( "instantaneous_ops_per_sec" ) );
		assertTrue( stats.containsKey( "total_net_input_bytes" ) );
		assertTrue( stats.containsKey( "total_net_output_bytes" ) );
		assertTrue( stats.containsKey( "instantaneous_input_kbps" ) );
		assertTrue( stats.containsKey( "instantaneous_output_kbps" ) );
		assertTrue( stats.containsKey( "rejected_connections" ) );
		assertTrue( stats.containsKey( "sync_full" ) );
		assertTrue( stats.containsKey( "sync_partial_ok" ) );
		assertTrue( stats.containsKey( "sync_partial_err" ) );
		assertTrue( stats.containsKey( "expired_keys" ) );
		assertTrue( stats.containsKey( "evicted_keys" ) );
		assertTrue( stats.containsKey( "keyspace_hits" ) );
		assertTrue( stats.containsKey( "keyspace_misses" ) );
		assertTrue( stats.containsKey( "pubsub_channels" ) );
		assertTrue( stats.containsKey( "pubsub_patterns" ) );
		assertTrue( stats.containsKey( "latest_fork_usec" ) );
		assertTrue( stats.containsKey( "migrate_cached_sockets" ) );

	}


	@Test
	public final void testSet() throws Exception {
		// Set an individual String region for this test
		String region = "testSet";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Set a cache entry
		redisCache.set( "foo", new cfStringData( "bar" ), -1, -1 );

		// Get the cache entry
		cfData cfDataEntry = redisCache.get( "foo" );
		String entry = cfDataEntry == null ? null : cfDataEntry.getString();

		assertTrue( "bar".equals( entry ) );

		// Set a cache entry with TTL of 10 seconds
		redisCache.set( "fox", new cfStringData( "bear" ), 6000, -1 );

		// Put this thread to sleep for ten seconds, giving time for it to be deleted by the scheduler
		Thread.sleep( 12000 );

		// Get the cache entry
		cfDataEntry = redisCache.get( "fox" );
		entry = cfDataEntry == null ? null : cfDataEntry.getString();

		assertTrue( entry == null );

		// Set a cache entry with TTL of 10 seconds
		redisCache.set( "dog", new cfStringData( "cat" ), 2000, -1 );

		/*
		 * Put this thread to sleep for 2 seconds,
		 * NOT giving time for it to be deleted by the scheduler, yet it should not be returned by the get
		 */
		Thread.sleep( 2000 );

		// Get the cache entry
		cfDataEntry = redisCache.get( "dog" );
		entry = cfDataEntry == null ? null : cfDataEntry.getString();

		assertTrue( entry == null );
	}


	@Test
	public final void testSetDoesNotMixRegions() throws Exception {
		// Set an individual String region for this test
		String region = "testSetDoesNotMixRegions";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Set a cache entry
		redisCache.set( "foo", new cfStringData( "bar" ), -1, -1 );

		// Get the cache entry
		cfData cfDataEntry = redisCache.get( "foo" );
		String entry = cfDataEntry == null ? null : cfDataEntry.getString();

		assertTrue( "bar".equals( entry ) );

		// Set another individual String region for this test
		String region1 = "testSetDoesNotMixRegionsOther";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region1, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region1, props );

		// Set a cache entry
		redisCache.set( "bar", new cfStringData( "foo" ), -1, -1 );

		// Get the cache entry
		cfDataEntry = redisCache.get( "bar" );
		entry = cfDataEntry == null ? null : cfDataEntry.getString();

		assertTrue( "foo".equals( entry ) );

		// Get the cache entry
		cfDataEntry = redisCache.get( "foo" );
		entry = cfDataEntry == null ? null : cfDataEntry.getString();

		assertTrue( entry == null );

		// Go back to other region

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Get the cache entry
		cfDataEntry = redisCache.get( "bar" );
		entry = cfDataEntry == null ? null : cfDataEntry.getString();

		assertTrue( entry == null );

		redisCache.deleteAll();
		Thread.sleep( 2 );

		redisCache.shutdown();

		// Go back to other region

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region1, props.getData( "server" ).getString() );

		// Delete all
		redisCache.deleteAll();
		Thread.sleep( 2 );
	}


	@Test
	public final void testSetNullKey() throws Exception {
		// Set an individual String region for this test
		String region = "testSetNullKey";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Set a cache entry
		redisCache.set( null, new cfStringData( "bar" ), -1, -1 );

		// Get the cache entry
		cfData cfDataEntry = redisCache.get( null );
		String entry = cfDataEntry == null ? null : cfDataEntry.getString();

		assertTrue( entry == null );
	}


	@Test
	public final void testSetNullValue() throws Exception {
		// Set an individual String region for this test
		String region = "testSetNullValue";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Set a cache entry
		redisCache.set( "foo", null, -1, -1 );

		// Get the cache entry
		cfData cfDataEntry = redisCache.get( "foo" );
		String entry = cfDataEntry == null ? null : cfDataEntry.getString();

		assertTrue( entry == null );
	}


	@Test
	public final void testSetProperties() throws Exception {
		// Set an individual String region for this test
		String region = "testSetProperties";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set the properties
		redisCache.setProperties( region, props );

	}


	@Test
	public final void testSetPropertiesNullProps() throws Exception {
		// Set an individual String region for this test
		String region = "testSetPropertiesNullProps";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties with null String region
		expectedException.expect( Exception.class );
		expectedException.expectMessage( "'props' can not be null" );
		redisCache.setProperties( region, null );

	}


	@Test
	public final void testSetPropertiesNullRegion() throws Exception {
		// Set an individual String region for this test
		String region = "testSetPropertiesNullRegion";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties with null String region
		expectedException.expect( Exception.class );
		expectedException.expectMessage( "'region' can not be null" );
		redisCache.setProperties( null, props );

	}


	@Test
	public final void testSetPropertiesWithoutServer() throws Exception {
		// Set an individual String region for this test
		String region = "testSetPropertiesWithoutServer";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Remove 'server' from properties
		props.remove( "server" );
		assertTrue( !props.containsKey( "server" ) );

		// Set properties with null String region
		expectedException.expect( Exception.class );
		expectedException.expectMessage( "'server' does not exist. in format: server1:port1 server2:port2" );
		redisCache.setProperties( region, props );

	}


	@Test
	public final void testSetPropertiesWithoutType() throws Exception {
		// Set an individual String region for this test
		String region = "testSetPropertiesWithoutType";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Remove 'type' from properties
		props.remove( "type" );
		assertTrue( !props.containsKey( "type" ) );

		// Set properties without 'type'
		expectedException.expect( Exception.class );
		expectedException.expectMessage( "'type' can not be null" );
		redisCache.setProperties( region, props );


	}


	@Test
	public final void testSetPropertiesWithoutWaittimeseconds() throws Exception {
		// Set an individual String region for this test
		String region = "testSetPropertiesWithoutWaittimeseconds";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Remove 'server' from properties
		props.remove( "waittimeseconds" );
		assertTrue( !props.containsKey( "waittimeseconds" ) );

		// Set properties without 'waittimeseconds'
		redisCache.setProperties( region, props );

	}


	@Test
	public final void testSetPropertiesWithTypeDifferentFromRedis() throws Exception {
		// Set an individual String region for this test
		String region = "testSetPropertiesWithTypeDifferentFromRedis";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Change 'type' to something else
		props.setData( "type", "sometype" );
		assertTrue( !props.getData( "type" ).equals( "redis" ) );

		// Set properties with 'type' different from 'redis'
		expectedException.expect( Exception.class );
		expectedException.expectMessage( "'type' must be 'redis'" );
		redisCache.setProperties( region, props );

	}


	@Test
	public final void testShutdown() throws Exception {
		// Set an individual String region for this test
		String region = "testShutdown";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region, props );

		// Set a cache entry
		redisCache.set( "foo", new cfStringData( "bar" ), -1, -1 );

		// Shut down
		redisCache.shutdown();

		// Get the cache entry
		cfData cfDataEntry = redisCache.get( "foo" );
		String entry = cfDataEntry == null ? null : cfDataEntry.getString();

		assertTrue( entry == null );

		// Recreate instance
		// Set an individual String region for this test
		String region1 = "testShutdown";

		// Get a RedisCache instance
		redisCache = RedisCacheImpl.getInstance( region1, props.getData( "server" ).getString() );

		// Set properties
		redisCache.setProperties( region1, props );

		// Set a cache entry
		redisCache.set( "foo", new cfStringData( "bar" ), -1, -1 );

		// Get the cache entry
		cfDataEntry = redisCache.get( "foo" );
		entry = cfDataEntry == null ? null : cfDataEntry.getString();

		assertTrue( "bar".equals( entry ) );
	}
}
