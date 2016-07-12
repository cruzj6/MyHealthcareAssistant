package com.cruzj6.mha.models;

/**
 * Created by Joey on 7/11/16.
 */
public class SimpleTimeItem {
    private int hour24;
    private int mins;
    private int hour;
    public SimpleTimeItem(int hours24, int mins)
    {
        this.hour24 = hours24;
        this.mins = mins;
        this.hour = hours24 > 12 ? hours24 - 12 : hour24;
    }
}
