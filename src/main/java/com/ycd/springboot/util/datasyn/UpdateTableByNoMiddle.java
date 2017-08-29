package com.ycd.springboot.util.datasyn;

import com.ycd.springboot.util.datasyn.dao.NotCloseDB;
import com.ycd.springboot.vo.datasyn.DataSynTableVO;
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

    private static int SQL_SIZE = 500;

    //执行所有更新sql
    public Map<String, Object> UpdateTable(DataSynTableVO tvo, NotCloseDB fromdao, NotCloseDB todao) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        Map<String, Object> sql = getUpdateSQL(tvo, fromdao, todao);
        String insertSQL = sql.get("insert")==null?"":sql.get("insert").toString();
        String deleteSQL = sql.get("delete")==null?"":sql.get("delete").toString();
        String updateSQL = sql.get("update")==null?"":sql.get("update").toString();

        if(insertSQL!=null && !"".equals(insertSQL)){
            String[] sqls = insertSQL.split(";");
            for(String s:sqls){
                if(s!=null && !"".equals(s))todao.execUpdate(s, null);
            }
        }
        if(deleteSQL!=null && !"".equals(deleteSQL)){
            String[] sqls = deleteSQL.split(";");
            for(String s:sqls){
                if(s!=null && !"".equals(s))todao.execUpdate(s, null);
            }
        }
        if(updateSQL!=null && !"".equals(updateSQL)){
            String[] sqls = updateSQL.split(";");
            for(String s:sqls){
                if(s!=null && !"".equals(s))todao.execUpdate(s, null);
            }
        }
        rMap.put("retflag","0");
        rMap.put("msg","更新成功");
        return rMap;
    }


    /**
     * 获取所有更新sql
     */
    private Map<String, Object> getUpdateSQL(DataSynTableVO tvo, NotCloseDB fromdao, NotCloseDB todao) throws Exception {
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
        column = UpdateDataTool.changeColumn(column, relation, "to");
        String fromcolumn = UpdateDataTool.changeColumn(column, relation, "from");

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
        fronsql.append("select DISTINCT ").append(UpdateDataTool.changeColumn(column,relation,"as"))
                .append(" from ").append(fromtable);
        fronsql.append(" order by ");
        for (String aFrompk : frompk) {
            fronsql.append(aFrompk).append(" asc,");
        }
        fronsql.deleteCharAt(fronsql.length() - 1);

        StringBuffer tosql = new StringBuffer();
        tosql.append(" select DISTINCT " + column
                + " from " + tablename);
        tosql.append(" order by ");
        for (String pkto : topk) {
            tosql.append(pkto).append(" asc,");
        }
        tosql.deleteCharAt(tosql.length() - 1);

        //查询出新的所有数据,然后进行三部操作
        List<Map<String, Object>> fromlist = fromdao.execQuery(fronsql.toString(), null);
        List<Map<String, Object>> tolist = todao.execQuery(tosql.toString(), null);

        if (topk.length != frompk.length) {
            throw new Exception("主键配置有误");
        }

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

        String insertSQL = getInsertSQL(insert, tablename, column);
        String deleteSQL = getDeleteSQL(delete, tablename, topk);
        String updateSQL = getUpdateSQL(update, tablename, column, topk);
        rMap.put("insert", insertSQL);
        rMap.put("delete", deleteSQL);
        rMap.put("update", updateSQL);
        return rMap;
    }


    private static String getInsertSQL(List<Map<String, Object>> insert, String tablename, String column){

        if (insert != null && insert.size() > 0) {
            StringBuffer insertsql = new StringBuffer();
            //有数据 准备插入
            int SIZE = SQL_SIZE;
            int num = 0;
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < insert.size(); j++) {
                if (num == SIZE) {
                    //到达一定条数，先插入
                    sb.append(UpdateDataTool.getColValue(insert.get(j), column));
                    sb.deleteCharAt(sb.length() - 1);
                    StringBuffer sqlsb = new StringBuffer();
                    sqlsb.append(" insert into " + tablename + "(" + column + ") values  ");
                    sqlsb.append(sb.toString());
                    insertsql.append(sqlsb.toString()+";");
                    sb.setLength(0);
                    num = 0;
                } else {
                    sb.append(UpdateDataTool.getColValue(insert.get(j), column));
                    num++;
                }
            }
            if (num > 0 && num < SIZE) {//插入最后不到整数的一些数据
                StringBuffer sqlsb = new StringBuffer();
                sb.deleteCharAt(sb.length() - 1);
                sqlsb.append(" insert into " + tablename + "(" + column + ") values  ");
                sqlsb.append(sb.toString());
                insertsql.append(sqlsb.toString()+";");
            }
            return insertsql.toString();
        }else{
            return null;
        }
    }
    private static String getDeleteSQL(List<Map<String, Object>> delete, String tablename, String[] topk){
        if(delete!=null && delete.size()>0){
            StringBuffer deletesql = new StringBuffer();
            deletesql.append(" delete from "+tablename + " where 1=2 ");
            for(int i=0;i<delete.size();i++){
                deletesql.append(" or ( 1=1 ");
                for(int j=0;j<topk.length;j++){
                    deletesql.append(" and "+topk[j]+"='"+delete.get(i).get(topk[j]).toString()+"' ");
                }
                deletesql.append(")");
            }
            return deletesql.toString();
        }else{
            return null;
        }
    }
    private static String getUpdateSQL(List<Map<String, Object>> update, String tablename,
                                       String column, String[] topk){
        if(update!=null && update.size() > 0){
            StringBuffer updatesql = new StringBuffer();
            String[] columns = column.split(",");
            String colvalue = "";
            for (int m = 0; m < update.size(); m++) {
                updatesql.append(" update " + tablename + " set ");
                for (int n = 0; n < columns.length; n++) {
                    colvalue = update.get(m).get(columns[n])==null ? "":update.get(m).get(columns[n]).toString();
                    updatesql.append(columns[n] + " = '" + colvalue + "'");
                    updatesql.append(",");
                }
                updatesql.deleteCharAt(updatesql.length() - 1);
                updatesql.append(" where 1=1");
                for (int k = 0; k < topk.length; k++) {
                    colvalue = update.get(m).get(topk[k]) == null ? "" : update.get(m).get(topk[k]).toString();
                    updatesql.append(" and " + topk[k] + " = '" + colvalue + "'"+";");
                }
            }
            return updatesql.toString();
        }else{
            return null;
        }
    }


    //判断主键是否完全相等
    private static boolean isPkEquals(String[] str1, String[] str2) {
        if (str1.length != str2.length) return false;
        boolean flag = true;
        for (int i = 0; i < str1.length; i++) {
            if (!str1[i].equals(str2[i])) flag = false;
        }
        return flag;
    }

    //有不同，返回false
    private static boolean isUpdate(Map<String, Object> fromMap, Map<String, Object> toMap, String column) throws Exception {
        boolean flag = true;
        String[] tos = column.split(",");
        String a = "";
        String b = "";
        for (int i = 0; i < tos.length; i++) {
            a = fromMap.get(tos[i]) == null ? "" : fromMap.get(tos[i]).toString();
            b = toMap.get(tos[i]) == null ? "" : toMap.get(tos[i]).toString();
            if (!a.equals(b)) flag = false;
        }
        return flag;
    }
}
