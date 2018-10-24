package com.naryx.tagfusion.cfm.cache.impl.redis.performance;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;


/**
 * Runs all tests under {@tests.performance}.
 * 
 * @author Luisa
 *
 */
public class RedisPerformanceTestRunner {

	public static void main( String[] args ) {
		runTestCase( RedisCacheImplTest100.class );
		runTestCase( RedisCacheImplTest1k.class );
		runTestCase( RedisCacheImplTest10k.class );
		runTestCase( RedisCacheImplTest100k.class );
		runTestCase( RedisCacheImplTest1M.class );
	}


	public static void runTestCase( Class<?> clazz ) {
		System.out.println();
		System.out.println( "Running Test Case: " + clazz.getName() );

		Result result = JUnitCore.runClasses( clazz );

		for ( Failure failure : result.getFailures() ) {
			System.out.println( failure.getTestHeader() + " failed:" + failure.getMessage() );
		}

		boolean success = result.wasSuccessful();
		if ( success ) {
			long runTime = result.getRunTime();
			System.out.println( "all tests succeeded, runtime: " + runTime + "ms" );
		}
	}
}

