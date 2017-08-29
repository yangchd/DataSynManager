package com.ycd.springboot.service.datasyn.impl;

import com.ycd.springboot.service.datasyn.IDataSynService;
import com.ycd.springboot.util.datasyn.dao.DataSynDAO;
import com.ycd.springboot.util.db.DBService;
import com.ycd.springboot.vo.datasyn.DataSynSourceVO;
import com.ycd.springboot.vo.datasyn.DataSynTableVO;
import com.ycd.springboot.vo.datasyn.DataSynVO;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

/**
 * Created by yangchd on 2017/7/25.
 * 同步实现类
 */

@Service("dataSynService")
public class DataSynServiceImpl implements IDataSynService {
    @Override
    public Boolean conTest(DataSynSourceVO dvo) throws Exception {
        Boolean flag = false;
        Connection con = null;
        Class.forName(dvo.getDriver());
        con = DriverManager.getConnection(dvo.getUrl(), dvo.getUsername(), dvo.getPassword());
        if (!con.isClosed()) {
            flag = true;
        }
        con.close();
        return flag;
    }

    @Override
    public Map<String, Object> insertDataSourceByVO(DataSynSourceVO dvo) throws Exception {
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

    @Override
    public Map<String, Object> getDataSourceList(DataSynSourceVO dvo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();
        DBService dao = new DBService();
        List<Map<String,Object>> list = null;

        String sql = "select * from syn_datasource";
        list = dao.execQuery(sql,null);

        rMap.put("retflag","0");
        rMap.put("msg","操作成功");
        rMap.put("data",list);
        return rMap;
    }

    @Override
    public Map<String, Object> getTableNameList(DataSynSourceVO dvo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();
        List<Map<String,Object>> list = null;
        DataSynDB dao = new DataSynTool().getDataSynDB(dvo);
        StringBuffer sb = new StringBuffer();

        if("com.mysql.jdbc.Driver".equals(dvo.getDriver())){
            sb.append(" select table_name from information_schema.tables where table_type='base table' ");
            String url = dvo.getUrl();
            if (dvo.getBasename()!=null && !"".equals(dvo.getBasename())) {
                sb.append(" and table_schema='" + dvo.getBasename() + "' ");
            }
        }else if("oracle.jdbc.driver.OracleDriver".equals(dvo.getDriver())){
            sb.append(" select table_name from user_tables");
        }else {
            rMap.put("retflag", "1");
            rMap.put("msg", "暂时没有适配！");
            return rMap;
        }
        sb.append(" order by table_name ");
        list = dao.execQuery(sb.toString(), null);
        rMap.put("retflag", "0");
        rMap.put("msg", "操作成功");
        rMap.put("data", list);
        return rMap;
    }

    @Override
    public Map<String, Object> getColumnNameList(DataSynSourceVO dvo, String tablename) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        List<Map<String,Object>> list = new DataSynTool().getAllColumn(dvo,tablename);
        rMap.put("retflag", "0");
        rMap.put("msg", "操作成功");
        rMap.put("data", list);
        return rMap;
    }

    @Override
    public Map<String, Object> saveDataSyn(DataSynVO dsvo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        rMap = DataSynDAO.insertOrUpdateByVO(dsvo);

        rMap.put("retflag","0");
        rMap.put("msg","操作成功");
        return rMap;
    }

    @Override
    public Map<String, Object> saveDataSynTable(DataSynVO dsvo, DataSynTableVO tvo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        //列名获取 格式整理
        String tablename = tvo.getTablename();
        String pk_datato = dsvo.getPk_datato();
        DataSynSourceVO dvo = new DataSynSourceVO();
        dvo.setPk_datasource(pk_datato);
        List<Map<String,Object>> columnList = new DataSynTool().getAllColumn(dvo,tablename);
        StringBuffer colsb = new StringBuffer();
        for (Map<String, Object> aColumnList : columnList) {
            colsb.append(aColumnList.get("column_name").toString());
            colsb.append(",");
        }
        colsb.deleteCharAt(colsb.length()-1);
        String allcolumn = colsb.toString();

        //table主键获取
        String tablekey = new DataSynTool().getTablePK(dvo,tablename);

        tvo.setAllcolumn(allcolumn);
        tvo.setTablekey(tablekey);

        rMap = new DataSynTool().insertOrUpdateSynTable(tvo);

        rMap.put("retflag","0");
        rMap.put("msg","操作成功");
        rMap.put("pk_table",tvo.getPk_table());
        return rMap;
    }

    @Override
    public Map<String, Object> getAllDataSyn() throws Exception {
        Map<String, Object> rMap = new HashMap<>();
        DBService dao = new DBService();

        String sql = "select * from syn_datasyn a left join syn_table b on a.pk_syntable = b.pk_table";
        List<Map<String,Object>> list = dao.execQuery(sql,null);
        rMap.put("retflag","0");
        rMap.put("msg","查询成功");
        rMap.put("data",list);
        return rMap;
    }
}
