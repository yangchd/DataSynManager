package com.platfrom.manage.datasyn.dao;

import com.platfrom.manage.datasyn.DataSynTools;
import com.platfrom.manage.datasyn.dao.vo.DataSynSourceVO;
import com.platfrom.manage.datasyn.dao.db.DBService;

import java.util.*;

/**
 * Created by yangchd on 2017/8/29.
 * 数据源VO相关数据库操作方法
 */
public class DataSynSourceDAO {

    //数据源查询方法
    public static List<Map<String,Object>> getDataSourceList(DataSynSourceVO vo) throws Exception {
        DBService dao = new DBService();
        StringBuffer sb = new StringBuffer();
        List<Object> plist = new ArrayList<>();
        sb.append(" select * from syn_datasource where 1=1 ");
        if(vo.getDatasourcename() != null){sb.append(" and datasourcename = ? ");plist.add(vo.getDatasourcename());}
        if(vo.getUrl() != null){sb.append(" and url = ? ");plist.add(vo.getUrl());}
        if(vo.getUsername() != null){sb.append(" and username = ? ");plist.add(vo.getUsername());}
        if(vo.getPassword() != null){sb.append(" and password = ? ");plist.add(vo.getPassword());}
        if(vo.getBasename() != null){sb.append(" and basename = ? ");plist.add(vo.getBasename());}
        if(vo.getDriver() != null){sb.append(" and driver = ? ");plist.add(vo.getDriver());}
        if(vo.getUrl_ip() != null){sb.append(" and url_ip = ? ");plist.add(vo.getUrl_ip());}
        if(vo.getUrl_port() != null){sb.append(" and url_port = ? ");plist.add(vo.getUrl_port());}
        Object[] objects = new Object[plist.size()];
        for (int i=0;i<plist.size();i++){
            objects[i] = plist.get(i);
        }
        return dao.execQuery(sb.toString(),objects);
    }

    //数据源删除方法
    public static Map<String,Object> deleteByVO(DataSynSourceVO vo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        String pk = vo.getPk_datasource();
        if(pk == null || "".equals(pk)){
            throw new Exception("删除失败,缺少要删除内容的主要信息，无法确定");
        }
        DBService dao = new DBService();
        String usesql = "select pk_sync from syn_datasyn where pk_datafrom = '"+pk+"' or pk_datato = '"+pk+"'";
        List<Map<String,Object>> list = dao.execQuery(usesql,null);
        if(list != null && list.size() > 0){
            throw new Exception("该数据源正在被使用，无法删除");
        }else{
            String sql = " delete from syn_datasource where pk_datasource = '"+ pk +"' ";
            dao.execUpdate(sql,null);
            rMap.put("retflag","0");
            rMap.put("msg","删除成功");
        }
        return rMap;
    }

    //数据源新增或者修改方法
    public static Map<String, Object> insertOrUpdateByVO(DataSynSourceVO vo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();
        DBService dao = new DBService();

        //先进行重复性校验
        String sql = "select * from syn_datasource where pk_datasource = '"+vo.getPk_datasource()+"'";
        List<Map<String,Object>> list = dao.execQuery(sql,null);
        StringBuffer sb = new StringBuffer();
        Object[] objs;
        if(list!=null && list.size() == 1){
            List<Object> plist = new ArrayList<>();
            sb.append(" update syn_datasource set ");
            if(vo.getDatasourcename() != null){sb.append(" datasourcename = ? ,");plist.add(vo.getDatasourcename());}
            if(vo.getUrl() != null){sb.append(" url = ? ,");plist.add(vo.getUrl());}
            if(vo.getUsername() != null){sb.append(" username = ? ,");plist.add(vo.getUsername());}
            if(vo.getPassword() != null){sb.append(" password = ? ,");plist.add(vo.getPassword());}
            if(vo.getBasename() != null){sb.append(" basename = ? ,");plist.add(vo.getBasename());}
            if(vo.getDriver() != null){sb.append(" driver = ? ,");plist.add(vo.getDriver());}
            if(vo.getUrl_ip() != null){sb.append(" url_ip = ? ,");plist.add(vo.getUrl_ip());}
            if(vo.getUrl_port() != null){sb.append(" url_port = ? ,");plist.add(vo.getUrl_port());}
            sb.deleteCharAt(sb.length()-1);
            sb.append(" where pk_datasource = ? ");
            plist.add( list.get(0).get("pk_datasource"));
            objs = new Object[plist.size()];
            for(int i=0;i<plist.size();i++){
                objs[i] = plist.get(i);
            }
        }else{
            if(list!=null && list.size() > 1){
                //大于1时，说明数据有误，进行删除操作
                StringBuffer deleteSB = new StringBuffer();
                objs = new Object[list.size()];
                deleteSB.append(" delete from syn_datasource where pk_datasource in (");
                for(int i=0;i<list.size();i++){
                    objs[i] = list.get(i).get("pk_datasource");
                    deleteSB.append("?,");
                }
                if(deleteSB.length() > 0){
                    deleteSB.deleteCharAt(deleteSB.length()-1);
                }
                DataSynTools.Logger("检测到错误的数据源信息，进行清除："+deleteSB.toString()+"参数："+ Arrays.toString(objs));
                dao.execUpdate(deleteSB.toString(),objs);
            }
            //不存在，做插入操作
            String pk = UUID.randomUUID().toString();
            sb.append(" insert into syn_datasource" +
                    "(pk_datasource,datasourcename,url,username,password,basename,driver,url_ip,url_port) ");
            sb.append(" values(?,?,?,?,?,?,?,?,?) ");
            objs = new Object[]{pk,vo.getDatasourcename(),vo.getUrl(),vo.getUsername(),
                            vo.getPassword(),vo.getBasename(),vo.getDriver(),vo.getUrl_ip(),vo.getUrl_port()};
        }
        int re = dao.execUpdate(sb.toString(),objs);
        if(re>0){
            rMap.put("retflag","0");
            rMap.put("msg","数据源保存成功");
        }else{
            rMap.put("retflag","1");
            rMap.put("msg","数据源保存失败");
        }
        return rMap;
    }
}
