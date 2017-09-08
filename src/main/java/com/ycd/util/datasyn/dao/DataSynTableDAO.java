package com.ycd.util.datasyn.dao;

import com.ycd.util.datasyn.dao.db.DBService;
import com.ycd.util.datasyn.dao.vo.DataSynTableVO;
import net.sf.json.JSONObject;

import java.util.*;

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

        //对同步表配置，进行判断
        if(null == tvo.getTablekey()){
            String keysql = " select tablekey from syn_table where pk_table = '"+tvo.getPk_table()+"' ";
            List<Map<String,Object>> keylist = dao.execQuery(keysql,null);
            if(keylist != null && keylist.size() == 1){
                tvo.setTablekey(keylist.get(0).get("tablekey").toString());
            }else{
                throw new Exception("同步表的主键需要配置正确的匹配列！");
            }
        }
        String[] keys = tvo.getTablekey().split(",");
        JSONObject object = JSONObject.fromObject(tvo.getRelation());
        for(String key:keys){
            if(null == object.get(key) || "".equals(object.get(key)))throw new Exception("同步表的主键需要配置正确的匹配列！");
        }

        StringBuffer sql = new StringBuffer();
        Object[] objs;
        String pk_table = tvo.getPk_table();
        if(pk_table != null && !"".equals(pk_table)) {
            List<Object> plist = new ArrayList<>();
            sql.append(" update syn_table set ");
            if(tvo.getTablename() != null){sql.append(" tablename = ? ,");plist.add(tvo.getTablename());}
            if(tvo.getTablekey() != null){sql.append(" tablekey = ? ,");plist.add(tvo.getTablekey());}
            if(tvo.getFromtables() != null){sql.append(" fromtables = ? ,");plist.add(tvo.getFromtables());}
            if(tvo.getTablesrelation() != null){sql.append(" tablesrelation = ? ,");plist.add(tvo.getTablesrelation());}
            if(tvo.getWherevalue() != null){sql.append(" wherevalue = ? ,");plist.add(tvo.getWherevalue());}
            if(tvo.getAllcolumn() != null){sql.append(" allcolumn = ? ,");plist.add(tvo.getAllcolumn());}
            if(tvo.getAllcolumnfrom() != null){sql.append(" allcolumnfrom = ? ,");plist.add(tvo.getAllcolumnfrom());}
            if(tvo.getRelation() != null){sql.append(" relation = ? ,");plist.add(tvo.getRelation());}
            sql.deleteCharAt(sql.length()-1);
            sql.append(" where pk_table = ? ");
            plist.add(pk_table);
            objs = new Object[plist.size()];
            for(int i=0;i<plist.size();i++){
                objs[i] = plist.get(i);
            }
            dao.execUpdate(sql.toString(),objs);
            rMap.put("msg", "表信息更新成功");
        }else{
            //不存在
            pk_table = UUID.randomUUID().toString();
            sql.append(" insert into syn_table" +
                    "(pk_table,tablename,tablekey,fromtables,tablesrelation,wherevalue,allcolumn,allcolumnfrom,relation) "+
                    " values(?,?,?,?,?,?,?,?,?) ");
            objs = new Object[]{pk_table,tvo.getTablename(),tvo.getTablekey(),tvo.getFromtables(),
                                tvo.getTablesrelation(),tvo.getWherevalue(),tvo.getAllcolumn(),tvo.getAllcolumnfrom(),tvo.getRelation()};
            dao.execUpdate(sql.toString(),objs);
            rMap.put("msg","表信息插入成功");
        }
        tvo.setPk_table(pk_table);
        rMap.put("retflag","0");
        return rMap;
    }
}
