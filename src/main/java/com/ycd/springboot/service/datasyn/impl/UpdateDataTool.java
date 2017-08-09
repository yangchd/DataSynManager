package com.ycd.springboot.service.datasyn.impl;

import com.ycd.springboot.vo.datasyn.DataSynTableVO;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangchd on 2017/8/3.
 * 更新工具类
 */
public class UpdateDataTool {
    /**
     * 转换column为所需的格式，方便查询和插入
     * by yangchd 20170301
     * type = from 为from表对应列的名称
     * type = to 为to对应表的列名
     */
    public String changeColumn(String column, JSONObject relation, String type) {
        String[] str = column.split(",");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length; i++) {
            String from = relation.getString(str[i]);
            if (from != null && !"".equals(from)) {
                if ("from".equals(type)) {
                    str[i] = from;
                }
                if ("to".equals(type)) {
                    //什么都不做
                }
                sb.append(str[i]);
                sb.append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * 根据数据和列名获对应的字段，没有则为空
     * by yangchd 20170301
     * 返回('a','b')形式的数据，暂时定为insert用
     */
    public String getColValue(Map<String, Object> rMap, String column) {
        String[] str = column.split(",");
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        for (int i = 0; i < str.length; i++) {
            if (str[i].indexOf(".") > 0) {
                int a = str[i].indexOf(".") + 1;
                int b = str[i].length();
                str[i] = str[i].substring(a, b);
            }
            sb.append("'");
            sb.append(rMap.get(str[i]) == null ? "" : rMap.get(str[i]).toString());
            sb.append("',");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("),");
        return sb.toString();
    }

    /**
     * 更新表函数，传表名
     */
    public Map<String, Object> updatetable(String syntable, DataSynTableVO tvo, NotCloseDB todao) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        //参数初始化
        String tablename = syntable;
        String column = tvo.getAllcolumn();
        String pk = tvo.getTablekey();
        JSONObject relation = JSONObject.fromObject(tvo.getRelation());
        String[] col = column.split(",");
        String[] pkstr = pk.split(",");
        if (col.length < 0 || pkstr.length < 0) {
            throw new Exception("配置信息有误！请检查同步配置！\r\n");
        }

        //全表同步操作 先做插入、再做删除、最后做更新操作
        insertData(todao, tablename, column, pk, relation);

        deleteData(todao, tablename, column, pk, relation);

        updateData(todao, tablename, column, pk, relation);

        return rMap;
    }


    /**
     * 将同步表中新出现的数据插入相关表中
     */
    public void insertData(NotCloseDB todao, String tablename, String column, String pk, JSONObject relation) throws Exception {

        String[] pkstr = pk.split(",");

        StringBuffer sql = new StringBuffer();
        sql.append(" select * from " + tablename + "_syn a where not EXISTS (");
        sql.append(" select * from " + tablename + " b where 1=1");
        for (int i = 0; i < pkstr.length; i++) {
            sql.append(" and a." + pkstr[i] + " = b." + pkstr[i]);
        }
        sql.append(")");

        List<Map<String, Object>> insert = todao.execQuery(sql.toString(), null);
        if (insert != null && insert.size() > 0) {
            //有数据 准备插入
            int SIZE = 1000;
            int num = 0;
            StringBuffer sb = new StringBuffer();
            column = changeColumn(column, relation, "to");
            for (int j = 0; j < insert.size(); j++) {
                if (num == SIZE) {
                    //到达一定条数，先插入
                    sb.append(getColValue(insert.get(j), column));
                    sb.deleteCharAt(sb.length() - 1);
                    StringBuffer sqlsb = new StringBuffer();
                    sqlsb.append("insert into " + tablename + "(" + changeColumn(column, relation, "to") + ") values  ");
                    sqlsb.append(sb.toString());
                    todao.execUpdate(sqlsb.toString(), null);
                    sb.setLength(0);
                    num = 0;
                } else {
                    sb.append(getColValue(insert.get(j), column));
                    num++;
                }
            }
            if (num > 0 && num < SIZE) {//插入最后不到整数的一些数据
                StringBuffer sqlsb = new StringBuffer();
                sb.deleteCharAt(sb.length() - 1);
                sqlsb.append("insert into " + tablename + "(" + changeColumn(column, relation, "to") + ") values  ");
                sqlsb.append(sb.toString());
                todao.execUpdate(sqlsb.toString(), null);
            }
        }
    }


    public void deleteData(NotCloseDB todao, String tablename, String column, String pk, JSONObject relation) throws Exception {

        String[] pkstr = pk.split(",");
        StringBuffer sql = new StringBuffer();
        sql.append(" delete from  " + tablename + "  where not EXISTS (");
        sql.append(" select * from " + tablename + "_syn  where 1=1");
        for (int i = 0; i < pkstr.length; i++) {
            sql.append(" and " + tablename + "." + pkstr[i] + " = " + tablename + "_syn." + pkstr[i]);
        }
        sql.append(")");
        todao.execUpdate(sql.toString(), null);
    }

    public void updateData(NotCloseDB todao, String tablename, String column, String pk, JSONObject relation) throws Exception {

        column = new UpdateDataTool().changeColumn(column, relation, "to");
        String[] str = column.split(",");
        String[] pkstr = pk.split(",");
        StringBuffer sb = new StringBuffer();
        sb.append(" select ");
        for (int i = 0; i < str.length; i++) {
            sb.append(" a." + str[i] + " as " + str[i] + "a,");
            sb.append(" b." + str[i] + " as " + str[i] + "b,");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" from " + tablename + " a left join " + tablename + "_syn b on");
        for (int j = 0; j < pkstr.length; j++) {
            sb.append(" a." + pkstr[j] + " = b." + pkstr[j]);
            sb.append(" and ");
        }
        sb.delete(sb.length() - 5, sb.length());
//        sb.append(" where 1=1 ");
        List<Map<String, Object>> update = new ArrayList<>();
        List<Map<String, Object>> list = todao.execQuery(sb.toString(), null);
        String a = "";
        String b = "";
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < str.length; j++) {
                    a = list.get(i).get(str[j] + "a") == null ? "" : list.get(i).get(str[j] + "a").toString();
                    b = list.get(i).get(str[j] + "b") == null ? "" : list.get(i).get(str[j] + "b").toString();
                    if (!a.equals(b)) {
                        //获取需要更新的数据
                        update.add(list.get(i));
                    }
                }
            }
        }

        StringBuffer up = new StringBuffer();
        for (int m = 0; m < update.size(); m++) {
            up.setLength(0);
            up.append(" update " + tablename + " set ");
            for (int n = 0; n < str.length; n++) {
                b = update.get(m).get(str[n] + "b") == null ? "" : update.get(m).get(str[n] + "b").toString();
                up.append(str[n] + " = '" + b + "'");
                up.append(",");
            }
            up.deleteCharAt(up.length() - 1);
            up.append(" where 1=1");
            for (int k = 0; k < pkstr.length; k++) {
                b = update.get(m).get(pkstr[k] + "a") == null ? "" : update.get(m).get(pkstr[k] + "a").toString();
                up.append(" and " + pkstr[k] + " = '" + b + "'");
            }
            todao.execUpdate(up.toString(), null);
        }
    }
}
