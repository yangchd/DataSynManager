package com.ycd.springboot.service.datasyn.impl;

import com.ycd.springboot.service.datasyn.IDataSynService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by yangchd on 2017/7/25.
 * 数据同步定时类
 */
public class DataSynJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //在这里执行定时任务实现方法

        //开始同步
        IDataSynService dataSynService = new DataSynServiceImpl();
        try {
            dataSynService.beginDataSyn();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
