package opr.example.connection.factory;

import java.sql.Connection;
import java.sql.SQLException;


public interface ConnectionFactory {

	public static final int CONN_CREATE_TIMEOUT = 30; //in seconds
	
	Connection createConnection() throws SQLException;
}
