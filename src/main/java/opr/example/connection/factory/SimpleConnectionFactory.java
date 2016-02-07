package opr.example.connection.factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class SimpleConnectionFactory implements ConnectionFactory {

	private final String connUrl;
	private final Properties connProps;

	public SimpleConnectionFactory(String connUrl, Properties connProps) {
		this.connUrl = connUrl;
		this.connProps = connProps;
	}

	@Override
	public Connection createConnection() throws SQLException {
		return DriverManager.getConnection(connUrl, connProps);
	}
}
