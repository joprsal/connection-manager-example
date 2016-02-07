package opr.example.connection.pool;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import opr.example.connection.pool.PooledConnection;
import opr.example.connection.pool.PooledConnectionListener;


public class PooledConnectionTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	private PooledConnection pooledConn;
	
	@Mock private Connection delegateConn;
	@Mock private PooledConnectionListener listener;

	private ArgumentCaptor<PooledConnection> pooledConnArg = ArgumentCaptor.forClass(PooledConnection.class);
	
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		pooledConn = new PooledConnection(delegateConn, listener);
	}
	
	
	@Test
	public void should_notify_listener_on_close() throws SQLException {
		pooledConn.close();
		
		verify(listener, times(1)).onClose(pooledConnArg.capture());
		assertThat(pooledConnArg.getValue(), is(pooledConn));
	}
	
	@Test
	public void should_not_notify_listener_on_other_method_calls() throws SQLException {
		pooledConn.commit();

		verify(listener, never()).onClose(any());
	}

}
