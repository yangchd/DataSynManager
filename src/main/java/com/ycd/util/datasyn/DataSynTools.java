package com.ycd.util.datasyn;

import com.ycd.springboot.util.db.DBPool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangchd on 2017/7/24.
 * 工具类
 */

@Service
public class DataSynTools {

    //这个工程移动到其他工程时，请重写这个方法，获取正确的连接
    public Connection getConnection() {

        DBPool pool = new DBPool();
        Connection con = null;
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

    //输出信息
    public static void Logger(String msg){
        System.out.println(msg);
    }

    public String getWebPath(HttpServletRequest request){
        return request.getServletContext().getRealPath("/");
    }

    public Map<String,Object> getReMap(String retflag,String msg,Object data){
        Map<String,Object> rtMap = new HashMap<>();
        rtMap.put("retflag",retflag);
        rtMap.put("msg",msg);
        if(data!=null)rtMap.put("data",data);
        return rtMap;
    }
}
