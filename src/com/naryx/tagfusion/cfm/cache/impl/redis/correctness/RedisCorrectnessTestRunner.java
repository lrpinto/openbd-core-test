package com.naryx.tagfusion.cfm.cache.impl.redis.correctness;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;


/**
 * Runs correctness tests for the RedisCacheImpl.
 * 
 * @author Luisa
 * @author CodeArcs, Inc.
 *
 */
public class RedisCorrectnessTestRunner {

	public static void main( String[] args ) {
		runTestCase( RedisCacheImplTest.class );
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

