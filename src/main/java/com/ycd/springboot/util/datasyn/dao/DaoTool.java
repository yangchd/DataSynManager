package com.ycd.springboot.util.datasyn.dao;

import com.ycd.springboot.vo.datasyn.DataSynSourceVO;
import com.ycd.springboot.util.db.DBService;

import java.util.List;
import java.util.Map;

/**
 * Created by yangchd on 2017/8/28.
 * 数据更新Dao工具
 */
public class DaoTool {

    //获取不关闭数据源
    public NotCloseDB getNotCloseDB(DataSynSourceVO dvo) throws Exception {
        NotCloseDB ndao = null;
        if (dvo.getPk_datasource() != null && !"".equals(dvo.getPk_datasource())) {
            DBService dao = new DBService();
            String datasql = "select * from syn_datasource where pk_datasource = '" + dvo.getPk_datasource() + "'";
            List<Map<String, Object>> list = dao.execQuery(datasql, null);
            if (list != null && list.size() > 0) {
                //获取目标数据库DB
                dvo.setDriver(list.get(0).get("driver").toString());
                dvo.setUrl(list.get(0).get("url").toString());
                dvo.setBasename(list.get(0).get("basename").toString());
                dvo.setUsername(list.get(0).get("username").toString());
                dvo.setPassword(list.get(0).get("password").toString());
                ndao = new NotCloseDB(dvo);
            } else {
                throw new Exception("数据源信息配置有误");
            }
        } else {
            throw new Exception("未找到对应的数据源");
        }
        return ndao;
    }


}
