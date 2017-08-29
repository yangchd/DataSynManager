package com.ycd.springboot.service.datasyn.impl;

import com.ycd.springboot.util.datasyn.*;
import com.ycd.springboot.util.datasyn.dao.NotCloseDB;
import com.ycd.springboot.vo.datasyn.DataSynSourceVO;
import com.ycd.springboot.util.db.DBService;
import com.ycd.springboot.vo.datasyn.DataSynTableVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Created by yangchd on 2017/8/1.
 * 数据同步工具类
 */
public class DataSynTool {

    //获取数据源
    public DataSynDB getDataSynDB(DataSynSourceVO dvo) throws Exception {
        DataSynDB sourcedao = null;
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
                sourcedao = new DataSynDB(dvo);
            } else {
                throw new Exception("数据源信息配置有误");
            }
        } else {
            throw new Exception("未找到对应的数据源");
        }
        return sourcedao;
    }

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

                //限制几个测试库
//                if(dvo.getUrl().indexOf("10.4.102.22") > 0){
//                    throw new Exception("10.4.102.22这个地址下的数据库禁止进行同步测试！");
//                }

                ndao = new NotCloseDB(dvo);
            } else {
                throw new Exception("数据源信息配置有误");
            }
        } else {
            throw new Exception("未找到对应的数据源");
        }
        return ndao;
    }

    //根据数据源和表名称获取表的所有列
    public List<Map<String, Object>> getAllColumn(DataSynSourceVO dvo, String tablename) throws Exception {
        DataSynDB dao = new DataSynTool().getDataSynDB(dvo);
        List<Map<String, Object>> list = null;

        StringBuffer sb = new StringBuffer();
        if ("com.mysql.jdbc.Driver".equals(dvo.getDriver())) {
            sb.append(" select COLUMN_NAME from information_schema.COLUMNS ");
            sb.append(" where table_name = '" + tablename + "' ");
            if (dvo.getBasename() != null && !"".equals(dvo.getBasename())) {
                sb.append(" and table_schema='" + dvo.getBasename() + "' ");
            }
        } else if ("oracle.jdbc.driver.OracleDriver".equals(dvo.getDriver())) {
            sb.append(" select t.column_name from user_col_comments t where t.table_name = '"+tablename.toUpperCase()+"'");
        }else {
            throw new Exception("暂时未对这种类型的数据库进行适配");
        }
//        sb.append(" order by column_name ");
        list = dao.execQuery(sb.toString(), null);
        return list;
    }

    //获取表的主键
    public String getTablePK(DataSynSourceVO dvo, String tablename) throws Exception {
        DataSynDB dao = new DataSynTool().getDataSynDB(dvo);
        List<Map<String, Object>> list = null;
        String pk = "";
        StringBuffer sb = new StringBuffer();
        if ("com.mysql.jdbc.Driver".equals(dvo.getDriver())) {
            sb.append(" SELECT " +
                    "  t.TABLE_NAME," +
                    "  t.CONSTRAINT_TYPE," +
                    "  c.COLUMN_NAME," +
                    "  c.ORDINAL_POSITION " +
                    " FROM   INFORMATION_SCHEMA.TABLE_CONSTRAINTS t LEFT JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE c on t.TABLE_NAME = c.TABLE_NAME " +
                    " WHERE " +
                    "  t.CONSTRAINT_TYPE = 'PRIMARY KEY'  " +
                    " and t.table_name = '" + tablename + "' ");
            if (dvo.getBasename() != null && !"".equals(dvo.getBasename())) {
                sb.append(" and t.table_schema='" + dvo.getBasename() + "' ");
                sb.append(" and c.CONSTRAINT_SCHEMA='" + dvo.getBasename() + "' ");
            }
        } else if ("oracle.jdbc.driver.OracleDriver".equals(dvo.getDriver())) {
            sb.append(" select cu.column_name as column_name from user_cons_columns cu, user_constraints au  " +
                        "where cu.constraint_name = au.constraint_name and au.table_name = '"+tablename.toUpperCase()+"' " +
                        "and ( au.constraint_type = 'U' or au.constraint_type = 'P' ) ");
        }else{
            throw new Exception("暂时未对该数据库进行适配");
        }
        list = dao.execQuery(sb.toString(), null);
        if(list!=null && list.size()>0){
            sb.setLength(0);
            for(Map<String,Object> pkmap : list){
                sb.append(pkmap.get("column_name").toString());
                sb.append(",");
            }
            sb.deleteCharAt(sb.length()-1);
        }
        pk = sb.toString();
        return pk;
    }

    //插入或更新表数据
    public Map<String,Object> insertOrUpdateSynTable(DataSynTableVO tvo) throws Exception {
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
            if(!"".equals(tablekey))sql.append(" `tablekey` = '" + tablekey + "' ,");
            if(!"".equals(fromtables))sql.append(" fromtables = '" + fromtables + "' ,");
            if(!"".equals(tablesrelation))sql.append(" tablesrelation = '" + tablesrelation + "' ,");
            if(!"".equals(allcolumn))sql.append(" `allcolumn` = '" + allcolumn + "' ,");
            if(!"".equals(relation))sql.append(" relation = '" + relation + "' ,");
            sql.deleteCharAt(sql.length()-1);
            sql.append(" where pk_table = '" + pk_table + "' ");
            rMap.put("msg", "表信息更新成功");
        }else{
            //不存在
            pk_table = UUID.randomUUID().toString();
            sql.append(" insert into syn_table(pk_table,tablename,`tablekey`,fromtables,tablesrelation,`allcolumn`,relation) "
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
