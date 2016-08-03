package com.cruzj6.mha.models;

import android.content.Context;
import android.provider.ContactsContract;

import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.models.RemovableItem;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Joey on 6/3/16.
 */
public class PillItem extends RemovableItem implements Comparable<PillItem>{

    private long pillId = -1;
    private String title;
    private String instr;
    private int duration;
    private long untilDate;
    private long refillDate;
    private List<long[]> timesPerDay = new ArrayList<>();
    private TimesPerDayManager timesManager;

    public PillItem(PillItem item)
    {
        this.pillId = item.pillId;
        this.title = item.title;
        this.instr = item.instr;
        this.duration = item.duration;
        this.untilDate = item.untilDate;
        this.refillDate = item.refillDate;
        this.timesPerDay = new ArrayList<>(item.timesPerDay);
        buildTimeManager();

    }

    private PillItem(String title, String instr, long refillDate)
    {
        this.refillDate = refillDate;
        this.title = title;
        this.instr = instr;
        for(int i = 0; i < 7; i++)
            timesPerDay.add(null);
        buildTimeManager();
    }

    public PillItem(String title, String instr, int duration, long refillDate)
    {
        this(title, instr, refillDate);
        this.duration = duration;
        this.untilDate = -1;
    }

    public PillItem(String title, String instr,long untilDate, long refillDate)
    {
        this(title, instr, refillDate);
        this.untilDate = untilDate;
        this.duration = -1;
    }

    public List<long[]> getTimesPerDay()
    {
        return timesPerDay;
    }

    public void setTimesForDay(Days day, long[] times)
    {
        timesPerDay.set(day.getNumVal(), times);
        buildTimeManager();
    }

    private void buildTimeManager()
    {
        timesManager = new TimesPerDayManager(timesPerDay);
    }

    public void setTimesForDay(int day, long[] times)
    {
        timesPerDay.set(day, times);
        buildTimeManager();
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

    public boolean getIsEndByDate()
    {
        return duration == -1;
    }
    public TimesPerDayManager getTimesManager()
    {
        return timesManager;
    }

    /**
     * Comapre by which one has the soonest time to take the pill
     * @param another
     * @return -1 = another has earliest first time, 1 = this has earliest first time,
     * 0 = both have same first times time
     */
    @Override
    public int compareTo(PillItem another) {
        DateTime today = new DateTime(new Date());
        int dayStoSa = today.getDayOfWeek() == 6 ? 0 : today.getDayOfWeek() - 1;
        List<TimesPerDayManagerItem> thisManagerItems =  getTimesManager().getTimesPerDay();
        List<TimesPerDayManagerItem> anManagerItems = another.getTimesManager().getTimesPerDay();

        //Check remainder of this week
        for (int i = dayStoSa; i < 7; i++)
        {
            //Both have times for today
            if(thisManagerItems.get(i).getTimesList().size() > 0 &&
                    anManagerItems.get(i).getTimesList().size() > 0)
            {
                SimpleTimeItem aEarliest = thisManagerItems.get(i).getTimesList().get(0);
                SimpleTimeItem thisEarliest = anManagerItems.get(i).getTimesList().get(0);
                if(aEarliest.getHour24() < thisEarliest.getHour24())
                {
                    return -1;
                }
                else if(aEarliest.getHour24() > thisEarliest.getHour24())
                {
                    return 1;
                }
                else
                {
                    if(aEarliest.getMins() > thisEarliest.getMins())
                    {
                        return 1;
                    }
                    else if(aEarliest.getMins() < thisEarliest.getMins())
                    {
                        return -1;
                    }
                    else return 0;
                }
            }//This has times for today, it wins
            else if(thisManagerItems.get(i).getTimesList().size() > 0)
            {
                return -1;
            }
            else if(anManagerItems.get(i).getTimesList().size() > 0)
            {
                //Another has times for today it wins
                return 1;
            }
        }

        //Okay loop around to next week
        for(int i = 0; i < 7; i++)
        {
            //Both have times for today
            if(thisManagerItems.get(i).getTimesList().size() > 0 &&
                    anManagerItems.get(i).getTimesList().size() > 0)
            {
                SimpleTimeItem aEarliest = thisManagerItems.get(i).getTimesList().get(0);
                SimpleTimeItem thisEarliest = anManagerItems.get(i).getTimesList().get(0);
                if(aEarliest.getHour24() < thisEarliest.getHour24())
                {
                    return -1;
                }
                else if(aEarliest.getHour24() > thisEarliest.getHour24())
                {
                    return 1;
                }
                else
                {
                    if(aEarliest.getMins() > thisEarliest.getMins())
                    {
                        return 1;
                    }
                    else if(aEarliest.getMins() < thisEarliest.getMins())
                    {
                        return -1;
                    }
                    else return 0;
                }
            }//This has times for today, it wins
            else if(thisManagerItems.get(i).getTimesList().size() > 0)
            {
                return -1;
            }
            else if(anManagerItems.get(i).getTimesList().size() > 0)
            {
                //Another has times for today it wins
                return 1;
            }
        }

        //This would be an error
        return 0;
    }
}
