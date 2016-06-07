package com.cruzj6.mha.models;

import android.content.Context;
import android.provider.ContactsContract;

import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.models.RemovableItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Joey on 6/3/16.
 */
public class PillItem extends RemovableItem {

    private long pillId = -1;
    private String title;
    private String instr;
    private int duration;
    private long untilDate;
    private long refillDate;
    private List<long[]> timesPerDay = new ArrayList<>();

    public PillItem(String title, String instr, int duration, long untilDate, long refillDate)
    {
        this.refillDate = refillDate;
        this.title = title;
        this.instr = instr;
        this.duration = duration;
        this.untilDate = untilDate;

        for(int i = 0; i < 7; i++)
            timesPerDay.add(null);
    }

    public void setTimesForDay(Days day, long[] times)
    {
        timesPerDay.set(day.getNumVal(), times);
    }

    public void setTimesForDay(int day, long[] times)
    {
        timesPerDay.set(day, times);
    }

    public String getTitle()
    {
        return title;
    }

    public String getInstr(){
        return instr;
    }

    public int getDuration()
    {
        return duration;
    }

    public long getUntilDate()
    {
        return untilDate;
    }

    public long[] getTimesForDay(Days day)
    {
        return timesPerDay.get(day.getNumVal());
    }

    public long[] getTimesForDay(int day)
    {
        return timesPerDay.get(day);
    }

    @Override
    public void removeFromDatabase(Context context) {
        new DatabaseManager(context).deletePill(getPillId());
    }

    public long getRefillDate() {
        return refillDate;
    }

    public long getPillId() {
        return pillId;
    }

    public void setPillId(long id){
        pillId = id;
    }
}
