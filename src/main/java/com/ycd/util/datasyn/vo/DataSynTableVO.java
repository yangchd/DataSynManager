package com.ycd.util.datasyn.vo;

/**
 * Created by yangchd on 2017/8/1.
 * 数据同步表VO
 */
public class DataSynTableVO {
    private String pk_table;
    private String tablename;
    private String tablekey;
    private String fromtables;
    private String tablesrelation;
    private String allcolumn;
    private String relation;

    public String getPk_table() {
        return pk_table;
    }

    public void setPk_table(String pk_table) {
        this.pk_table = pk_table;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public String getFromtables() {
        return fromtables;
    }

    public void setFromtables(String fromtables) {
        this.fromtables = fromtables;
    }

    public String getTablesrelation() {
        return tablesrelation;
    }

    public void setTablesrelation(String tablesrelation) {
        this.tablesrelation = tablesrelation;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getTablekey() {
        return tablekey;
    }

    public void setTablekey(String tablekey) {
        this.tablekey = tablekey;
    }

    public String getAllcolumn() {
        return allcolumn;
    }

    public void setAllcolumn(String allcolumn) {
        this.allcolumn = allcolumn;
    }
}
