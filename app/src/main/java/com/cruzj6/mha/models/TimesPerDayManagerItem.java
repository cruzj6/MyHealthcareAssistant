package com.cruzj6.mha.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Joey on 7/11/16.
 *
 */
public class TimesPerDayManagerItem implements Comparable<TimesPerDayManagerItem>{
    private Days day;
    private List<SimpleTimeItem> timesList;

    public TimesPerDayManagerItem(Days day, long[] times)
    {
        this.day = day;
        Calendar c = Calendar.getInstance();
        Date d = new Date();
        this.timesList = new ArrayList<>();
        if(times != null) {
            for (long time : times) {
                d.setTime(time);
                SimpleDateFormat f = new SimpleDateFormat();
                f.applyPattern("HH");
                int hours = Integer.parseInt(f.format(d));
                f.applyPattern("mm");
                int mins = Integer.parseInt(f.format(d));
                SimpleTimeItem tItem = new SimpleTimeItem(hours, mins);
                timesList.add(tItem);
            }
        }
        //Sort the times
        Collections.sort(timesList);
    }

    public Days getDay()
    {
        return day;
    }

    public List<SimpleTimeItem> getTimesList()
    {
        return timesList;
    }

    @Override
    public int compareTo(TimesPerDayManagerItem another) {
        int aDayNum = another.getDay().getNumVal();
        int thisDayNum = this.day.getNumVal();
        return Integer.compare(thisDayNum, aDayNum);
    }
}
