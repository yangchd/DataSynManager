package com.ycd.springboot.service.datasyn.impl;

import com.ycd.springboot.service.datasyn.IUpdateService;
import com.ycd.springboot.util.db.DBService;
import com.ycd.springboot.vo.datasyn.DataSourceVO;
import com.ycd.springboot.vo.datasyn.DataSynTableVO;
import com.ycd.springboot.vo.datasyn.DataSynVO;

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
    public DataSynDB getDataSynDB(DataSourceVO dvo) throws Exception {
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
    public NotCloseDB getNotCloseDB(DataSourceVO dvo) throws Exception {
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
    public List<Map<String, Object>> getAllColumn(DataSourceVO dvo, String tablename) throws Exception {
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
        list = dao.execQuery(sb.toString(), null);
        return list;
    }

    //获取表的主键
    public String getTablePK(DataSourceVO dvo, String tablename) throws Exception {
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
                    " FROM " +
                    "  INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS t, " +
                    "  INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS c " +
                    " WHERE " +
                    "  t.TABLE_NAME = c.TABLE_NAME " +
                    " AND t.CONSTRAINT_TYPE = 'PRIMARY KEY' " +
                    " and t.table_name = '" + tablename + "' ");
            if (dvo.getBasename() != null && !"".equals(dvo.getBasename())) {
                sb.append(" and t.table_schema='" + dvo.getBasename() + "' ");
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

    //插入或更新表同步数据
    public Map<String,Object> insertOrUpdateSyn(DataSynVO dsvo) throws Exception {
        DBService dao = new DBService();
        Map<String,Object> rMap = new HashMap<>();

        //解决直接插入null的问题
        String pk_datafrom = dsvo.getPk_datafrom()==null?"":dsvo.getPk_datafrom();
        String pk_datato = dsvo.getPk_datato()==null?"":dsvo.getPk_datato();
        String pk_syntable = dsvo.getPk_syntable()==null?"":dsvo.getPk_syntable();
        String flag = dsvo.getFlag()==null?"":dsvo.getFlag();
        String lasttime = dsvo.getLasttime()==null?"":dsvo.getLasttime();
        String timecost = dsvo.getTimecost()==null?"":dsvo.getTimecost();
        String datasynmsg = dsvo.getDatasynmsg()==null?"":dsvo.getDatasynmsg();

        String pk_sync = dsvo.getPk_sync();
        if(pk_sync==null || "".equals(pk_sync)){
            //如果主键为空，做重复性判断
            String tablesql = " select pk_sync,pk_datafrom,pk_datato,pk_syntable from syn_datasyn where "
                            +" pk_datafrom='"+pk_datafrom+"' and  pk_datato='"+pk_datato+"' and pk_syntable='"+pk_syntable+"' ";
            List<Map<String,Object>> tlist = dao.execQuery(tablesql,null);
            if(tlist!=null && tlist.size()>0){
                pk_sync = tlist.get(0).get("pk_sync")==null?"":tlist.get(0).get("pk_sync").toString();
            }else{
                pk_sync = null;
            }
        }
        StringBuffer sql = new StringBuffer();
        if(pk_sync!=null && !"".equals(pk_sync)){
            sql.append(" update syn_datasyn set ");
            if(!"".equals(pk_datafrom))sql.append(" pk_datafrom = '"+pk_datafrom+"' ,");
            if(!"".equals(pk_datato))sql.append(" `pk_datato` = '"+pk_datato+"' ,");
            if(!"".equals(pk_syntable))sql.append(" pk_syntable = '"+pk_syntable+"' ,");
            if(!"".equals(flag))sql.append(" flag = '"+flag+"' ,");
            if(!"".equals(lasttime))sql.append( " `lasttime` = '"+lasttime+"' ,");
            if(!"".equals(timecost))sql.append(" timecost = '"+timecost+"' ,");
            if(!"".equals(datasynmsg))sql.append(" datasynmsg = '"+datasynmsg+"' ,");
            sql.deleteCharAt(sql.length()-1);
            sql.append(" where pk_sync = '"+pk_sync+"' ");
            rMap.put("msg","同步列表更新成功");
        }else{
            pk_sync = UUID.randomUUID().toString();
            sql.append(" insert into syn_datasyn(pk_sync,pk_datafrom,`pk_datato`,pk_syntable,flag,`lasttime`,timecost,datasynmsg) "
                    +" values('"+pk_sync+"','"+pk_datafrom+"','"+pk_datato+"','"+pk_syntable
                    +"','"+flag+"','"+lasttime+"','"+timecost+"','"+datasynmsg+"') ");
            rMap.put("msg","同步列表新增成功");
        }
        dsvo.setPk_sync(pk_sync);
        dao.execUpdate(sql.toString(),null);
        rMap.put("retflag","0");
        return rMap;
    }


    //开始同步
    public Map<String,Object> startDataSynByMap(Map<String,Object> synMap) throws Exception {
        Map<String,Object> rMap = new HashMap<>();
        String msg = "";

        //获取目标库和源库的连接
        DataSourceVO fdvo = new DataSourceVO();
        DataSourceVO tdvo = new DataSourceVO();

        fdvo.setPk_datasource(synMap.get("pk_datafrom").toString());
        tdvo.setPk_datasource(synMap.get("pk_datato").toString());
        NotCloseDB fromdao = null;
        NotCloseDB todao = null;
        try {
            fromdao = getNotCloseDB(fdvo);
            todao = getNotCloseDB(tdvo);
        } catch (Exception e) {
            if(fromdao != null){
                fromdao.destory();}
            if(todao != null){
                todao.destory();
            }
            throw new Exception(e.getMessage());
        }

        //建立一个标志位，在有异常的时候，停止同步
        boolean flag = true;

        //获取完整的同步信息
        DataSynTableVO tvo = new DataSynTableVO();
        tvo.setPk_table(synMap.get("pk_syntable").toString());
        tvo = getDataSynTableVO(tvo);

        IUpdateService updateService = new UpdateServiceImpl();
        //建立同步表
        rMap = updateService.createSynTable(tvo,todao,tdvo);
//        msg = msg + rMap.get("msg").toString();

        //更新同步表数据
        rMap = updateService.transferTableSyn(tvo,todao,fromdao);
//        msg = msg + rMap.get("msg").toString();

        //数据更新
        rMap = updateService.updateData(tvo,todao);
//        msg = msg + rMap.get("msg").toString();

        fromdao.destory();
        todao.destory();
        rMap.put("retflag","0");
        rMap.put("msg",msg);
        System.out.println(msg);
        return rMap;
    }

    //解析同步表信息
    public DataSynTableVO getDataSynTableVO(DataSynTableVO tvo) throws Exception {
        String pk_table = tvo.getPk_table();
        if(pk_table != null && !"".equals(pk_table)){
            DBService dao = new DBService();
            String sql = "select * from syn_table where pk_table = '"+pk_table+"'";
            List<Map<String,Object>> list = dao.execQuery(sql,null);
            if(list!=null && list.size() == 1){
                tvo.setTablename(list.get(0).get("tablename")==null?"":list.get(0).get("tablename").toString());
                tvo.setTablekey(list.get(0).get("tablekey")==null?"":list.get(0).get("tablekey").toString());
                tvo.setAllcolumn(list.get(0).get("allcolumn")==null?"":list.get(0).get("allcolumn").toString());
                tvo.setFromtables(list.get(0).get("fromtables")==null?"":list.get(0).get("fromtables").toString());
                tvo.setRelation(list.get(0).get("relation")==null?"":list.get(0).get("relation").toString());
                tvo.setTablesrelation(list.get(0).get("tablesrelation")==null?"":list.get(0).get("tablesrelation").toString());
            }else {
                throw new Exception("配置的同步信息有误，请检查");
            }
        }else{
            throw new Exception("未找到配置的同步信息，请检查配置");
        }
        return tvo;
    }
}
