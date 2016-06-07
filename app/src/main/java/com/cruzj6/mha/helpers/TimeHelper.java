package com.cruzj6.mha.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Joey on 6/6/16.
 */
public final class TimeHelper {

    private TimeHelper(){}

    public static long getTimeUnix(int h, int m, int s)
    {
        //Start at 0
        Date setDate = new Date(0);
        Calendar c = Calendar.getInstance();
        c.setTime(setDate);
        c.set(Calendar.MINUTE, m);
        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.SECOND, s);

        long time = c.getTimeInMillis()/1000L;
        return time;
    }

    public static boolean compareUnixDateTimes(long unix1, long unix2)
    {
        if(unix1 == unix2) return true;
        else
        {
            SimpleDateFormat f = new SimpleDateFormat("hh:mm aaa");
            Date d1 = new Date(unix1);
            Date d2 = new Date(unix2);
            long h1, m1, s1;
            String str1 = f.format(d1);
            String str2 = f.format(d2);
            if(!str1.equals(str2)) return false;
            else return true;
        }

    }
}
