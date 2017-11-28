package com.yangchd.data.manage.datasyn.dao;

import com.yangchd.data.manage.datasyn.dao.db.DBService;
import com.yangchd.data.manage.datasyn.dao.db.DataSynDB;
import com.yangchd.data.manage.datasyn.dao.db.NotCloseDB;
import com.yangchd.data.manage.datasyn.dao.vo.DataSynSourceVO;

import java.util.HashMap;
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

    //获取数据源下所有表
    public List<Map<String, Object>> getTableNameList(DataSynSourceVO dvo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();
        List<Map<String,Object>> list = null;
        DataSynDB dao = getDataSynDB(dvo);
        StringBuffer sb = new StringBuffer();

        if("com.mysql.jdbc.Driver".equals(dvo.getDriver())){
            sb.append(" select table_name from information_schema.tables where table_type='base table' ");
            String url = dvo.getUrl();
            if (dvo.getBasename()!=null && !"".equals(dvo.getBasename())) {
                sb.append(" and table_schema='" + dvo.getBasename() + "' ");
            }
        }else if("oracle.jdbc.driver.OracleDriver".equals(dvo.getDriver())){
            sb.append(" select table_name from user_tables");
        }else if("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(dvo.getDriver())){
            sb.append(" select name as table_name from SysObjects where XType = 'U' ");
        }else if("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(dvo.getDriver())){
            sb.append(" select name as table_name from SysObjects where XType = 'U'  ");
        }else {
            throw new Exception("暂时未对该数据库进行适配");
        }
        sb.append(" order by table_name ");
        list = dao.execQuery(sb.toString(), null);
        return list;
    }


    //获取表的主键
    public String getKeyByTableName(DataSynSourceVO dvo, String tablename) throws Exception {
        DataSynDB dao = getDataSynDB(dvo);
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
        }else if("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(dvo.getDriver())){
            sb.append(" select table_name,table_schema,column_name from INFORMATION_SCHEMA.KEY_COLUMN_USAGE where table_name = '"+tablename+"' ");
        }else if("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(dvo.getDriver())){
            sb.append(" select table_name,table_schema,column_name from INFORMATION_SCHEMA.KEY_COLUMN_USAGE where table_name = '"+tablename+"' ");
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

    //根据数据源和表名称获取表的所有列
    public List<Map<String, Object>> getAllColumnByTableName(DataSynSourceVO dvo, String tablename) throws Exception {
        DataSynDB dao = getDataSynDB(dvo);
        List<Map<String, Object>> list = null;
        StringBuffer sb = new StringBuffer();
        if ("com.mysql.jdbc.Driver".equals(dvo.getDriver())) {
            sb.append(" select COLUMN_NAME from information_schema.COLUMNS ");
            sb.append(" where table_name = '" + tablename + "' ");
            if (dvo.getBasename() != null && !"".equals(dvo.getBasename())) {
                sb.append(" and table_schema='" + dvo.getBasename() + "' ");
            }
        }else if ("oracle.jdbc.driver.OracleDriver".equals(dvo.getDriver())) {
            sb.append(" select t.column_name from user_col_comments t where t.table_name = '"+tablename.toUpperCase()+"'");
        }else if("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(dvo.getDriver())){
            sb.append(" select column_name from INFORMATION_SCHEMA.COLUMNS where table_name = '"+tablename+"' ");
        }else if("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(dvo.getDriver())){
            sb.append(" select column_name from INFORMATION_SCHEMA.COLUMNS where table_name = '"+tablename+"' ");
        }else {
            throw new Exception("暂时未对这种类型的数据库进行适配");
        }
        list = dao.execQuery(sb.toString(), null);
        return list;
    }



}
