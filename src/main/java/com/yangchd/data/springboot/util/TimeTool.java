package com.yangchd.data.springboot.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 这个类用来处理各种跟时间有关的方法
 * @author yangchd
 */
public class TimeTool {

    //当时间为空时，默认时间
    public static String TIME_DEFAULT = "2000-01-01 00:00:00";

    //时间的格式
    public static String TIME_FORMAT_19 = "yyyy-MM-dd HH:mm:ss";
    public static String TIME_FORMAT_10 = "yyyy-MM-dd";

    /**
     * 计算日期时间差
     * @return 返回相差多少毫秒
     */
    public static int difTimeByDate(Date date1,Date date2 ){
        return (int) ((date2.getTime()-date1.getTime()));
    }

    /**
     * 计算是否在时间差内
     * 传入时间和相差秒
     */
    public static boolean isOverTimeBySec(String time,int timeInterval) throws ParseException{
        Date now = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date last = sf.parse(time);
        int second = (int) ((now.getTime()-last.getTime())/(1000));
        return second < timeInterval;
    }

}
