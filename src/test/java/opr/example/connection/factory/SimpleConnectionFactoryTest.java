package opr.example.connection.factory;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;


public class SimpleConnectionFactoryTest {

	private SimpleConnectionFactory factory;
	
	
	@Before
	public void init() {
		factory = new SimpleConnectionFactory(
				"jdbc:derby:testdb;create=true",
				new Properties());
	}
	
	
	@Test
	public void should_create_connection() throws SQLException {
		Connection conn = factory.createConnection();
		
		assertThat(conn, is(notNullValue()));
	}

}
