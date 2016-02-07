package opr.example.connection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import opr.example.connection.factory.ConnectionFactory;
import opr.example.connection.util.DelegateConnection;


public class ConnectionManagerFactoryTest {
	
	private static final int POOL_SIZE = 2;
	
	@Mock private ConnectionFactory masterConnFactory;
	@Mock private ConnectionFactory slaveConnFactory;
	@Mock private Connection masterConn;
	@Mock private Connection slaveConn;
	
	private ConnectionManagerFactory factory = new ConnectionManagerFactory();
	private ConnectionManager connectionMgr;
	

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(masterConnFactory.createConnection()).thenReturn(masterConn);
		when(slaveConnFactory.createConnection()).thenReturn(slaveConn);
		when(masterConn.isValid(anyInt())).thenReturn(true);
		when(slaveConn.isValid(anyInt())).thenReturn(true);
		connectionMgr = factory.createWithPoolingAndFailover(POOL_SIZE, masterConnFactory, slaveConnFactory);
	}
	
	
	@Test
	public void should_use_failover() throws Exception {
		breakMasterConnectionFactory();

		Connection conn = connectionMgr.getConnection();

		assertThat(((DelegateConnection)conn).getDelegate(), is(slaveConn));
	}

	private void breakMasterConnectionFactory() throws SQLException {
		SQLException sqlException = new SQLException("Master error");
		when(masterConnFactory.createConnection()).thenThrow(sqlException);
	}

	
	@Test
	public void should_use_pooling() throws Exception {
		for (int i=0; i < POOL_SIZE + 1; i++) {
			Connection conn = connectionMgr.getConnection();
			conn.close();
		}
		verify(masterConnFactory, times(1)).createConnection();
	}
	
}
