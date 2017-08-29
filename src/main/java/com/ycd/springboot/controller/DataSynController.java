package com.ycd.springboot.controller;

import com.ycd.springboot.service.datasyn.IDataSynService;
import com.ycd.springboot.util.datasyn.IUpdateService;
import com.ycd.springboot.util.Tools;
import com.ycd.springboot.vo.datasyn.DataSynSourceVO;
import com.ycd.springboot.vo.datasyn.DataSynTableVO;
import com.ycd.springboot.vo.datasyn.DataSynVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangchd on 2017/7/25.
 * 数据同步
 */
@Controller
@RequestMapping(value="/datasyn")
public class DataSynController {

    @Autowired
    private IDataSynService dataSynService;

    @Autowired
    private IUpdateService updateService;

    @Autowired
    private Tools tools;

    @RequestMapping(value="/savedatasource")
    @ResponseBody
    public Map<String,Object> saveDataSource(DataSynSourceVO dvo){
        Map<String,Object> rMap = new HashMap<>();
        try {
            Boolean flag = dataSynService.conTest(dvo);
            if(flag){
                rMap = dataSynService.insertDataSourceByVO(dvo);
                rMap = tools.getReMap(rMap.get("retflag").toString(),"连接成功，"+rMap.get("msg").toString(),null);
            }
        } catch (Exception e) {
            rMap = tools.getReMap("1","连接失败"+e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/getdatasource")
    @ResponseBody
    public Map<String,Object> getDataSource(DataSynSourceVO dvo){
        Map<String,Object> rMap = new HashMap<>();
        try {
            rMap = dataSynService.getDataSourceList(dvo);
            rMap = tools.getReMap(rMap.get("retflag").toString(),rMap.get("msg").toString(),rMap.get("data"));
        } catch (Exception e) {
            rMap = tools.getReMap("1",e.getMessage(),null);
        }
        return rMap;
    }

    @RequestMapping(value="/getalltable")
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

    @RequestMapping(value="/getcolumnname")
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

    @RequestMapping(value="/savedatasyn")
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

    @RequestMapping(value="/savedatasyntable")
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

    @RequestMapping(value="/getdatasynstatus")
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

    @RequestMapping(value="/startdatasyn")
    @ResponseBody
    public Map<String,Object> startDataSyn(){
        Map<String,Object> rMap = new HashMap<>();
        try {
            updateService.beginDataSyn();
            rMap.put("retflag","0");
            rMap.put("msg","同步成功结束");
        } catch (Exception e) {
            rMap.put("retflag","1");
            rMap.put("msg","同步失败! "+e.getMessage());
        }
        return rMap;
    }

}
