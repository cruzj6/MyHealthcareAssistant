package com.cruzj6.mha.models;

import java.io.Serializable;

/**
 * Created by Joey on 7/11/16.
 */
public class SimpleTimeItem implements Comparable<SimpleTimeItem>, Serializable {
    private int hour24;
    private int mins;
    private int hour;
    public SimpleTimeItem(int hours24, int mins)
    {
        this.hour24 = hours24;
        this.mins = mins;
        this.hour = hours24 > 12 ? hours24 - 12 : hour24;
    }

    public int getHour24(){
        return hour24;
    }

    public int getHour() {
        return hour;
    }

    public int getMins() {
        return mins;
    }

    @Override
    public int compareTo(SimpleTimeItem another) {
        if(another.getHour24() > this.hour24)
        {
            return 1;
        }
        else if(another.getHour24() == this.getHour24())
        {
            if(another.getMins() > this.getMins())
            {
                return 1;
            }
            else if(another.getMins() < this.getMins())
            {
                return -1;
            }
            else
            {
                return 0;
            }
        }
        else
        {
            return -1;
        }
    }
}
