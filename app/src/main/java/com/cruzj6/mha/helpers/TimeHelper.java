package com.cruzj6.mha.helpers;

import com.cruzj6.mha.models.PillItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Joey on 6/6/16.
 * Convinience methods for managing unix times from the database
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

    public static boolean compareUnixDatesTimes(long unix1, long unix2)
    {
        if(unix1 == unix2) return true;
        else
        {
            SimpleDateFormat f = new SimpleDateFormat("hh:mm aaa");
            Date d1 = new Date(unix1*1000);
            Date d2 = new Date(unix2*1000);
            long h1, m1, s1;
            String str1 = f.format(d1);
            String str2 = f.format(d2);
            if(!str1.equals(str2)) return false;
            else return true;
        }

    }

    public static boolean checkSameTimesEachDay(PillItem item){
        boolean diffTimes = false;
        long[] lastDaysTimes = null;
        long[] times = null;
        long[] cacheTimes = null;
        int numDays = 0;
        for(int i = 0; i < 7; i++)
        {
            times = item.getTimesForDay(i);
            if (times != null) {
                numDays++;

                if (cacheTimes == null) cacheTimes = times;
                //If we have not determined if these times differ yet, and we have something to compare
                if (!diffTimes && lastDaysTimes != null) {
                    //If different amount of times they do have differing times per day
                    if (lastDaysTimes.length != times.length) {
                        diffTimes = true;
                    } else {//If same amount check if they differ
                        for (int j = 0; j < times.length; j++) {

                            //These should be sorted
                            if (!TimeHelper.compareUnixDatesTimes(times[j], lastDaysTimes[j])) {
                                diffTimes = true;
                            }
                        }
                    }
                }
            }
            lastDaysTimes = times;
        }

        return !diffTimes;
    }
}
