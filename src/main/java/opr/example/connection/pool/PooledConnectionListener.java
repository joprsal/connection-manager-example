package opr.example.connection.pool;


interface PooledConnectionListener {

	void onClose(PooledConnection closedConnection);
}
