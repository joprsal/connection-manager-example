package opr.example.connection.pool;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import opr.example.connection.factory.SimpleConnectionFactory;
import opr.example.connection.util.DelegateConnection;


public class ConnectionPoolTest {

	private static final int MAX_POOL_SIZE = 3;

	private ConnectionPool connMgr;

	@Mock private SimpleConnectionFactory connFactory;
	@Mock private Connection conn1;
	@Mock private Connection conn2;
	@Mock private Connection conn3;
	

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		setupConnectionValidity(conn1);
		setupConnectionValidity(conn2);
		setupConnectionValidity(conn3);
		when(connFactory.createConnection()).thenReturn(conn1, conn2, conn3);
		connMgr = new ConnectionPool(MAX_POOL_SIZE, connFactory);
	}

	private void setupConnectionValidity(Connection conn) throws SQLException {
		when(conn.isValid(anyInt())).thenReturn(true);
	}
	
	
	@Test
	public void should_return_connection_1_from_factory() throws Exception {
		Connection conn = connMgr.getConnection();

		verify(connFactory, times(1)).createConnection();
		assertThat(unwrapDelegateIn(conn), is(conn1));
	}
	
	@Test
	public void should_return_connection_1_and_2_when_used_concurrently() throws Exception {
		Connection actualConn1 = connMgr.getConnection();
		Connection actualConn2 = connMgr.getConnection();

		verify(connFactory, times(2)).createConnection();
		assertThat(unwrapDelegateIn(actualConn1), is(conn1));
		assertThat(unwrapDelegateIn(actualConn2), is(conn2));
	}
	
	@Test
	public void should_reuse_connection_1_when_used_in_sequence() throws Exception {
		Connection actualConn1 = connMgr.getConnection();
		close(actualConn1);
		Connection actualConn2 = connMgr.getConnection();

		verify(connFactory, times(1)).createConnection();
		assertThat(unwrapDelegateIn(actualConn1), is(conn1));
		assertThat(unwrapDelegateIn(actualConn2), is(conn1));
	}
	
	@Test
	public void should_respect_max_pool_size_when_used_concurrently() throws Exception {
		int THREAD_COUNT = 50;
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
		for (int id = 0; id < THREAD_COUNT; id++) {
			launchNewConnectionUserThread(id, latch);
		}
		
		latch.await();
		verify(connFactory, times(MAX_POOL_SIZE)).createConnection();
	}

	private void launchNewConnectionUserThread(int id, CountDownLatch latch) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try (Connection conn = connMgr.getConnection()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ignore) {
					}
				} catch (Exception e2) {
					e2.printStackTrace();
					throw new AssertionError("Unexpected error creating test connection");
				} finally {
					latch.countDown();
				}
			}
		}, "ConnectionUserThread #"+id).start();
	}
	
	@Test
	public void should_replace_pooled_connection_if_closed_by_higher_force() throws Exception {
		Connection pooledConn1 = connMgr.getConnection();
		when(conn1.isClosed()).thenReturn(true); //close underlying connection
		close(pooledConn1);
		
		Connection pooledConn2 = connMgr.getConnection();

		verify(connFactory, times(2)).createConnection();
		assertThat(unwrapDelegateIn(pooledConn2), is(conn2));
	}
	
	@Test
	public void should_replace_pooled_connection_if_invalidated_by_higher_force() throws Exception {
		Connection pooledConn1 = connMgr.getConnection();
		makeInvalid(conn1);
		close(pooledConn1);
		
		Connection pooledConn2 = connMgr.getConnection();

		verify(connFactory, times(2)).createConnection();
		assertThat(unwrapDelegateIn(pooledConn2), is(conn2));
	}
	

	private static void makeInvalid(Connection conn) throws SQLException {
		when(conn.isValid(anyInt())).thenReturn(false);
	}
	
	private static Connection unwrapDelegateIn(Connection delegateConn) {
		return ((DelegateConnection) delegateConn).getDelegate();
	}

	private static void close(AutoCloseable closeableResource) throws AssertionError {
		try {
			closeableResource.close();
		} catch (Exception e) {
			throw new AssertionError("Unexpected error closing mock resource");
		}
	}
	
}
