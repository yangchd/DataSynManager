package com.ycd.util.datasyn.dao;

import com.ycd.util.datasyn.dao.db.DBService;
import com.ycd.util.datasyn.dao.vo.DataSynTableVO;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yangchd on 2017/8/29.
 * 同步表工具类
 */
public class DataSynTableDAO {

    //查询方法

    //删除方法

    //插入或更新表数据
    public static Map<String,Object> insertOrUpdateByVO(DataSynTableVO tvo) throws Exception {
        DBService dao = new DBService();
        Map<String,Object> rMap = new HashMap<>();

        if(null == tvo.getTablekey()){
            String keysql = " select tablekey from syn_table where pk_table = '"+tvo.getPk_table()+"' ";
            List<Map<String,Object>> keylist = dao.execQuery(keysql,null);
            if(keylist != null && keylist.size() == 1){
                tvo.setTablekey(keylist.get(0).get("tablekey").toString());
            }else{
                throw new Exception("同步信息更新异常！");
            }
        }
        String[] keys = tvo.getTablekey().split(",");
        JSONObject object = JSONObject.fromObject(tvo.getRelation());
        for(String key:keys){
            if(null == object.get(key) || "".equals(object.get(key)))throw new Exception("同步表的主键需要配置正确的匹配列！");
        }
        StringBuffer sql = new StringBuffer();

        //对同步表不进行重复判断，防止出现不同数据库，同步信息相同的问题
        String pk_table = tvo.getPk_table();
        if(pk_table != null && !"".equals(pk_table)) {
            //已存在
            sql.append(" update syn_table set ");
            if(tvo.getTablename() != null)sql.append(" tablename = '" + tvo.getTablename() + "' ,");
            if(tvo.getTablekey() != null)sql.append(" tablekey = '" + tvo.getTablekey() + "' ,");
            if(tvo.getFromtables() != null)sql.append(" fromtables = '" + tvo.getFromtables() + "' ,");
            if(tvo.getTablesrelation() != null)sql.append(" tablesrelation = '" + tvo.getTablesrelation() + "' ,");
            if(tvo.getAllcolumn() != null)sql.append(" allcolumn = '" + tvo.getAllcolumn() + "' ,");
            if(tvo.getAllcolumnfrom() != null)sql.append(" allcolumnfrom = '" + tvo.getAllcolumnfrom() + "' ,");
            if(tvo.getRelation() != null)sql.append(" relation = '" + tvo.getRelation() + "' ,");
            sql.deleteCharAt(sql.length()-1);
            sql.append(" where pk_table = '" + pk_table + "' ");
            rMap.put("msg", "表信息更新成功");
        }else{
            //不存在
            pk_table = UUID.randomUUID().toString();
            sql.append(" insert into syn_table(pk_table,tablename,tablekey,fromtables,tablesrelation,allcolumn,allcolumnfrom,relation) "
                    +" values('"+pk_table+"','"+tvo.getTablename()+"','"+tvo.getTablekey()+"','"+tvo.getFromtables()
                    +"','"+tvo.getTablesrelation()+"','"+tvo.getAllcolumn()+"','"+tvo.getAllcolumnfrom()+"','"+tvo.getRelation()+"') ");
            rMap.put("msg","表信息插入成功");
        }
        tvo.setPk_table(pk_table);
        dao.execUpdate(sql.toString(),null);
        rMap.put("retflag","0");
        return rMap;
    }
}
