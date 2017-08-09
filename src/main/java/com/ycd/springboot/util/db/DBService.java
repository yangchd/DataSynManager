package com.ycd.springboot.util.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBService {
	Connection con = null;// 连接对象
	PreparedStatement pstmt = null;// 语句对象
	ResultSet rs = null;// 结果集对象

	/**
	 * 获得连接对象
	 */
	public Connection getConnection() {
		DBPool pool = new DBPool();
		try{
			con = pool.getConnection();
		}catch(Exception e1){
			 if(con != null) {
	                try {
	                    con.close();
	                } catch (SQLException e) {
	                    con = null;
	                }
	            }
		}
		return con;
	}

	/**
	 * 关闭 连接对象、语句对象、结果集对象
	 */
	public void close(ResultSet rs, PreparedStatement pstmt, Connection con) {
		if(rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                rs = null;
            }
        }
        if(pstmt != null) {
            try {
            	pstmt.close();
            } catch (SQLException e) {
            	pstmt = null;
            }
        }
        if(con != null) {
        	try {
				con.close();
			} catch (SQLException e) {
				con=null;
			}
        }
	}

	/**
	 * 执行更新
	 */
	public int execUpdate(String sql, Object[] params) throws Exception {
		try {
			this.getConnection();// 获得连接对象
			this.pstmt = this.con.prepareStatement(sql);// 获得预设语句对象
			if (params != null) {
				// 设置参数列表
				for (int i = 0; i < params.length; i++) {
					// 因为问号参数的索引是从1开始，所以是i+1，将所有值都转为字符串形式，好让setObject成功运行
					this.pstmt.setObject(i + 1, params[i] + "");
				}
			}
			return this.pstmt.executeUpdate(); // 执行更新，并返回影响行数
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		} finally {
			this.close(this.rs, this.pstmt, this.con);
		}
	}

	/**
	 * 执行查询
	 */
	public List<Map<String, Object>> execQuery(String sql, Object[] params) throws Exception {
		try {
			this.getConnection();// 获得连接对象
			this.pstmt = this.con.prepareStatement(sql);// 获得预设语句对象
			if (params != null) {
				// 设置参数列表
				for (int i = 0; i < params.length; i++) {
					// 因为问号参数的索引是从1开始，所以是i+1，将所有值都转为字符串形式，好让setObject成功运行
					this.pstmt.setObject(i + 1, params[i] + "");
				}
			}
			// 执行查询
			ResultSet rs = pstmt.executeQuery();

			List<Map<String, Object>> al = new ArrayList<Map<String, Object>>();

			// 获得结果集元数据（元数据就是描述数据的数据，比如把表的列类型列名等作为数据）
			ResultSetMetaData rsmd = rs.getMetaData();

			// 获得列的总数
			int columnCount = rsmd.getColumnCount();

			// 遍历结果集
			while (rs.next()) {
				Map<String, Object> hm = new HashMap<String, Object>();
				for (int i = 0; i < columnCount; i++) {
					// 根据列索引取得每一列的列名,索引从1开始
					String columnName = rsmd.getColumnName(i + 1).toLowerCase();
					// 根据列名获得列值
					Object columnValue = rs.getObject(columnName);
					// 将列名作为key，列值作为值，放入 hm中，每个 hm相当于一条记录
					hm.put(columnName, columnValue);
				}
				// 将每个 hm添加到al中, al相当于是整个表，每个 hm是里面的一条记录
				al.add(hm);
			}
			return al;
		} catch ( Exception e) {
			throw new Exception("数据查询异常:"+e.getMessage()+"\r\nCause by :"+e.getCause()+"\r\n");
		} finally {
			this.close(this.rs, this.pstmt, this.con);
		}
	}

}