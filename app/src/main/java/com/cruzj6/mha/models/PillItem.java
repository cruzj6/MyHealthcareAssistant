package com.cruzj6.mha.models;

import android.content.Context;

import com.cruzj6.mha.models.RemovableItem;

/**
 * Created by Joey on 6/3/16.
 */
public class PillItem extends RemovableItem {

    private String title;
    private String notes;

    public String getTitle()
    {
        return title;
    }

    public String getNotes(){
        return notes;
    }

    @Override
    public void removeFromDatabase(Context context) {
        //TODO
    }
}
