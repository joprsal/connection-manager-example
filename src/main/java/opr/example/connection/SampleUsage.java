package opr.example.connection;

import static opr.example.connection.factory.ExampleConnectionFactories.DERBY_1;
import static opr.example.connection.factory.ExampleConnectionFactories.MYSQL;

import java.sql.Connection;


public class SampleUsage {
	
	private static ConnectionManagerFactory CONN_MGR_FACTORY = new ConnectionManagerFactory();

	
	public static void main(String[] args) throws Exception {
		ConnectionManager connManager = CONN_MGR_FACTORY.createWithPoolingAndFailover(10, MYSQL, DERBY_1);
		try (Connection connection = connManager.getConnection()) {
			//use connection
		}
	}

}
