package com.ycd.springboot.service.datasyn.impl;

import com.ycd.springboot.service.datasyn.IUpdateService;
import com.ycd.springboot.vo.datasyn.DataSourceVO;
import com.ycd.springboot.vo.datasyn.DataSynTableVO;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangchd on 2017/8/3.
 * 更新服务实现
 */
public class UpdateServiceImpl implements IUpdateService {
    @Override
    public Map<String, Object> updateData(DataSynTableVO tvo, NotCloseDB todao) throws Exception {
        Map<String, Object> rMap = new HashMap<>();

        String syntable = tvo.getTablename();
        //获取需要更新的表以后进行更新
        rMap = new UpdateDataTool().updatetable(syntable, tvo, todao);

        return rMap;
    }


    /**
     * 创建用于同步的表、如果存在，在不操作，不存在，创建
     */
    @Override
    public Map<String, Object> createSynTable(DataSynTableVO tvo, NotCloseDB todao,
                                              DataSourceVO tdvo) throws Exception {
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
                        String createtablea = createtable.substring(0,createtable.indexOf("("));
                        String createtableb = createtable.substring(createtable.indexOf("("),createtable.length());
                        createtablea = createtablea.replaceAll(tablename, tablename + "_syn");
                        createtablea = createtablea.replaceAll("CREATE TABLE", "CREATE TABLE if not EXISTS");
                        createtable = createtablea + createtableb;

                        //在同步表中添加同步标志位
                        String syn = " syn int(10) DEFAULT 0, \n";
                        int insert = createtable.indexOf("PRIMARY");
                        createtable = createtable.substring(0, insert) + syn + createtable.substring(insert);

                        int re = todao.execUpdate(createtable, null);
                        rMap.put("msg", "同步表[" + tablename + "]创建成功");
                    }
                }
            } else if ("oracle.jdbc.driver.OracleDriver".equals(tdvo.getDriver())) {
                tablename = tablename.toUpperCase();
                String exist = "select count(*) as have from user_tables where table_name = '"+tablename+"_SYN'";
                List<Map<String, Object>> list = todao.execQuery(exist,null);
                if(list != null && list.size() == 1){
                    String num = list.get(0).get("have").toString();
                    if("1".equals(num)){
                        //存在
                    }else {
                        //不存在，创建
                        try{
                            String sql = " select dbms_metadata.get_ddl('TABLE','"+tablename+"') as createtable from dual ";
                            List<Map<String, Object>> lst = todao.execQuery(sql, null);
                            if (lst != null && lst.size() > 0) {
                                String createtable = lst.get(0).get("createtable") == null ? "" : lst.get(0).get("createtable").toString();
                                if (createtable != null && !"".equals(createtable)) {
                                    //在这里截断字符串，防止出现表名称和列名相同的情况
                                    String createtablea = createtable.substring(0,createtable.indexOf("("));
                                    String createtableb = createtable.substring(createtable.indexOf("("),createtable.length());
                                    createtablea = createtablea.replaceAll(tablename, tablename + "_SYN");
                                    createtable = createtablea + createtableb;

                                    //后部多余字段删除
                                    createtablea = createtable.substring(0,createtable.indexOf("CONSTRAINT"));
                                    createtableb = createtable.substring(createtable.indexOf("UNIQUE"),createtable.length());
                                    createtableb = createtableb.substring(0,createtableb.indexOf(")"));

                                    String pksql = " select cu.column_name as tablekey from user_cons_columns cu, user_constraints au " +
                                            " where cu.constraint_name = au.constraint_name and au.table_name = '"+tablename+"' " +
                                            " and au.constraint_type = 'U'  ";
                                    lst = todao.execQuery(pksql,null);
                                    pksql = lst.get(0).get("tablekey").toString();

                                    String middle = "CONSTRAINT  \"" + pksql + "\"";

                                    createtable = createtablea + createtableb + ")";

                                    //在同步表中添加同步标志位
                                    String syn = " syn NUMBER(10) DEFAULT 0, \n";
                                    int insert = createtable.indexOf("CONSTRAINT");
                                    createtable = createtable.substring(0, insert) + syn + createtable.substring(insert);

//                                int re = todao.execUpdate(createtable, null);
//                                rMap.put("msg", "同步表[" + tablename + "]创建成功");
                                }
                            }
                        }catch (Exception e){
                            throw new Exception("创建同步表["+tablename+"]失败，请手动创建！");
                        }

                    }
                }else{
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
    @Override
    public Map<String, Object> transferTableSyn(DataSynTableVO tvo, NotCloseDB todao, NotCloseDB fromdao) throws Exception {

        int SIZE = 1000;//设置同步单次执行最大条数
        Map<String, Object> rMap = new HashMap<>();

        String pk = tvo.getTablekey();
        String column = tvo.getAllcolumn();
        String from = tvo.getFromtables();
        String to = tvo.getTablename() + "_syn";
        JSONObject relation = JSONObject.fromObject(tvo.getRelation());

        String fromcolumn = new UpdateDataTool().changeColumn(column, relation, "from");
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
                    sqlsb.append(" insert into " + to + "(" + new UpdateDataTool().changeColumn(column, relation, "to") + ") values  ");
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
                sqlsb.append(" insert into " + to + "(" + new UpdateDataTool().changeColumn(column, relation, "to") + ") values  ");
                sb.deleteCharAt(sb.length() - 1);
                sqlsb.append(sb.toString());
                sqlsb.append(";  ");
                re = todao.execUpdate(sqlsb.toString(), null);
            }
        }
        rMap.put("msg", "成功更新中间表数据");
        return rMap;
    }
}
