package cn.lzx.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class DataTimeUtil {
    private static final String DEFAULT_TIME_PATTERN="HH:mm:ss";

    public static String getCurrentTime(){
        return getCurrentTime(DEFAULT_TIME_PATTERN);
    }

    public static String getCurrentTime(String format){
        SimpleDateFormat sdf =new SimpleDateFormat(format);
        Timestamp ts=new Timestamp(System.currentTimeMillis());
        return sdf.format(ts);
    }
}
