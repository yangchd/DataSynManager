package com.ycd.util.datasyn.dao;

import com.ycd.util.datasyn.dao.db.DBService;
import com.ycd.util.datasyn.vo.DataSynSourceVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yangchd on 2017/8/29.
 */
public class DataSynSourceDAO {

    public Map<String, Object> insertOrUpdateByVO(DataSynSourceVO dvo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();
        DBService dao = new DBService();

        //先进行重复性校验
        String url = dvo.getUrl();
        String sql = "select * from syn_datasource where url = '"+url+"'";
        List<Map<String,Object>> list = dao.execQuery(sql,null);
        StringBuffer sb = new StringBuffer();
        int re = 0;
        if(list!=null && list.size()>0){
            //已存在做更新操作
            sb.append(" update syn_datasource set ");
            sb.append(" driver = '"+ dvo.getDriver() +"' ,");
            sb.append(" url = '"+ dvo.getUrl() +"' ,");
            sb.append(" basename = '"+ dvo.getBasename() +"' ,");
            sb.append(" username = '"+ dvo.getUsername() +"' ,");
            sb.append(" password = '"+ dvo.getPassword() +"' ");
            sb.append(" where pk_datasource = '"+ list.get(0).get("pk_datasource") +"' ");
            re = dao.execUpdate(sb.toString(),null);
        }else{
            //不存在，做插入操作
            String pk = UUID.randomUUID().toString();
            sb.append(" insert into syn_datasource(pk_datasource,driver,url,basename,username,password) ");
            sb.append(" values('"+pk+"','"+dvo.getDriver()+"','"+dvo.getUrl()+"','"+dvo.getBasename()+"','"
                    +dvo.getUsername()+"','"+dvo.getPassword()+"') ");
            re = dao.execUpdate(sb.toString(),null);
        }
        if(re>0){
            rMap.put("retflag","0");
            rMap.put("msg","数据源保存成功");
        }else{
            rMap.put("retflag","1");
            rMap.put("msg","数据源保存失败");
        }
        return rMap;
    }
}
