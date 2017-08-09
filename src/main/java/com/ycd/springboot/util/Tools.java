package com.ycd.springboot.util;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangchd on 2017/7/24.
 * 工具类
 */

@Component
public class Tools {
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
