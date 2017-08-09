package com.ycd.springboot.service.datasyn;

import com.ycd.springboot.service.datasyn.impl.NotCloseDB;
import com.ycd.springboot.vo.datasyn.DataSourceVO;
import com.ycd.springboot.vo.datasyn.DataSynTableVO;

import java.util.Map;

/**
 * Created by yangchd on 2017/8/3.
 * 更新服务
 */
public interface IUpdateService {

    Map<String, Object> updateData(DataSynTableVO tvo, NotCloseDB todao) throws Exception;

    Map<String,Object> createSynTable(DataSynTableVO tvo, NotCloseDB todao, DataSourceVO tdvo) throws Exception;

    Map<String,Object> transferTableSyn(DataSynTableVO tvo, NotCloseDB todao, NotCloseDB fromdao) throws Exception;
}
