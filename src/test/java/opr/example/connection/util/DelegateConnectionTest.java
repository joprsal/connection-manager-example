package opr.example.connection.util;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import opr.example.connection.util.DelegateConnection;

@RunWith(Parameterized.class)
public class DelegateConnectionTest {

	enum DelegateCloseBehaviour {
		DELEGATE_CLOSE,
		NEVER_DELEGATE_CLOSE,
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				//shouldNeverDelegateClose?
				{ DelegateCloseBehaviour.DELEGATE_CLOSE },
				{ DelegateCloseBehaviour.NEVER_DELEGATE_CLOSE },
		});
	}
    
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private final boolean shouldNeverDelegateClose;
	private DelegateConnection conn;

	@Mock private Connection delegate;
	
	
	public DelegateConnectionTest(DelegateCloseBehaviour delegateCloseBehaviour) {
		this.shouldNeverDelegateClose = (delegateCloseBehaviour == DelegateCloseBehaviour.NEVER_DELEGATE_CLOSE);
	}
	
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		conn = new DelegateConnection(delegate, shouldNeverDelegateClose);
	}
	
	
	@Test
	public void should_delegate_most_methods() throws SQLException {
		//test a few important methods, but really all methods except close() should be delegated
		conn.createStatement();
		verify(delegate, times(1)).createStatement();
		
		conn.prepareStatement("");
		verify(delegate, times(1)).prepareStatement("");
		
		conn.getSchema();
		verify(delegate, times(1)).getSchema();
		
		conn.commit();
		verify(delegate, times(1)).commit();
	}


	@Test
	public void should_correctly_delegate_or_not_call_to_close() throws SQLException {
		conn.close();
		verify(delegate, times(shouldNeverDelegateClose ? 0 : 1)).close();
	}
	
	
	@Test
	public void should_delegate_nothing_after_closed() throws SQLException {
		conn.close();
		try {
			conn.createStatement();
		} catch (SQLException ignore) {}
		
		verify(delegate, never()).createStatement();
	}
	
	@Test
	public void should_fail_everything_after_closed() throws SQLException {
		conn.close();

		expectedException.expect(SQLException.class);
		expectedException.expectMessage("closed");
		
		conn.createStatement();
	}

}
