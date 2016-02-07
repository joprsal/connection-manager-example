package opr.example.connection.pool;

import static opr.example.connection.factory.ConnectionFactory.CONN_CREATE_TIMEOUT;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import opr.example.connection.factory.ConnectionFactory;
import opr.example.connection.factory.SimpleConnectionFactory;


/**
 * Bounded pool for database connections.
 * 
 * Actual connections are obtained using the given {@link SimpleConnectionFactory}.
 * 
 * New connections are being created lazily, i.e. only if someone requests it and
 * there is no free connection available. Once created, the connection remains in the
 * pool until a "higher force" makes it closed or invalid. This can for example happen
 * due to a DB error, but is generally independent from how the pool operates. 
 */
public class ConnectionPool {

	private final int maxPoolSize;
	private final ConnectionFactory connectionFactory;
	
	private final AtomicInteger poolSize = new AtomicInteger(0);
	private final BlockingQueue<Connection> freeConnections;
	
	
	public ConnectionPool(int maxPoolSize, ConnectionFactory connectionFactory) {
		this.maxPoolSize = maxPoolSize;
		this.connectionFactory = connectionFactory;
		this.freeConnections = new ArrayBlockingQueue<>(maxPoolSize, true);
	}


	public Connection getConnection() throws InterruptedException, SQLException {
		createConnectionIfNoOneIsFreeButLimitAllows();
		Connection conn = freeConnections.take();
		return wrapConnection(conn);
	}

	private Connection wrapConnection(Connection conn) {
		return new PooledConnection(conn, this::onPooledConnectionClose);
	}

	private void createConnectionIfNoOneIsFreeButLimitAllows() throws SQLException {
		if (freeConnections.isEmpty()) {
			createConnectionIfLimitAllows();
		}
	}

	private void createConnectionIfLimitAllows() throws SQLException {
		while (true) {
			int currentSize = poolSize.get();
			boolean isSaturated = (currentSize >= maxPoolSize);
			if (isSaturated) {
				return;
			} else if (poolSize.compareAndSet(currentSize, currentSize+1)) {
				createFreeConnection();
				return;
			} else {
				//someone just interfered with the pool changing it's size, let's retry
			}
		}
	}

	private void createFreeConnection() throws SQLException {
		Connection conn = connectionFactory.createConnection();
		freeConnections.offer(conn);
	}

	
	private void onPooledConnectionClose(PooledConnection closedConnection) {
		Connection delegate = closedConnection.getDelegate();
		if (isConnectionOperational(delegate)) {
			freeConnections.offer(delegate);
		} else {
			doQuietly(() -> delegate.close());
			poolSize.decrementAndGet();
			doQuietly(() -> createConnectionIfLimitAllows());
		}
	}

	private boolean isConnectionOperational(Connection delegate) {
		try {
			return !delegate.isClosed() && delegate.isValid(CONN_CREATE_TIMEOUT);
		} catch (SQLException e) {
			return false;
		}
	}
	
	private static void doQuietly(SQLOperation sqlOperation) {
		try {
			sqlOperation.run();
		} catch (SQLException ignore) {}
	}
	
	private interface SQLOperation {
		void run() throws SQLException;
	}
}
