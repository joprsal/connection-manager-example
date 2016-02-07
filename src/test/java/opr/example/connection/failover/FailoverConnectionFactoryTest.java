package opr.example.connection.failover;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import opr.example.connection.factory.ConnectionFactory;


public class FailoverConnectionFactoryTest {

	@Rule
	public Timeout timeout = new Timeout(10000L, TimeUnit.MILLISECONDS);
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock private ConnectionFactory masterConnFactory;
	@Mock private ConnectionFactory slaveConnFactory;
	
	@Mock private Connection masterConn;
	@Mock private Connection slaveConn;
	
	private FailoverConnectionFactory failoverConnFactory;

	
	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		resetConnectionFactory(masterConnFactory, masterConn);
		resetConnectionFactory(slaveConnFactory, slaveConn);
		failoverConnFactory = new FailoverConnectionFactory(masterConnFactory, slaveConnFactory);
	}


	@Test
	public void should_use_master_by_default() throws Exception {
		createConnectionAndAssert(masterConn);
		verifyDelegationCounts(1, 0);
	}
	
	@Test
	public void should_use_slave_if_master_fails() throws Exception {
		brakeConnectionFactory(masterConnFactory, "Master error");

		createConnectionAndAssert(slaveConn);
		verifyDelegationCounts(1, 1);
	}
	
	@Test
	public void should_fail_if_both_factories_are_broken() throws Exception {
		brakeConnectionFactory(masterConnFactory, "Master error");
		brakeConnectionFactory(slaveConnFactory, "Slave error");

		expectedException.expect(SQLException.class);
		expectedException.expectMessage("Slave error");
		
		failoverConnFactory.createConnection();
	}
	
	@Test
	public void should_keep_using_slave_while_master_unhealthy() throws Exception {
		brakeConnectionFactory(masterConnFactory, "Master error");
		
		createConnectionAndAssert(slaveConn);
		
		failoverConnFactory.attemptMasterRecovery();
		
		createConnectionAndAssert(slaveConn);
	}
	
	@Test
	public void should_switch_back_to_master_when_it_recovers() throws Exception {
		brakeConnectionFactory(masterConnFactory, "Master error");
		
		createConnectionAndAssert(slaveConn);

		resetConnectionFactory(masterConnFactory, masterConn);
		failoverConnFactory.attemptMasterRecovery();

		createConnectionAndAssert(masterConn);
	}

	@Test
	public void should_be_trying_to_recover_asynchronously() throws Exception {
		brakeConnectionFactory(masterConnFactory, "Master error");

		createConnectionAndAssert(slaveConn);

		resetConnectionFactory(masterConnFactory, masterConn);

		createConnectionAndAssert(slaveConn);
		createConnectionAndAssert(slaveConn);
		
		boolean hasMasterRecovered = failoverConnFactory.attemptMasterRecovery();
		assertThat(hasMasterRecovered, is(true));
		
		createConnectionAndAssert(masterConn);
	}

	@Test
	public void should_recover_automatically() throws Exception {
		failoverConnFactory.setRecoveryInterval(10L);
		brakeConnectionFactory(masterConnFactory, "Master error");

		createConnectionAndAssert(slaveConn);

		resetConnectionFactory(masterConnFactory, masterConn);

		Connection conn;
		do {
			Thread.sleep(10);
			conn = failoverConnFactory.createConnection();
		} while (conn != masterConn);
	}

	
	private void createConnectionAndAssert(Connection expectedConn) throws SQLException {
		Connection conn = failoverConnFactory.createConnection();
		assertThat(conn, is(expectedConn));
	}
	
	private void resetConnectionFactory(ConnectionFactory connFactory, Connection conn) throws SQLException {
		//Using reset() is mostly bad practice, indicating that the test is too complex!
		//Our tests, however, cannot be more simple because the FailoverConnectionFactory cannot be created in a
		//state where the master is already broken. Allowing it would unjustifiably complicate the component.
		reset(connFactory);
		when(connFactory.createConnection()).thenReturn(conn);
	}

	private void brakeConnectionFactory(ConnectionFactory connFactory, String errMsg) throws SQLException {
		SQLException sqlException = new SQLException(errMsg);
		reset(connFactory);
		when(connFactory.createConnection()).thenThrow(sqlException);
	}


	private void verifyDelegationCounts(int expectedCallsToMaster, int expectedCallsToSlave) throws SQLException {
		verify(masterConnFactory, times(expectedCallsToMaster)).createConnection();
		verify(slaveConnFactory, times(expectedCallsToSlave)).createConnection();
	}
}
