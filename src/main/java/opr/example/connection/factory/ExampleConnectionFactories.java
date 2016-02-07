package opr.example.connection.factory;

import java.util.Properties;

public class ExampleConnectionFactories {

	public static final ConnectionFactory DERBY_1;
	public static final ConnectionFactory DERBY_2;
	public static final ConnectionFactory MYSQL;
	
	static {
		DERBY_1 = new SimpleConnectionFactory("jdbc:derby:testdb1;create=true", createDefaultProps());
		DERBY_2 = new SimpleConnectionFactory("jdbc:derby:testdb2;create=true", createDefaultProps());
		MYSQL = new SimpleConnectionFactory("jdbc:mysql://myhost:3306/", createDefaultProps());
	}
	
	private static Properties createDefaultProps() {
		Properties connProps = new Properties();
		connProps.put("user", "juraj");
		connProps.put("password", "heslo");
		return connProps;
	}
}
