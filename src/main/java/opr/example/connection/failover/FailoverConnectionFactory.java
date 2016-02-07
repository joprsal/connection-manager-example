package opr.example.connection.failover;

import java.sql.Connection;
import java.sql.SQLException;

import opr.example.connection.factory.ConnectionFactory;

public class FailoverConnectionFactory implements ConnectionFactory {

	private static final SQLException MASTER_ERROR_INDICATOR = new SQLException("Master error");
	
	private final ConnectionFactory masterConnFactory;
	private final ConnectionFactory slaveConnFactory;

	private volatile boolean isMasterHealthy = true;

	private long recoveryIntervalMillis = 60_000L;
	
	
	public FailoverConnectionFactory(
			ConnectionFactory masterConnFactory,
			ConnectionFactory slaveConnFactory) {
		this.masterConnFactory = masterConnFactory;
		this.slaveConnFactory = slaveConnFactory;
	}


	//for test purposes
	void setRecoveryInterval(long recoveryIntervalMillis) {
		this.recoveryIntervalMillis = recoveryIntervalMillis;
	}

	
	@Override
	public Connection createConnection() throws SQLException {
		try {
			return createConnectionToMasterIfHealthy();
		} catch (SQLException e) {
			return slaveConnFactory.createConnection();
		}
	}


	private Connection createConnectionToMasterIfHealthy() throws SQLException {
		blowImmediatelyIfMasterUnhealthy();
		try {
			return masterConnFactory.createConnection();
		} catch (SQLException e) {
			disableMasterAndStartCheckingForRecovery();
			throw e;
		}
	}

	private void disableMasterAndStartCheckingForRecovery() {
		isMasterHealthy = false;
		startRecoveryChecking();
	}

	private void startRecoveryChecking() {
		new Thread(new RecoveryCheckTask()).start();
	}


	private void blowImmediatelyIfMasterUnhealthy() throws SQLException {
		if (!isMasterHealthy) {
			throw MASTER_ERROR_INDICATOR;
		}
	}


	boolean attemptMasterRecovery() {
		try (Connection conn = masterConnFactory.createConnection()) {
			checkValidityOf(conn);
			isMasterHealthy = true;
		} catch (SQLException e) {
			//master is still unhealthy, do nothing
		}
		return isMasterHealthy;
	}

	private void checkValidityOf(Connection conn) throws SQLException {
		if (conn.isValid(CONN_CREATE_TIMEOUT)) {
			throw new SQLException("Connection invalid");
		}
	}

	
	private class RecoveryCheckTask implements Runnable {
		@Override
		public void run() {
			sleepQuietly(recoveryIntervalMillis);
			if (!attemptMasterRecovery()) {
				startRecoveryChecking();
			}
		}
		
		private void sleepQuietly(long delay) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException("Task was interrupted");
			}
		}
	}
}
