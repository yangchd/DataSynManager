package com.yangchd.data.manage.datasyn.dao.vo;

/**
 * Created by yangchd on 2017/8/1.
 * 数据同步总表
 */
public class DataSynVO {
    private String pk_sync;
    private String pk_datafrom;
    private String pk_datato;
    private String pk_syntable;
    private String flag;
    private String lasttime;
    private String timecost;
    private String datasynmsg;

    public String getPk_sync() {
        return pk_sync;
    }

    public void setPk_sync(String pk_sync) {
        this.pk_sync = pk_sync;
    }

    public String getPk_datafrom() {
        return pk_datafrom;
    }

    public void setPk_datafrom(String pk_datafrom) {
        this.pk_datafrom = pk_datafrom;
    }

    public String getPk_datato() {
        return pk_datato;
    }

    public void setPk_datato(String pk_datato) {
        this.pk_datato = pk_datato;
    }

    public String getPk_syntable() {
        return pk_syntable;
    }

    public void setPk_syntable(String pk_syntable) {
        this.pk_syntable = pk_syntable;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getLasttime() {
        return lasttime;
    }

    public void setLasttime(String lasttime) {
        this.lasttime = lasttime;
    }

    public String getTimecost() {
        return timecost;
    }

    public void setTimecost(String timecost) {
        this.timecost = timecost;
    }

    public String getDatasynmsg() {
        return datasynmsg;
    }

    public void setDatasynmsg(String datasynmsg) {
        this.datasynmsg = datasynmsg;
    }
}
