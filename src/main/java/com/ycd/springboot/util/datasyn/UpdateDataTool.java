package com.ycd.springboot.util.datasyn;

import com.ycd.springboot.util.datasyn.dao.DaoTool;
import com.ycd.springboot.util.datasyn.dao.NotCloseDB;
import com.ycd.springboot.vo.datasyn.DataSynSourceVO;
import com.ycd.springboot.vo.datasyn.DataSynTableVO;
import com.ycd.springboot.util.db.DBService;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangchd on 2017/8/3.
 * 更新工具类
 */
public class UpdateDataTool {

    private static int SQL_SIZE = 500;

    /**
     * 开始进行数据同步，根据数据同步配置信息Map
     * flag 是否使用同步表
     * by yangchd 2017-08-28
     */
    public Map<String,Object> startDataSynByMap(Map<String,Object> synMap,String flag) throws Exception {
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
            throw new Exception(e.getMessage());
        }
        //获取完整的同步信息
        DataSynTableVO tvo = new DataSynTableVO();
        tvo.setPk_table(synMap.get("pk_syntable").toString());
        tvo = getDataSynTableVO(tvo);

        if("true".equals(flag)){
            new UpdateTableByMiddle().UpdateTable(tvo,tdvo,fromdao,todao);
        }else{
            new UpdateTableByNoMiddle().UpdateTable(tvo,fromdao,todao);
        }
        fromdao.destory();
        todao.destory();
        rMap.put("retflag","0");
        rMap.put("msg",msg);
        return rMap;
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
                tvo.setTablekey(list.get(0).get("tablekey")==null?"":list.get(0).get("tablekey").toString());
                tvo.setAllcolumn(list.get(0).get("allcolumn")==null?"":list.get(0).get("allcolumn").toString());
                tvo.setFromtables(list.get(0).get("fromtables")==null?"":list.get(0).get("fromtables").toString());
                tvo.setRelation(list.get(0).get("relation")==null?"":list.get(0).get("relation").toString());
                tvo.setTablesrelation(list.get(0).get("tablesrelation")==null?"":list.get(0).get("tablesrelation").toString());
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
     * type = from 为from表对应列的名称
     * type = to 为to对应表的列名
     */
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
                if ("as".equals(type)) {
                    str[i] = from +" as "+ str[i];
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

}
