package com.cruzj6.mha.models;

import android.content.Context;

import com.cruzj6.mha.dataManagement.DatabaseManager;

/**
 * Created by Joey on 8/1/16.
 */
public class MissedPillContainer extends RemovableItem {

    private String pillName;
    private long missedPillId;
    private long missedTime;

    public MissedPillContainer(String pillName, long missedTime)
    {
        this.pillName = pillName;
        this.missedTime = missedTime;
    }

    public MissedPillContainer(String pillName, long missedPillId, long missedPillTime)
    {
        this(pillName, missedPillTime);
        this.missedPillId = missedPillId;
    }

    public String getPillName() {
        return pillName;
    }

    public long getMissedPillId() {
        return missedPillId;
    }

    public long getMissedTime() {
        return missedTime;
    }

    public void setMissedPillId(long missedPillId) {
        this.missedPillId = missedPillId;
    }

    @Override
    public void removeFromDatabase(Context context) {
        new DatabaseManager(context).removeMissedPill(this.missedPillId);
    }
}
