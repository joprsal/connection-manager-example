package opr.example.connection.util;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;


public class DelegateConnection implements Connection {

	private final Connection delegate;
	private final boolean shouldNeverDelegateClose;

	private volatile boolean isClosed;

	
	public DelegateConnection(Connection delegate, boolean shouldNeverDelegateClose) {
		this.delegate = delegate;
		this.shouldNeverDelegateClose = shouldNeverDelegateClose;
	}

	
	public Connection getDelegate() {
		return delegate;
	}
	
	
	@Override
	public void close() throws SQLException {
		isClosed = true;
		if (!shouldNeverDelegateClose) {
			delegate.close();
		}
	}
	
	private void blowIfClosed() throws SQLException {
		if (isClosed) {
			throw new SQLException("Connection has been closed");
		}
	}

	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		blowIfClosed();
		return delegate.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		blowIfClosed();
		return delegate.isWrapperFor(iface);
	}

	@Override
	public Statement createStatement() throws SQLException {
		blowIfClosed();
		return delegate.createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		blowIfClosed();
		return delegate.prepareStatement(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		blowIfClosed();
		return delegate.prepareCall(sql);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		blowIfClosed();
		return delegate.nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		blowIfClosed();
		delegate.setAutoCommit(autoCommit);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		blowIfClosed();
		return delegate.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		blowIfClosed();
		delegate.commit();
	}

	@Override
	public void rollback() throws SQLException {
		blowIfClosed();
		delegate.rollback();
	}

	@Override
	public boolean isClosed() throws SQLException {
		blowIfClosed();
		return delegate.isClosed();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		blowIfClosed();
		return delegate.getMetaData();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		blowIfClosed();
		delegate.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		blowIfClosed();
		return delegate.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		blowIfClosed();
		delegate.setCatalog(catalog);
	}

	@Override
	public String getCatalog() throws SQLException {
		blowIfClosed();
		return delegate.getCatalog();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		blowIfClosed();
		delegate.setTransactionIsolation(level);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		blowIfClosed();
		return delegate.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		blowIfClosed();
		return delegate.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		blowIfClosed();
		delegate.clearWarnings();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		blowIfClosed();
		return delegate.createStatement(resultSetType, resultSetConcurrency);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		blowIfClosed();
		return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		blowIfClosed();
		return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		blowIfClosed();
		return delegate.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		blowIfClosed();
		delegate.setTypeMap(map);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		blowIfClosed();
		delegate.setHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		blowIfClosed();
		return delegate.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		blowIfClosed();
		return delegate.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		blowIfClosed();
		return delegate.setSavepoint(name);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		blowIfClosed();
		delegate.rollback(savepoint);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		blowIfClosed();
		delegate.releaseSavepoint(savepoint);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		blowIfClosed();
		return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		blowIfClosed();
		return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		blowIfClosed();
		return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		blowIfClosed();
		return delegate.prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		blowIfClosed();
		return delegate.prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		blowIfClosed();
		return delegate.prepareStatement(sql, columnNames);
	}

	@Override
	public Clob createClob() throws SQLException {
		blowIfClosed();
		return delegate.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		blowIfClosed();
		return delegate.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		blowIfClosed();
		return delegate.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		blowIfClosed();
		return delegate.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		blowIfClosed();
		return delegate.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		try {
			blowIfClosed();
		} catch (SQLException e) {
			throw new SQLClientInfoException(null, e);
		}
		delegate.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		try {
			blowIfClosed();
		} catch (SQLException e) {
			throw new SQLClientInfoException(null, e);
		}
		delegate.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		blowIfClosed();
		return delegate.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		blowIfClosed();
		return delegate.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		blowIfClosed();
		return delegate.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		blowIfClosed();
		return delegate.createStruct(typeName, attributes);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		blowIfClosed();
		delegate.setSchema(schema);
	}

	@Override
	public String getSchema() throws SQLException {
		blowIfClosed();
		return delegate.getSchema();
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		blowIfClosed();
		delegate.abort(executor);
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		blowIfClosed();
		delegate.setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		blowIfClosed();
		return delegate.getNetworkTimeout();
	}

}
