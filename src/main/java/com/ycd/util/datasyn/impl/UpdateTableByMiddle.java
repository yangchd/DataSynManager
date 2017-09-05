package com.ycd.util.datasyn.impl;

import com.ycd.util.datasyn.dao.db.NotCloseDB;
import com.ycd.util.datasyn.dao.vo.DataSynSourceVO;
import com.ycd.util.datasyn.dao.vo.DataSynTableVO;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangchd on 2017/8/29.
 * 有中间表模式同步
 */
public class UpdateTableByMiddle {

    public Map<String, Object> UpdateTable(DataSynTableVO tvo, DataSynSourceVO tdvo, NotCloseDB fromdao, NotCloseDB todao) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        //建立同步表
        createSynTable(tvo, todao, tdvo);

        //更新同步表数据
        transferTableSyn(tvo, todao, fromdao);

        //数据更新, 无中间表模式
        doUpdate(tvo, todao);

        rMap.put("retflag", "0");
        rMap.put("msg", "同步成功！");
        return rMap;
    }

    /**
     * 创建用于同步的表、如果存在，在不操作，不存在，创建
     */
    public Map<String, Object> createSynTable(DataSynTableVO tvo, NotCloseDB todao,
                                              DataSynSourceVO tdvo) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        String tablename = tvo.getTablename();
        //通过读取的配置文件，创建先关表的_syn表，用来同步数据用
        if (tablename != null && !"".equals(tablename)) {
            if ("com.mysql.jdbc.Driver".equals(tdvo.getDriver())) {
                String sql = "show create table " + tablename;
                List<Map<String, Object>> lst = todao.execQuery(sql, null);
                if (lst != null && lst.size() > 0) {
                    String createtable = lst.get(0).get("create table") == null ? "" : lst.get(0).get("create table").toString();
                    if (createtable != null && !"".equals(createtable)) {
                        //在这里截断字符串，防止出现表名称和列名相同的情况
                        String createtablea = createtable.substring(0, createtable.indexOf("("));
                        String createtableb = createtable.substring(createtable.indexOf("("), createtable.length());
                        createtablea = createtablea.replaceAll(tablename, tablename + "_syn");
                        createtablea = createtablea.replaceAll("CREATE TABLE", "CREATE TABLE if not EXISTS");
                        createtable = createtablea + createtableb;

                        int re = todao.execUpdate(createtable, null);
                        rMap.put("msg", "同步表[" + tablename + "]创建成功");
                    }
                }
            } else if ("oracle.jdbc.driver.OracleDriver".equals(tdvo.getDriver())) {
                tablename = tablename.toUpperCase();
                String exist = "select count(*) as have from user_tables where table_name = '" + tablename + "_SYN'";
                List<Map<String, Object>> list = todao.execQuery(exist, null);
                if (list != null && list.size() == 1) {
                    String num = list.get(0).get("have").toString();
                    if ("1".equals(num)) {
                        //存在
                    } else {
                        //不存在，创建
                        try {
                            String sql = " select dbms_metadata.get_ddl('TABLE','" + tablename + "') as createtable from dual ";
                            List<Map<String, Object>> lst = todao.execQuery(sql, null);
                            if (lst != null && lst.size() > 0) {
                                String createtable = lst.get(0).get("createtable") == null ? "" : lst.get(0).get("createtable").toString();
                                if (createtable != null && !"".equals(createtable)) {
                                    //在这里截断字符串，防止出现表名称和列名相同的情况
                                    String createtablea = createtable.substring(0, createtable.indexOf("("));
                                    String createtableb = createtable.substring(createtable.indexOf("("), createtable.length());
                                    createtablea = createtablea.replaceAll(tablename, tablename + "_SYN");
                                    createtable = createtablea + createtableb;

                                    //后部多余字段删除
                                    createtablea = createtable.substring(0, createtable.indexOf("CONSTRAINT"));
                                    createtableb = createtable.substring(createtable.indexOf("UNIQUE"), createtable.length());
                                    createtableb = createtableb.substring(0, createtableb.indexOf(")"));

                                    String pksql = " select cu.column_name as tablekey from user_cons_columns cu, user_constraints au " +
                                            " where cu.constraint_name = au.constraint_name and au.table_name = '" + tablename + "' " +
                                            " and au.constraint_type = 'U'  ";
                                    lst = todao.execQuery(pksql, null);
                                    pksql = lst.get(0).get("tablekey").toString();

                                    String middle = "CONSTRAINT  \"" + pksql + "\"";

                                    createtable = createtablea + createtableb + ")";

//                                int re = todao.execUpdate(createtable, null);
//                                rMap.put("msg", "同步表[" + tablename + "]创建成功");
                                }
                            }
                        } catch (Exception e) {
                            throw new Exception("创建同步表[" + tablename + "]失败，请手动创建！");
                        }

                    }
                } else {
                    throw new Exception("检测同步表时出错");
                }
            } else {
                throw new Exception("该类型的数据库暂时未适配");
            }
        }
        return rMap;
    }

    /**
     * 更新数据到_syn表中
     */
    public Map<String, Object> transferTableSyn(DataSynTableVO tvo, NotCloseDB todao, NotCloseDB fromdao) throws Exception {

        int SIZE = 1000;//设置同步单次执行最大条数
        Map<String, Object> rMap = new HashMap<>();

        String pk = tvo.getTablekey();
        String column = tvo.getAllcolumn();
        String from = tvo.getFromtables();
        String to = tvo.getTablename() + "_syn";
        JSONObject relation = JSONObject.fromObject(tvo.getRelation());

        String fromcolumn = UpdateDataTool.getColumn(tvo, "from");
        //在插入之前，清除原有数据
        String deletesql = " delete from " + to;
        int re = todao.execUpdate(deletesql, null);

        //查询导出表所有需要更新的信息
        StringBuffer sql = new StringBuffer();
        String[] fromtables = from.split(",");
        if (fromtables.length == 1) {
            //单表时不处理，多表时会进行多表关联
        } else {
            String on = tvo.getTablesrelation();
            if (on != null && !"".equals(on)) {
                StringBuffer ft = new StringBuffer();
                String[] ons = on.split(",");
                ft.append(" " + fromtables[0] + " ");
                for (int m = 0; m < ons.length; m++) {
                    ft.append(" left join " + fromtables[m + 1] + " on " + ons[m]);
                }
                from = ft.toString();
            } else {
                throw new Exception("多表关联信息配置有误");
            }
        }

        String[] pkstr = pk.split(",");
        String[] frompk = new String[pkstr.length];
        for (int p = 0; p < pkstr.length; p++) {
            frompk[p] = relation.getString(pkstr[p]);
        }
        sql.append("select DISTINCT " + fromcolumn + " from " + from);
        sql.append(" order by ");
        for (int k = 0; k < frompk.length; k++) {
            sql.append(frompk[k] + ",");
        }
        sql.deleteCharAt(sql.length() - 1);
        List<Map<String, Object>> updatelist = fromdao.execQuery(sql.toString(), null);
        if (updatelist != null && updatelist.size() > 0) {
            //去掉主键重复

            int num = 0;//设置一个计数器2000条执行一次插入，防止数据太多
            StringBuffer sb = new StringBuffer();
            for (Map<String, Object> anUpdatelist : updatelist) {
                if (num == SIZE) {
                    //到达一定条数，先插入
                    sb.append(new UpdateDataTool().getColValue(anUpdatelist, fromcolumn));
                    sb.deleteCharAt(sb.length() - 1);
                    StringBuffer sqlsb = new StringBuffer();
                    sqlsb.append(" insert into " + to + "(" + UpdateDataTool.getColumn(tvo, "to") + ") values  ");
                    sqlsb.append(sb.toString());
                    sqlsb.append(";  ");
                    re = todao.execUpdate(sqlsb.toString(), null);
                    sb.setLength(0);
                    num = 0;
                } else {
                    sb.append(new UpdateDataTool().getColValue(anUpdatelist, fromcolumn));
                    num++;
                }
            }
            if (num > 0 && num < SIZE) {//插入最后不到整数的一些数据
                StringBuffer sqlsb = new StringBuffer();
                sqlsb.append(" insert into " + to + "(" + UpdateDataTool.getColumn(tvo, "to") + ") values  ");
                sb.deleteCharAt(sb.length() - 1);
                sqlsb.append(sb.toString());
                sqlsb.append(";  ");
                re = todao.execUpdate(sqlsb.toString(), null);
            }
        }
        rMap.put("msg", "成功更新中间表数据");
        return rMap;
    }

    /**
     * 更新表中所有信息
     */
    private Map<String, Object> doUpdate(DataSynTableVO tvo, NotCloseDB todao) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        //参数初始化
        String tablename = tvo.getTablename();
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
    private void insertData(NotCloseDB todao, String tablename, String column, String pk, JSONObject relation) throws Exception {

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
            column = UpdateDataTool.changeColumn(column, relation, "to");
            for (int j = 0; j < insert.size(); j++) {
                if (num == SIZE) {
                    //到达一定条数，先插入
                    sb.append(UpdateDataTool.getColValue(insert.get(j), column));
                    sb.deleteCharAt(sb.length() - 1);
                    StringBuffer sqlsb = new StringBuffer();
                    sqlsb.append("insert into " + tablename + "(" + UpdateDataTool.changeColumn(column, relation, "to") + ") values  ");
                    sqlsb.append(sb.toString());
                    todao.execUpdate(sqlsb.toString(), null);
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
                sqlsb.append("insert into " + tablename + "(" + UpdateDataTool.changeColumn(column, relation, "to") + ") values  ");
                sqlsb.append(sb.toString());
                todao.execUpdate(sqlsb.toString(), null);
            }
        }
    }


    private void deleteData(NotCloseDB todao, String tablename, String column, String pk, JSONObject relation) throws Exception {

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

    private void updateData(NotCloseDB todao, String tablename, String column, String pk, JSONObject relation) throws Exception {

        column = UpdateDataTool.changeColumn(column, relation, "to");
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
