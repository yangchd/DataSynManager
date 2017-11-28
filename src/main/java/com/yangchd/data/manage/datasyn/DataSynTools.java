package com.yangchd.data.manage.datasyn;

import com.yangchd.data.springboot.util.DBPool;
import org.springframework.stereotype.Repository;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yangchd
 * @date 2017/7/24
 * 工具类
 */
@Repository
public class DataSynTools {

    /**
     * /这个工程移动到其他工程时，请重写这个方法，获取正确的连接
     */
    public Connection getConnection() {
        DBPool pool = new DBPool();
        Connection con = null;
        try{
            con = pool.getConnection();
        }catch(Exception e){
            e.printStackTrace();
        }
        return con;
    }

    /**
     * 输出信息
     */
    public static void dataLogger(String msg){
        System.out.println(msg);
    }

    public Map<String,Object> getReMap(String retflag,String msg,Object data){
        Map<String,Object> rtMap = new HashMap<>(4);
        rtMap.put("retflag",retflag);
        rtMap.put("msg",msg);
        if(data!=null){
            rtMap.put("data",data);
        }
        return rtMap;
    }
}
