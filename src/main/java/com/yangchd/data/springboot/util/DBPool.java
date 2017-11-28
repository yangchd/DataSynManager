package com.yangchd.data.springboot.util;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * 连接池工具类，获取数据库连接
 */
public class DBPool implements DataSource {
 
    //重写父类的getConnection()方法，返回数据库连接池中的一个connection对象，
    @Override
    public Connection getConnection() throws SQLException{
		Connection con = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/datasyn?useUnicode=true&characterEncoding=utf-8&useSSL=false&useOldAliasMetadataBehavior=true",
					"ycd","111111");
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return con;
    }

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return null;
	}
}