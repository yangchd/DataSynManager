package com.ycd.util.datasyn.impl;

import com.ycd.util.datasyn.dao.db.NotCloseDB;
import com.ycd.util.datasyn.dao.vo.DataSynTableVO;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by yangchd on 2017/8/28.
 * 无中间表同步模式
 */
public class UpdateTableByNoMiddle {

    //执行所有更新sql
    public Map<String, Object> UpdateTable(DataSynTableVO tvo, NotCloseDB fromdao, NotCloseDB todao) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        List<Map<String,Object>> slist = getUpdateSQL(tvo, fromdao, todao);
        if(slist!=null && slist.size()>0){
            for(Map<String,Object> sqlMap:slist){
                String sql = sqlMap.get("sql")==null?"":sqlMap.get("sql").toString();
                List<Object[]> plist = null;
                if(sqlMap.get("plist") != null){
                    plist = (List<Object[]>)sqlMap.get("plist");
                }
                if(!"".equals(sql))todao.execUpdateByArr(sql,plist);
            }
        }
        rMap.put("retflag","0");
        rMap.put("msg","更新成功");
        return rMap;
    }


    /**
     * 获取所有更新sql
     */
    private List<Map<String, Object>> getUpdateSQL(DataSynTableVO tvo, NotCloseDB fromdao, NotCloseDB todao) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        //参数初始化
        String tablename = tvo.getTablename();
        String fromtable = tvo.getFromtables();

        String pk = tvo.getTablekey();
        String column = tvo.getAllcolumn();
        String[] col = column.split(",");
        String[] topk = pk.split(",");
        if (col.length < 0 || topk.length < 0) {
            throw new Exception("配置信息有误！请检查同步配置！\r\n");
        }
        JSONObject relation = JSONObject.fromObject(tvo.getRelation());

        //from表关联建立
        String[] fromtables = fromtable.split(",");
        if (fromtables.length == 1) {
            //单表时不处理，多表时会进行多表关联
        } else {
            String on = tvo.getTablesrelation();
            if (on != null && !"".equals(on)) {
                StringBuffer ft = new StringBuffer();
                String[] ons = on.split(",");
                ft.append(" ").append(fromtables[0]).append(" ");
                for (int m = 0; m < ons.length; m++) {
                    ft.append(" left join ").append(fromtables[m + 1]).append(" on ").append(ons[m]);
                }
                fromtable = ft.toString();
            } else {
                throw new Exception("多表关联信息配置有误");
            }
        }

        //from pk确定，排序
        String[] frompk = new String[topk.length];
        for (int p = 0; p < topk.length; p++) {
            frompk[p] = relation.getString(topk[p]);
        }
        StringBuffer fronsql = new StringBuffer();
        fronsql.append("select DISTINCT ").append(UpdateDataTool.getColumnSQL(tvo,fromdao,"as"))
                .append(" from ").append(fromtable);
        fronsql.append(" order by ");
        for (String aFrompk : frompk) {
            fronsql.append(UpdateDataTool.addSymbol(aFrompk,fromdao)).append(" asc,");
        }
        fronsql.deleteCharAt(fronsql.length() - 1);

        StringBuffer tosql = new StringBuffer();
        tosql.append(" select DISTINCT " + UpdateDataTool.getColumnSQL(tvo,todao,null)
                + " from " + tablename);
        tosql.append(" order by ");
        for (String pkto : topk) {
            tosql.append(UpdateDataTool.addSymbol(pkto,todao)).append(" asc,");
        }
        tosql.deleteCharAt(tosql.length() - 1);

        //查询出需要同步的数据，然后进入比较
        List<Map<String, Object>> fromlist = fromdao.execQuery(fronsql.toString(), null);
        List<Map<String, Object>> tolist = todao.execQuery(tosql.toString(), null);

        String[] frompkvalue = new String[frompk.length];
        String[] topkvalue = new String[topk.length];

        //分别存放insert、delete、update的语句
        List<Map<String, Object>> insert = new ArrayList<>();
        List<Map<String, Object>> delete = new ArrayList<>();
        List<Map<String, Object>> update = new ArrayList<>();

        for (int i = 0; i < fromlist.size(); i++) {
            //先获取主键值
            for (int m = 0; m < frompkvalue.length; m++) {
                frompkvalue[m] = fromlist.get(i).get(topk[m]).toString();
            }
            for (int j = 0; j < tolist.size(); j++) {
                for (int n = 0; n < topkvalue.length; n++) {
                    topkvalue[n] = tolist.get(j).get(topk[n]).toString();
                }
                if (isPkEquals(frompkvalue, topkvalue)) {
                    //找到对应list,判断是否相等
                    if (!isUpdate(fromlist.get(i), tolist.get(j), column)) {
                        //如果不同
                        update.add(fromlist.get(i));
                    }
                    fromlist.remove(i--);
                    tolist.remove(j--);
                    break;
                }
            }
        }
        insert.addAll(fromlist);
        delete.addAll(tolist);

        Map<String,Object> insertSQL = getInsertSQL(insert, tvo,todao);
        Map<String,Object> deleteSQL = getDeleteSQL(delete, tvo,todao);
        Map<String,Object> updateSQL = getUpdateSQL(update, tvo,todao);
        List<Map<String,Object>> sqllist = new ArrayList<>();
        if(insertSQL!=null)sqllist.add(insertSQL);
        if(deleteSQL!=null)sqllist.add(deleteSQL);
        if(updateSQL!=null)sqllist.add(updateSQL);
        return sqllist;
    }


    private static Map<String,Object> getInsertSQL(List<Map<String, Object>> insert, DataSynTableVO tvo,NotCloseDB dao){
        Map<String,Object> rMap = new HashMap<>();
        List<Object[]> plist = new ArrayList<>();
        if (insert != null && insert.size() > 0) {
            //先准备sql
            StringBuffer insertsql = new StringBuffer();
            String column = UpdateDataTool.getColumnSQL(tvo,dao,null);
            insertsql.append("insert into "+tvo.getTablename()+" ("+column+") values (");
            for(int i=0;i<column.split(",").length;i++){
                insertsql.append("?,");
            }
            insertsql.deleteCharAt(insertsql.length()-1);
            insertsql.append(")");
            //准备参数

            for (Map<String, Object> anInsert : insert) {
                plist.add(UpdateDataTool.getObjectValue(anInsert, tvo.getAllcolumn()));
            }
            rMap.put("sql",insertsql.toString());
            rMap.put("plist",plist);
            return rMap;
        }else{
            return null;
        }
    }
    private static Map<String,Object> getDeleteSQL(List<Map<String, Object>> delete, DataSynTableVO tvo,NotCloseDB dao){
        Map<String,Object> rMap = new HashMap<>();
        String[] topk = tvo.getTablekey().split(",");
        if(delete!=null && delete.size()>0){
            StringBuffer deletesql = new StringBuffer();
            deletesql.append(" delete from "+tvo.getTablename() + " where 1=2 ");
            for(int i=0;i<delete.size();i++){
                deletesql.append(" or ( 1=1 ");
                for(int j=0;j<topk.length;j++){
                    deletesql.append(" and "+UpdateDataTool.addSymbol(topk[j],dao)+"='"+delete.get(i).get(topk[j]).toString()+"' ");
                }
                deletesql.append(")");
            }
            rMap.put("sql",deletesql.toString());
            rMap.put("plist",null);
            return rMap;
        }else{
            return null;
        }
    }
    private static Map<String,Object> getUpdateSQL(List<Map<String, Object>> update, DataSynTableVO tvo,NotCloseDB dao){
        Map<String,Object> rMap = new HashMap<>();
        List<Object[]> plist = new ArrayList<>();
        if(update!=null && update.size() > 0){
            StringBuffer updatesql = new StringBuffer();
            String[] columns = tvo.getAllcolumn().split(",");
            String[] typecolumns = UpdateDataTool.getColumnSQL(tvo,dao,null).split(",");
            String[] topk = tvo.getTablekey().split(",");

            updatesql.append(" update " + tvo.getTablename() + " set ");
            for (String typecolumn : typecolumns) {
                updatesql.append(typecolumn + " = ?,");
            }
            updatesql.deleteCharAt(updatesql.length()-1);
            updatesql.append(" where 1=1");
            for (String aTopk : topk) {
                updatesql.append(" and " + UpdateDataTool.addSymbol(aTopk, dao) + " = ?");
            }

            Object colvalue = "";
            for (Map<String, Object> anUpdate : update) {
                Object[] para = new Object[typecolumns.length + topk.length];
                for (int n = 0; n < typecolumns.length; n++) {
                    colvalue = anUpdate.get(columns[n]);
                    para[n] = colvalue;
                }
                for (int k = typecolumns.length; k < typecolumns.length + topk.length; k++) {
                    colvalue = anUpdate.get(topk[k-typecolumns.length]);
                    para[k] = colvalue;
                }
                plist.add(para);
            }
            rMap.put("sql",updatesql.toString());
            rMap.put("plist",plist);
            return rMap;
        }else{
            return null;
        }
    }


    //判断主键是否完全相等
    private static boolean isPkEquals(String[] str1, String[] str2) {
        if (str1.length != str2.length) return false;
        boolean flag = true;
        for (int i = 0; i < str1.length; i++) {
            //去掉首尾的空格进行比较
            if (!str1[i].trim().equals(str2[i].trim())) flag = false;
        }
        return flag;
    }

    //有不同，返回false null也加入判断
    private static boolean isUpdate(Map<String, Object> fromMap, Map<String, Object> toMap, String column) throws Exception {
        boolean flag = true;
        String[] tos = column.split(",");
        Object a,b;
        for (String to : tos) {
            a = fromMap.get(to);
            b = toMap.get(to);
            if(null != a && null != b){
                if(!a.toString().trim().equals(b.toString().trim()))flag = false;
            }else if( a==null && b == null){
                //相等
            }else{
                flag = false;
            }
        }
        return flag;
    }
}
