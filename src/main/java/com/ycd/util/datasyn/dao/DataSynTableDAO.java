package com.ycd.util.datasyn.dao;

import com.ycd.util.datasyn.dao.db.DBService;
import com.ycd.util.datasyn.vo.DataSynTableVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yangchd on 2017/8/29.
 * 同步表工具类
 */
public class DataSynTableDAO {

    //插入或更新表数据
    public Map<String,Object> insertOrUpdateByVO(DataSynTableVO tvo) throws Exception {
        DBService dao = new DBService();
        Map<String,Object> rMap = new HashMap<>();

        String tablename = tvo.getTablename()==null?"": tvo.getTablename();
        String fromtables = tvo.getFromtables()==null?"":tvo.getFromtables();
        String relation = tvo.getRelation()==null?"":tvo.getRelation();
        String tablesrelation = tvo.getTablesrelation()==null?"":tvo.getTablesrelation();
        String allcolumn = tvo.getAllcolumn()==null?"":tvo.getAllcolumn();
        String tablekey = tvo.getTablekey()==null?"":tvo.getTablekey();

        String pk_table = tvo.getPk_table();
        if(pk_table==null || "".equals(pk_table)){
            //如果主键为空，做重复性判断
            String tablesql = "select pk_table,tablename,fromtables from syn_table where tablename='"
                    +tablename+"' and fromtables='"+fromtables+"' ";
            List<Map<String,Object>> tlist = dao.execQuery(tablesql,null);
            if(tlist!=null && tlist.size()>0){
                pk_table = tlist.get(0).get("pk_table")==null?"":tlist.get(0).get("pk_table").toString();
            }else{
                pk_table = null;
            }
        }
        StringBuffer sql = new StringBuffer();
        if(pk_table != null && !"".equals(pk_table)) {
            //已存在
            sql.append(" update syn_table set ");
            if(!"".equals(tablename))sql.append(" tablename = '" + tablename + "' ,");
            if(!"".equals(tablekey))sql.append(" tablekey = '" + tablekey + "' ,");
            if(!"".equals(fromtables))sql.append(" fromtables = '" + fromtables + "' ,");
            if(!"".equals(tablesrelation))sql.append(" tablesrelation = '" + tablesrelation + "' ,");
            if(!"".equals(allcolumn))sql.append(" allcolumn = '" + allcolumn + "' ,");
            if(!"".equals(relation))sql.append(" relation = '" + relation + "' ,");
            sql.deleteCharAt(sql.length()-1);
            sql.append(" where pk_table = '" + pk_table + "' ");
            rMap.put("msg", "表信息更新成功");
        }else{
            //不存在
            pk_table = UUID.randomUUID().toString();
            sql.append(" insert into syn_table(pk_table,tablename,tablekey,fromtables,tablesrelation,allcolumn,relation) "
                    +" values('"+pk_table+"','"+tablename+"','"+tablekey+"','"+fromtables
                    +"','"+tablesrelation+"','"+allcolumn+"','"+relation+"') ");
            rMap.put("msg","表信息插入成功");
        }
        tvo.setPk_table(pk_table);
        dao.execUpdate(sql.toString(),null);
        rMap.put("retflag","0");
        return rMap;
    }
}
