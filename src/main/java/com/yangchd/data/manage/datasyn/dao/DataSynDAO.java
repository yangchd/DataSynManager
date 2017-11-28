package com.yangchd.data.manage.datasyn.dao;

import com.yangchd.data.manage.datasyn.dao.db.DBService;
import com.yangchd.data.manage.datasyn.dao.vo.DataSynVO;

import java.util.*;

/**
 * Created by yangchd on 2017/8/28.
 * datasyn表相关DAO
 */
public class DataSynDAO {

    //查询方法
    public static List<Map<String,Object>> getDataSynList(DataSynVO dsvo) throws Exception {
        DBService dao = new DBService();

        StringBuffer sql = new StringBuffer();
        sql.append(" select * from syn_datasyn a left join syn_table b on a.pk_syntable = b.pk_table where 1=1 ");
        List<Object> plist = new ArrayList<>();
        if(dsvo.getPk_sync() != null){sql.append(" and pk_sync = ? ");plist.add(dsvo.getPk_sync());}
        if(dsvo.getPk_datafrom() != null){sql.append(" and pk_datafrom = ? ");plist.add(dsvo.getPk_datafrom());}
        if(dsvo.getPk_datato() != null){sql.append(" and pk_datato = ? ");plist.add(dsvo.getPk_datato());}
        if(dsvo.getPk_syntable() != null){sql.append(" and pk_syntable = ? ");plist.add(dsvo.getPk_syntable());}
        if(dsvo.getFlag() != null){sql.append(" and flag = ? ");plist.add(dsvo.getFlag());}
        if(dsvo.getLasttime() != null){sql.append(" and lasttime = ? ");plist.add(dsvo.getLasttime());}
        if(dsvo.getTimecost() != null){sql.append(" and timecost = ? ");plist.add(dsvo.getTimecost());}
        if(dsvo.getDatasynmsg() != null){sql.append(" and datasynmsg = ? ");plist.add(dsvo.getDatasynmsg());}

        Object[] objects = new Object[plist.size()];
        for(int i=0;i<plist.size();i++){
            objects[i] = plist.get(i);
        }
        return dao.execQuery(sql.toString(),objects);
    }


    //删除操作
    public static Map<String,Object> deleteByVO(DataSynVO dsvo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        String pk_sync = dsvo.getPk_sync();
        if(pk_sync ==null || "".equals(pk_sync)){
            rMap.put("retflag","1");
            rMap.put("msg","删除失败,缺少要删除内容的主要信息，无法确定");
            return rMap;
        }
        DBService dao = new DBService();
        String sql = " delete from syn_datasyn where pk_sync = '"+pk_sync+"' ";
        dao.execUpdate(sql,null);

        //对关联的表配置信息进行删除
        String deleteTableSQL = "delete from syn_table where pk_table not in (select pk_syntable from syn_datasyn)";
        dao.execUpdate(deleteTableSQL,null);
        rMap.put("retflag","0");
        rMap.put("msg","删除成功");
        return rMap;
    }

    //插入或更新表同步配置表数据
    public static Map<String,Object> insertOrUpdateByVO(DataSynVO dsvo) throws Exception {
        DBService dao = new DBService();
        Map<String,Object> rMap = new HashMap<>();

        String pk_sync = dsvo.getPk_sync();
        if(pk_sync==null || "".equals(pk_sync)){
            //如果主键为空，做重复性判断
            String tablesql = " select pk_sync,pk_datafrom,pk_datato,pk_syntable from syn_datasyn where "
                    +" pk_datafrom='"+dsvo.getPk_datafrom()+"' and  pk_datato='"+dsvo.getPk_datato()+"' " +
                    " and pk_syntable='"+dsvo.getPk_syntable()+"' ";
            List<Map<String,Object>> tlist = dao.execQuery(tablesql,null);
            if(tlist!=null && tlist.size() == 1){
                pk_sync = tlist.get(0).get("pk_sync")==null?"":tlist.get(0).get("pk_sync").toString();
            }else{
                pk_sync = null;
            }
        }
        StringBuffer sql = new StringBuffer();
        Object[] para;
        if(pk_sync!=null && !"".equals(pk_sync)){
            List<Object> plist = new ArrayList<>();
            sql.append(" update syn_datasyn set ");
            if(dsvo.getPk_datafrom() != null){sql.append(" pk_datafrom = ? ,");plist.add(dsvo.getPk_datafrom());}
            if(dsvo.getPk_datato() != null){sql.append(" pk_datato = ? ,");plist.add(dsvo.getPk_datato());}
            if(dsvo.getPk_syntable() != null){sql.append(" pk_syntable = ? ,");plist.add(dsvo.getPk_syntable());}
            if(dsvo.getFlag() != null){sql.append(" flag = ? ,");plist.add(dsvo.getFlag());}
            if(dsvo.getLasttime() != null){sql.append( " lasttime = ? ,");plist.add(dsvo.getLasttime());}
            if(dsvo.getTimecost() != null){sql.append(" timecost = ? ,");plist.add(dsvo.getTimecost());}
            if(dsvo.getDatasynmsg() != null){sql.append(" datasynmsg = ? ,");plist.add(dsvo.getDatasynmsg());}
            sql.deleteCharAt(sql.length()-1);
            sql.append(" where pk_sync = ? ");
            plist.add(pk_sync);
            para = new Object[plist.size()];
            for (int i = 0; i < plist.size(); i++) {
                para[i] = plist.get(i);
            }
            dao.execUpdate(sql.toString(), para);
            rMap.put("msg","同步列表更新成功");
        }else{
            pk_sync = UUID.randomUUID().toString();
            sql.append(" insert into syn_datasyn(pk_sync,pk_datafrom,pk_datato,pk_syntable,flag,lasttime,timecost,datasynmsg) "
                    +" values(?,?,?,?,?,?,?,?) ");
            para = new Object[]{pk_sync,dsvo.getPk_datafrom(),dsvo.getPk_datato(),dsvo.getPk_syntable(),dsvo.getFlag(),
                    dsvo.getLasttime(),dsvo.getTimecost(),dsvo.getDatasynmsg()};
            dao.execUpdate(sql.toString(),para);
            rMap.put("msg","同步列表新增成功");
        }
        dsvo.setPk_sync(pk_sync);
        rMap.put("retflag","0");
        return rMap;
    }

}
