package com.ycd.util.datasyn.impl;

import com.ycd.springboot.util.TimeTool;
import com.ycd.util.datasyn.IUpdateService;
import com.ycd.util.datasyn.dao.DataSynDAO;
import com.ycd.util.datasyn.vo.DataSynVO;
import com.ycd.util.datasyn.dao.db.DBService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by yangchd on 2017/8/3.
 * 更新服务实现
 */

@Service("updateService")
public class UpdateServiceImpl implements IUpdateService {

    @Override
    public void beginDataSyn() throws Exception {
        //获取所有需要同步的配置
        DBService dao = new DBService();
        String sql = "select * from syn_datasyn where flag = 'true'";
        List<Map<String,Object>> synlist = dao.execQuery(sql,null);

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String msg = "";

        //对每一条信息分别进行同步
        for(Map<String,Object> synMap : synlist){
            Date begin = new Date();
            try {
                //这里先暂时写死，不采用中间表形式
                new UpdateDataTool().startDataSynByMap(synMap,"false");
            } catch (Exception e) {
                msg = "同步失败"+e.getMessage();
                throw new Exception(msg);
            }finally {
                Date end = new Date();
                int cost = TimeTool.difTimeByDate(begin,end);
                String timecose = String.valueOf(cost) + "ms";
                String begintime = sf.format(begin);
                if("".equals(msg))msg = "同步成功！";

                //更新同步信息
                DataSynVO dsvo = new DataSynVO();
                dsvo.setPk_sync(synMap.get("pk_sync").toString());
                dsvo.setLasttime(begintime);
                dsvo.setTimecost(timecose);
                dsvo.setDatasynmsg(msg);
                DataSynDAO.insertOrUpdateByVO(dsvo);
            }
        }
    }

}
