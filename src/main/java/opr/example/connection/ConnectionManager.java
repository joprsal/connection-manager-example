package opr.example.connection;

import java.sql.Connection;
import java.sql.SQLException;

import opr.example.connection.pool.ConnectionPool;


public class ConnectionManager {

	private final ConnectionPool connectionPool;
	
	ConnectionManager(ConnectionPool connectionPool) {
		this.connectionPool = connectionPool;
	}


	public Connection getConnection() throws InterruptedException, SQLException {
		return connectionPool.getConnection();
	}

}
