package com.platfrom.manage.datasyn.controller;

import com.platfrom.manage.datasyn.DataSynTools;
import com.platfrom.manage.datasyn.IDataSynService;
import com.platfrom.manage.datasyn.IUpdateService;
import com.platfrom.manage.datasyn.dao.vo.DataSynSourceVO;
import com.platfrom.manage.datasyn.dao.vo.DataSynVO;
import com.platfrom.manage.datasyn.dao.vo.DataSynTableVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangchd on 2017/7/25.
 * 数据同步有关方法
 */
@Controller
@RequestMapping(value="/dataSyn")
public class DataSynController {

    @Autowired
    private IDataSynService dataSynService;

    @Autowired
    private IUpdateService updateService;

    @Autowired
    private DataSynTools tools;

    @RequestMapping(value="/testConnection")
    @ResponseBody
    public Map<String,Object> testConnection(DataSynSourceVO dvo){
        Map<String,Object> rMap = new HashMap<>();
        try {
            Boolean flag = dataSynService.conTest(dvo);
            if(flag){
                rMap = tools.getReMap("0","连接成功",null);
            }
        } catch (Exception e) {
            rMap = tools.getReMap("1","连接失败"+e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/saveDataSource")
    @ResponseBody
    public Map<String,Object> saveDataSource(DataSynSourceVO dvo){
        Map<String,Object> rMap = new HashMap<>();
        try {
            Boolean flag = dataSynService.conTest(dvo);
            if(flag){
                rMap = dataSynService.insertDataSourceByVO(dvo);
                rMap = tools.getReMap(rMap.get("retflag").toString(),"数据源保存成功",null);
            }
        } catch (Exception e) {
            rMap = tools.getReMap("1","数据源操作失败："+e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/getDataSource")
    @ResponseBody
    public Map<String,Object> getDataSourceList(DataSynSourceVO dvo){
        Map<String,Object> rMap = new HashMap<>();
        try {
            rMap = dataSynService.getDataSourceList(dvo);
            rMap = tools.getReMap(rMap.get("retflag").toString(),rMap.get("msg").toString(),rMap.get("data"));
        } catch (Exception e) {
            rMap = tools.getReMap("1",e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/deleteDataSource")
    @ResponseBody
    public Map<String,Object> deleteDataSource(DataSynSourceVO dvo){
        Map<String,Object> rMap = new HashMap<>();
        try {
            rMap = dataSynService.deleteDataSourceByVO(dvo);
            rMap = tools.getReMap(rMap.get("retflag").toString(),rMap.get("msg").toString(),null);
        } catch (Exception e) {
            rMap = tools.getReMap("1",e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/getTables")
    @ResponseBody
    public Map<String,Object> getAllTable(DataSynSourceVO dvo){
        Map<String,Object> rMap = new HashMap<>();
        try {
            rMap = dataSynService.getTableNameList(dvo);
            rMap = tools.getReMap(rMap.get("retflag").toString(),rMap.get("msg").toString(),rMap.get("data"));
        } catch (Exception e) {
            rMap = tools.getReMap("1",e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/getColumnName")
    @ResponseBody
    public Map<String,Object> getColumnName(DataSynSourceVO dvo, @RequestParam("tablename") String tablename){
        Map<String,Object> rMap = new HashMap<>();
        try {
            rMap = dataSynService.getColumnNameList(dvo,tablename);
            rMap = tools.getReMap(rMap.get("retflag").toString(),rMap.get("msg").toString(),rMap.get("data"));
        } catch (Exception e) {
            rMap = tools.getReMap("1",e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/saveDataSynTable")
    @ResponseBody
    public Map<String,Object> saveDataSynAndTable(DataSynVO dsvo, DataSynTableVO tvo){
        Map<String,Object> rMap = new HashMap<>();
        try {
            //先插入同步表信息
            dataSynService.saveDataSynTable(dsvo,tvo);
            dsvo.setPk_syntable(tvo.getPk_table());
            rMap = dataSynService.saveDataSyn(dsvo);
            rMap = tools.getReMap(rMap.get("retflag").toString(),rMap.get("msg").toString(),null);
        } catch (Exception e) {
            rMap = tools.getReMap("1",e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/updateSynTable")
    @ResponseBody
    public Map<String,Object> updateSynTable(DataSynTableVO tvo){
        Map<String,Object> rMap = new HashMap<>();
        try {
            //先插入同步表信息
            rMap = dataSynService.updateSynTableByVO(tvo);
            rMap = tools.getReMap(rMap.get("retflag").toString(),rMap.get("msg").toString(),null);
        } catch (Exception e) {
            rMap = tools.getReMap("1",e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/saveDataSyn")
    @ResponseBody
    public Map<String,Object> saveDataSyn(DataSynVO dsvo){
        Map<String,Object> rMap = new HashMap<>();
        try {
            //先插入同步表信息
            rMap = dataSynService.saveDataSyn(dsvo);
            rMap = tools.getReMap(rMap.get("retflag").toString(),rMap.get("msg").toString(),null);
        } catch (Exception e) {
            rMap = tools.getReMap("1",e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/getDataSynStatus")
    @ResponseBody
    public Map<String,Object> getDataSynStatus(){
        Map<String,Object> rMap = new HashMap<>();
        try {
            //先插入同步表信息
            rMap = dataSynService.getAllDataSyn();
            rMap = tools.getReMap(rMap.get("retflag").toString(),rMap.get("msg").toString(),rMap.get("data"));
        } catch (Exception e) {
            rMap = tools.getReMap("1",e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/getDataSyn")
    @ResponseBody
    public Map<String,Object> getDataSynByPk(DataSynVO dvo){
        Map<String,Object> rMap = new HashMap<>();
        try {
            //先插入同步表信息
            rMap = dataSynService.getDataSynByVO(dvo);
            rMap = tools.getReMap(rMap.get("retflag").toString(),rMap.get("msg").toString(),rMap.get("data"));
        } catch (Exception e) {
            rMap = tools.getReMap("1",e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/deleteDataSyn")
    @ResponseBody
    public Map<String,Object> deleteDataSyn(DataSynVO dvo){
        Map<String,Object> rMap = new HashMap<>();
        try {
            //先插入同步表信息
            rMap = dataSynService.deleteDataSynByVO(dvo);
            rMap = tools.getReMap(rMap.get("retflag").toString(),rMap.get("msg").toString(),null);
        } catch (Exception e) {
            rMap = tools.getReMap("1",e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/startSyn")
    @ResponseBody
    public Map<String,Object> startDataSyn(){
        Map<String,Object> rMap = new HashMap<>();
        try {
            updateService.beginDataSyn();
            rMap.put("retflag","0");
            rMap.put("msg","同步成功结束");
        } catch (Exception e) {
            rMap.put("retflag","1");
            rMap.put("msg",e.getMessage());
        }
        rMap = tools.getReMap(rMap.get("retflag").toString(),rMap.get("msg").toString(),null);
        return rMap;
    }

}
