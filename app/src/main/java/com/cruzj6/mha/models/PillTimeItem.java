package com.cruzj6.mha.models;

import android.content.Context;

import java.util.List;

/**
 * Created by Joey on 7/5/16.
 */
public class PillTimeItem extends RemovableItem {
    public int hourOfDay;
    public int minute;
    private PillItem pItem;

    public PillTimeItem(int hourOfDay, int minute)
    {
        this.hourOfDay = hourOfDay;
        this.minute = minute;
        this.pItem = pItem;
    }

    @Override
    public void removeFromDatabase(Context context) {

    }
}
