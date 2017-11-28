package com.yangchd.data.manage.datasyn.impl;

import com.yangchd.data.manage.datasyn.IDataSynService;
import com.yangchd.data.manage.datasyn.dao.DaoTool;
import com.yangchd.data.manage.datasyn.dao.DataSynDAO;
import com.yangchd.data.manage.datasyn.dao.DataSynSourceDAO;
import com.yangchd.data.manage.datasyn.dao.DataSynTableDAO;
import com.yangchd.data.manage.datasyn.dao.vo.DataSynSourceVO;
import com.yangchd.data.manage.datasyn.dao.vo.DataSynTableVO;
import com.yangchd.data.manage.datasyn.dao.vo.DataSynVO;
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
        return DataSynSourceDAO.insertOrUpdateByVO(dvo);
    }

    @Override
    public Map<String, Object> getDataSourceList(DataSynSourceVO dvo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();
        List<Map<String, Object>> list = DataSynSourceDAO.getDataSourceList(dvo);
        rMap.put("retflag", "0");
        rMap.put("msg", "操作成功");
        rMap.put("data", list);
        return rMap;
    }

    @Override
    public Map<String, Object> deleteDataSourceByVO(DataSynSourceVO dvo) throws Exception {
        return DataSynSourceDAO.deleteByVO(dvo);
    }

    @Override
    public Map<String, Object> getTableNameList(DataSynSourceVO dvo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();
        List<Map<String, Object>> list = new DaoTool().getTableNameList(dvo);
        rMap.put("retflag", "0");
        rMap.put("msg", "操作成功");
        rMap.put("data", list);
        return rMap;
    }

    @Override
    public Map<String, Object> getColumnNameList(DataSynSourceVO dvo, String tablename) throws Exception {
        Map<String, Object> rMap = new HashMap<>();
        List<Map<String, Object>> list = new DaoTool().getAllColumnByTableName(dvo, tablename);
        rMap.put("retflag", "0");
        rMap.put("msg", "操作成功");
        rMap.put("data", list);
        return rMap;
    }

    @Override
    public Map<String, Object> saveDataSyn(DataSynVO dsvo) throws Exception {
        Map<String, Object> rMap = DataSynDAO.insertOrUpdateByVO(dsvo);
        rMap.put("retflag", "0");
        return rMap;
    }

    @Override
    public Map<String, Object> saveDataSynTable(DataSynVO dsvo, DataSynTableVO tvo) throws Exception {
        //列名获取 格式整理
        String tablename = tvo.getTablename();
        String pk_datato = dsvo.getPk_datato();
        DataSynSourceVO dvo = new DataSynSourceVO();
        dvo.setPk_datasource(pk_datato);
        List<Map<String, Object>> columnList = new DaoTool().getAllColumnByTableName(dvo, tablename);
        StringBuffer colsb = new StringBuffer();
        for (Map<String, Object> aColumnList : columnList) {
            colsb.append(aColumnList.get("column_name").toString());
            colsb.append(",");
        }
        colsb.deleteCharAt(colsb.length() - 1);
        String allcolumn = colsb.toString();

        //table主键获取
        String tablekey = new DaoTool().getKeyByTableName(dvo, tablename);

        tvo.setAllcolumn(allcolumn);
        tvo.setTablekey(tablekey);

        Map<String, Object> rMap = DataSynTableDAO.insertOrUpdateByVO(tvo);

        rMap.put("retflag", "0");
        rMap.put("msg", "操作成功");
        rMap.put("pk_table", tvo.getPk_table());
        return rMap;
    }

    @Override
    public Map<String, Object> getAllDataSyn() throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        DataSynVO dsvo = new DataSynVO();
        List<Map<String, Object>> list = DataSynDAO.getDataSynList(dsvo);
        rMap.put("retflag", "0");
        rMap.put("msg", "查询成功");
        rMap.put("data", list);
        return rMap;
    }

    @Override
    public Map<String, Object> getDataSynByVO(DataSynVO dvo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        List<Map<String, Object>> list = DataSynDAO.getDataSynList(dvo);

        rMap.put("retflag", "0");
        rMap.put("msg", "查询成功");
        rMap.put("data", list);
        return rMap;
    }

    @Override
    public Map<String, Object> deleteDataSynByVO(DataSynVO dvo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();
        rMap = DataSynDAO.deleteByVO(dvo);
        return rMap;
    }

    @Override
    public Map<String, Object> updateSynTableByVO(DataSynTableVO tvo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();
        rMap = DataSynTableDAO.insertOrUpdateByVO(tvo);
        rMap.put("retflag", "0");
        rMap.put("msg", "修改成功");
        return rMap;
    }


}
