package com.cruzj6.mha.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Joey on 7/11/16.
 * This class manages times for medications (PillItem objects)
 * from the database into managable object with simple
 * day, hour, and minute data
 */
public class TimesPerDayManager {
    private List<TimesPerDayManagerItem> timesPerDay;

    public TimesPerDayManager(List<long[]> timesPerDay)
    {
        this.timesPerDay = new ArrayList<>();
        for(int i = 0; i < 7; i++)
        {
            this.timesPerDay
                    .add(new TimesPerDayManagerItem(Days.values()[i], timesPerDay.get(i)));
        }

        //Sort by day
        Collections.sort(this.timesPerDay);
    }

    public List<TimesPerDayManagerItem> getTimesPerDay()
    {
        return timesPerDay;
    }
}
