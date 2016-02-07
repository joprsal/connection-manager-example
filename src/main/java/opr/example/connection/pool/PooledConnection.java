package opr.example.connection.pool;

import java.sql.Connection;
import java.sql.SQLException;

import opr.example.connection.util.DelegateConnection;


class PooledConnection extends DelegateConnection implements Connection {

	private final PooledConnectionListener listener;


	PooledConnection(Connection delegate, PooledConnectionListener listener) {
		super(delegate, true);
		this.listener = listener;
	}

	
	@Override
	public void close() throws SQLException {
		super.close();
		listener.onClose(this);
	}

}
