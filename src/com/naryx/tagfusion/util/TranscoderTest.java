package com.naryx.tagfusion.util;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.util.Transcoder;


public class TranscoderTest {

	cfData data;


	@Before
	public void setUp() throws Exception {
		data = new cfStringData( "bar" );
	}


	@After
	public void tearDown() throws Exception {
		data = null;
	}


	@Test
	public final void testToStringFromString() throws cfmRunTimeException {
		String str = Transcoder.toString( data );
		cfData d = (cfData) Transcoder.fromString( str );
		assertTrue( d.equals( data ) );
	}

}
