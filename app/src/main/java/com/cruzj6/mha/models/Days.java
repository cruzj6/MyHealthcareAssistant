package com.cruzj6.mha.models;

/**
 * Created by Joey on 6/6/16.
 */
public enum Days {

    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6);

    private int numVal;

    Days(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
