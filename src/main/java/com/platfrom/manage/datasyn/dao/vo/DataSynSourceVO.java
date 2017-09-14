package com.platfrom.manage.datasyn.dao.vo;

/**
 * Created by yangchd on 2017/7/25.
 * 数据源VO
 */
public class DataSynSourceVO {
    private String pk_datasource;
    private String datasourcename;
    private String url;
    private String username;
    private String password;
    private String basename;
    private String driver;
    private String url_ip;
    private String url_port;

    public String getPk_datasource() {
        return pk_datasource;
    }

    public void setPk_datasource(String pk_datasource) {
        this.pk_datasource = pk_datasource;
    }

    public String getDatasourcename() {
        return datasourcename;
    }

    public void setDatasourcename(String datasourcename) {
        this.datasourcename = datasourcename;
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

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl_ip() {
        return url_ip;
    }

    public void setUrl_ip(String url_ip) {
        this.url_ip = url_ip;
    }

    public String getUrl_port() {
        return url_port;
    }

    public void setUrl_port(String url_port) {
        this.url_port = url_port;
    }
}
