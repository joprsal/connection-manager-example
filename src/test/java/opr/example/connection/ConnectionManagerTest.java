package opr.example.connection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import opr.example.connection.ConnectionManager;
import opr.example.connection.pool.ConnectionPool;


public class ConnectionManagerTest {

	private ConnectionManager connMgr;
	
	@Mock private ConnectionPool connPool;
	@Mock private Connection conn;
	

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(connPool.getConnection()).thenReturn(conn);
		connMgr = new ConnectionManager(connPool);
	}
	
	
	@Test
	public void should_request_connection_from_pool() throws Exception {
		Connection conn = connMgr.getConnection();

		verify(connPool, times(1)).getConnection();
		assertThat(conn, is(conn));
	}
	
}
