package com.yangchd.data.manage.datasyn.impl;

import com.yangchd.data.manage.datasyn.dao.DaoTool;
import com.yangchd.data.manage.datasyn.dao.db.DBService;
import com.yangchd.data.manage.datasyn.dao.db.NotCloseDB;
import com.yangchd.data.manage.datasyn.dao.vo.DataSynSourceVO;
import com.yangchd.data.manage.datasyn.dao.vo.DataSynTableVO;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangchd on 2017/8/3.
 * 更新工具类
 */
public class UpdateDataTool {

    /**
     * 开始进行数据同步，根据数据同步配置信息Map
     * flag 是否使用同步表
     * by yangchd 2017-08-28
     */
    public void startDataSynByMap(Map<String,Object> synMap,String flag) throws Exception {
        Map<String,Object> rMap = new HashMap<>();
        String msg = "";

        //获取目标库和源库的连接
        DataSynSourceVO fdvo = new DataSynSourceVO();
        DataSynSourceVO tdvo = new DataSynSourceVO();

        fdvo.setPk_datasource(synMap.get("pk_datafrom").toString());
        tdvo.setPk_datasource(synMap.get("pk_datato").toString());
        NotCloseDB fromdao = null;
        NotCloseDB todao = null;
        try {
            fromdao = new DaoTool().getNotCloseDB(fdvo);
            todao = new DaoTool().getNotCloseDB(tdvo);
        }catch (Exception e){
            if(fromdao != null){
                fromdao.destory();
            }
            if(todao != null){
                todao.destory();
            }
            throw new Exception("数据源获取失败："+e.getMessage());
        }
        try {
            //获取完整的同步信息
            DataSynTableVO tvo = new DataSynTableVO();
            tvo.setPk_table(synMap.get("pk_syntable").toString());
            tvo = getDataSynTableVO(tvo);

            if("true".equals(flag)){
                new UpdateTableByMiddle().UpdateTable(tvo,tdvo,fromdao,todao);
            }else{
                new UpdateTableByNoMiddle().UpdateTable(tvo,fromdao,todao);
            }
        }finally {
            //执行完操作以后，关闭连接
            if(fromdao != null){
                fromdao.destory();
            }
            if(todao != null){
                todao.destory();
            }
        }
    }


    /**
     * 根据同步表主键，解析同步表配置信息
     * by yangchd 2017-08-28
     */
    private static DataSynTableVO getDataSynTableVO(DataSynTableVO tvo) throws Exception {
        String pk_table = tvo.getPk_table();
        if(pk_table != null && !"".equals(pk_table)){
            DBService dao = new DBService();
            String sql = "select * from syn_table where pk_table = '"+pk_table+"'";
            List<Map<String,Object>> list = dao.execQuery(sql,null);
            if(list!=null && list.size() == 1){
                tvo.setTablename(list.get(0).get("tablename")==null?"":list.get(0).get("tablename").toString());
                tvo.setFromtables(list.get(0).get("fromtables")==null?"":list.get(0).get("fromtables").toString());
                tvo.setTablesrelation(list.get(0).get("tablesrelation")==null?"":list.get(0).get("tablesrelation").toString());

                //对表的主键、列、关联关系进行转小写
                tvo.setTablekey(list.get(0).get("tablekey")==null?"":list.get(0).get("tablekey").toString().toLowerCase());
                tvo.setAllcolumn(list.get(0).get("allcolumn")==null?"":list.get(0).get("allcolumn").toString().toLowerCase());
                tvo.setRelation(list.get(0).get("relation")==null?"":list.get(0).get("relation").toString().toLowerCase());
                tvo.setWherevalue(list.get(0).get("wherevalue")==null?"":list.get(0).get("wherevalue").toString().toLowerCase());
                //去除多余列信息
                tvo.setAllcolumn(getColumn(tvo,"to"));
            }else {
                throw new Exception("配置的同步信息有误，请检查");
            }
        }else{
            throw new Exception("未找到配置的同步信息，请检查配置");
        }
        return tvo;
    }

    /**
     * 转换column为所需的格式，方便查询和插入
     * by yangchd 20170301
     * 2017-08-30修改 适配数据库别名冲突问题
     */
    static String getColumn(DataSynTableVO tvo, String type) {
        String[] str = tvo.getAllcolumn().split(",");
        JSONObject relation = JSONObject.fromObject(tvo.getRelation());
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
     * 根据数据库获取sql语句的列名称，防止出现命名问题
     * com.mysql.jdbc.Driver
     * oracle.jdbc.driver.OracleDriver
     * com.microsoft.sqlserver.jdbc.SQLServerDriver
     * com.microsoft.jdbc.sqlserver.SQLServerDriver
     */
    static String getColumnSQL(DataSynTableVO tvo,NotCloseDB dao,String type) {
        String[] str = tvo.getAllcolumn().split(",");
        JSONObject relation = JSONObject.fromObject(tvo.getRelation());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length; i++) {
            String from = relation.getString(str[i]);
            if (from != null && !"".equals(from)) {
                if("as".equals(type)){
                    if ("com.mysql.jdbc.Driver".equals(dao.getDriver())) {
                        str[i] = addSymbol(from,dao) +" as "+
                                addSymbol(str[i],dao);
                    }
                    if("oracle.jdbc.driver.OracleDriver".equals(dao.getDriver())){
                        str[i] = addSymbol(from,dao) +" as "+
                                addSymbol(str[i],dao);
                    }
                    if("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(dao.getDriver())
                            ||"com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(dao.getDriver())){
                        str[i] = addSymbol(from,dao) +" as "+
                                addSymbol(str[i],dao);
                    }
                }else{
                    if ("com.mysql.jdbc.Driver".equals(dao.getDriver())) {
                        str[i] = addSymbol(str[i],dao);
                    }
                    if("oracle.jdbc.driver.OracleDriver".equals(dao.getDriver())){
                        str[i] = addSymbol(str[i],dao);
                    }
                    if("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(dao.getDriver())
                            ||"com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(dao.getDriver())){
                        str[i] = addSymbol(str[i],dao);
                    }
                }
                sb.append(str[i]);
                sb.append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * 根据数据库，改变列格式
     */
    static String addSymbol(String column,NotCloseDB dao){
        String symbolleft = "";
        String symbolright = "";
        if ("com.mysql.jdbc.Driver".equals(dao.getDriver())) {
            symbolleft = "`";symbolright="`";
        }
        if("oracle.jdbc.driver.OracleDriver".equals(dao.getDriver())){
            symbolleft = "";symbolright="";
        }
        if("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(dao.getDriver())
                ||"com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(dao.getDriver())){
            symbolleft = "[";symbolright="]";
        }
        StringBuffer re = new StringBuffer();
        if(null != column){
            String columns[] = column.split("\\.");
            for (String a:columns){
                a = symbolleft+a+symbolright;
                re.append(a+".");
            }
            if(re.length()>0)re.deleteCharAt(re.length()-1);
            return re.toString();
        }else{
            return null;
        }
    }


    static String changeColumn(String column, JSONObject relation, String type) {
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
    static String getColValue(Map<String, Object> rMap, String column) {
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
            sb.append( rMap.get(str[i].toUpperCase())==null?(rMap.get(str[i].toLowerCase())==null?"":rMap.get(str[i].toLowerCase()).toString()):rMap.get(str[i].toUpperCase()).toString() );
            sb.append("',");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("),");
        return sb.toString();
    }

    /**
     * 获取插入的参数
     */
    static Object[] getObjectValue(Map<String, Object> rMap, String column) {
        String[] str = column.split(",");
        Object[] obj = new Object[str.length];
        for (int i = 0; i < str.length; i++) {
            if (str[i].indexOf(".") > 0) {
                str[i] = str[i].substring(str[i].lastIndexOf(".")+1, str[i].length());
            }
            obj[i] = rMap.get(str[i]);
        }
        return obj;
    }

}
