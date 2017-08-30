package com.ycd.util.datasyn.dao;

import com.ycd.util.datasyn.dao.db.DBService;
import com.ycd.util.datasyn.vo.DataSynVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yangchd on 2017/8/28.
 * datasyn表相关DAO
 */
public class DataSynDAO {

    //插入或更新表同步配置表数据
    public static Map<String,Object> insertOrUpdateByVO(DataSynVO dsvo) throws Exception {
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
            if(tlist!=null && tlist.size() == 1){
                pk_sync = tlist.get(0).get("pk_sync")==null?"":tlist.get(0).get("pk_sync").toString();
            }else{
                pk_sync = null;
            }
        }
        StringBuffer sql = new StringBuffer();
        if(pk_sync!=null && !"".equals(pk_sync)){
            sql.append(" update syn_datasyn set ");
            if(!"".equals(pk_datafrom))sql.append(" pk_datafrom = '"+pk_datafrom+"' ,");
            if(!"".equals(pk_datato))sql.append(" pk_datato = '"+pk_datato+"' ,");
            if(!"".equals(pk_syntable))sql.append(" pk_syntable = '"+pk_syntable+"' ,");
            if(!"".equals(flag))sql.append(" flag = '"+flag+"' ,");
            if(!"".equals(lasttime))sql.append( " lasttime = '"+lasttime+"' ,");
            if(!"".equals(timecost))sql.append(" timecost = '"+timecost+"' ,");
            if(!"".equals(datasynmsg))sql.append(" datasynmsg = '"+datasynmsg+"' ,");
            sql.deleteCharAt(sql.length()-1);
            sql.append(" where pk_sync = '"+pk_sync+"' ");
            rMap.put("msg","同步列表更新成功");
        }else{
            pk_sync = UUID.randomUUID().toString();
            sql.append(" insert into syn_datasyn(pk_sync,pk_datafrom,pk_datato,pk_syntable,flag,lasttime,timecost,datasynmsg) "
                    +" values('"+pk_sync+"','"+pk_datafrom+"','"+pk_datato+"','"+pk_syntable
                    +"','"+flag+"','"+lasttime+"','"+timecost+"','"+datasynmsg+"') ");
            rMap.put("msg","同步列表新增成功");
        }
        dsvo.setPk_sync(pk_sync);
        dao.execUpdate(sql.toString(),null);
        rMap.put("retflag","0");
        return rMap;
    }

}
