package com.ycd.util.datasyn.vo;

/**
 * Created by yangchd on 2017/7/25.
 * 数据源VO
 */
public class DataSynSourceVO {
    private String pk_datasource;
    private String driver;
    private String url;
    private String basename;
    private String username;
    private String password;

    public String getPk_datasource() {
        return pk_datasource;
    }

    public void setPk_datasource(String pk_datasource) {
        this.pk_datasource = pk_datasource;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBasename() {
        return basename;
    }

    public void setBasename(String basename) {
        this.basename = basename;
    }
}
