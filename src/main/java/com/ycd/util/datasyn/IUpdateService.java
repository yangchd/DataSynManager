package com.ycd.util.datasyn;

/**
 * Created by yangchd on 2017/8/3.
 * 提供跟同步有关的服务
 */


public interface IUpdateService {

    //根据配置开始进行同步
    void beginDataSyn() throws Exception;

    //获取表结构

}
