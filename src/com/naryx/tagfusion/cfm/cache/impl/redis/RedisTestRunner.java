package com.naryx.tagfusion.cfm.cache.impl.redis;

import com.naryx.tagfusion.cfm.cache.impl.redis.correctness.RedisCorrectnessTestRunner;
import com.naryx.tagfusion.cfm.cache.impl.redis.performance.RedisPerformanceTestRunner;


/**
 * Runs all the tests under this project.
 * 
 * 
 * @author Luisa
 *
 */
public class RedisTestRunner {

	public static void main( String[] args ) {
		System.out.println( "Running Correctness Tests (RedisCacheImpl)" );
		RedisCorrectnessTestRunner.main( null );

		System.out.println();
		System.out.println();
		System.out.println( "Running Performance Tests (RedisCacheImpl )" );
		RedisPerformanceTestRunner.main( null );


		System.exit( 1 );
	}

}
