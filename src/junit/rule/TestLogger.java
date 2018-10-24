package junit.rule;


import org.junit.rules.TestWatcher;
import org.junit.runner.Description;


/**
 * Log the currently running test.
 * 
 * <p>
 * Typical usage:
 * 
 * <p>
 * {@code @Rule public LogTestName logTestName = new LogTestName();}
 *
 * <p>
 * See also:
 * <br>
 * {@link org.junit.Rule}
 * <br>
 * {@link org.junit.rules.TestWatcher}
 */
public class TestLogger extends TestWatcher {


	private long startTime;


	public TestLogger() {
		super();
		startTime = 0;
	}


	@Override
	protected void starting( Description description ) {
		startTime = System.currentTimeMillis();
		System.out.print( "Test { " + description.getMethodName() + "..." );
	}


	@Override
	protected void failed( Throwable e, Description description ) {
		System.out.println( "===> failed }" );
		e.printStackTrace();
	}


	@Override
	protected void succeeded( Description description ) {
		long execTime = System.currentTimeMillis() - startTime;
		System.out.println( " succeeded, executionTime: " + execTime + "ms }" );
	}
}
