package opr.example.connection;

import opr.example.connection.factory.ConnectionFactory;
import opr.example.connection.failover.FailoverConnectionFactory;
import opr.example.connection.pool.ConnectionPool;


public class ConnectionManagerFactory {
	
	/**
	 * Creates new instance of {@link ConnectionManager} that will limit number of concurrently open connections.
	 * Normally, all connections will be created using the master factory. While master is down, the manager will
	 * fall back to use the slave connection factory.
	 */
	public ConnectionManager createWithPoolingAndFailover(
			int poolSize,
			ConnectionFactory master,
			ConnectionFactory slave) {
		
		ConnectionFactory failoverFactory = new FailoverConnectionFactory(master, slave);
		ConnectionPool pool = new ConnectionPool(poolSize, failoverFactory);
		return new ConnectionManager(pool);
	}

}
