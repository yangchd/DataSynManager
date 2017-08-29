package com.ycd.springboot.util.datasyn.dao;

import com.ycd.springboot.vo.datasyn.DataSynSourceVO;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotCloseDB {

    private Connection con = null;// 连接对象
    private PreparedStatement pstmt = null;// 语句对象
    private ResultSet rs = null;// 结果集对象

    public NotCloseDB(DataSynSourceVO dvo){
        try {
            getConnection(dvo);
        } catch (Exception e) {
            destory();
        }
    }
    //连接销毁
    public void destory(){
        this.close(rs, pstmt, con);
    }

    /**
     * 获得连接对象,但是不释放
     */
    private Connection getConnection(DataSynSourceVO dvo) throws SQLException, ClassNotFoundException {
        Class.forName(dvo.getDriver());
        con = DriverManager.getConnection(dvo.getUrl(),
                dvo.getUsername(), dvo.getPassword());
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
            this.pstmt = this.con.prepareStatement(sql);// 获得预设语句对象
            if (params != null) {
                // 设置参数列表
                for (int i = 0; i < params.length; i++) {
                    // 因为问号参数的索引是从1开始，所以是i+1，将所有值都转为字符串形式，好让setObject成功运行
                    this.pstmt.setObject(i+1, params[i] + "");
                }
            }
            return this.pstmt.executeUpdate(); // 执行更新，并返回影响行数
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * 执行查询
     */
    public List<Map<String, Object>> execQuery(String sql, Object[] params) throws Exception {
        try {

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

            // 获得结果集元数据（元数据就是描述数据的数据，比如把表的列类型列名等作为数据）
            ResultSetMetaData rsmd = rs.getMetaData();

            // 获得列的总数
            int columnCount = rsmd.getColumnCount();

            // 遍历结果集
            List<Map<String, Object>> al = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> rm = new HashMap<>();
                for (int i = 0; i < columnCount; i++) {
                    String columnName = rsmd.getColumnLabel(i + 1).toLowerCase();
                    Object columnValue = rs.getObject(columnName);
                    rm.put(columnName, columnValue);
                }
                // 将每个 hm添加到al中, al相当于是整个表，每个 hm是里面的一条记录
                al.add(rm);
            }
            return al;
        } catch ( Exception e) {
            throw new Exception("数据查询异常:"+e.getMessage()+"\r\nCause by :"+e.getCause()+"\r\n");
        }
    }

}