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
    private String stringName;

    Days(int numVal) {
        this.numVal = numVal;
        switch(numVal)
        {
            case 0:
                this.stringName = "Sunday";
                break;
            case 1:
                this.stringName = "Monday";
                break;
            case 2:
                this.stringName = "Tuesday";
                break;
            case 3:
                this.stringName = "Wednesday";
                break;
            case 4:
                this.stringName = "Thursday";
                break;
            case 5:
                this.stringName = "Friday";
                break;
            case 6:
                this.stringName = "Saturday";
                break;
        }
    }

    public int getNumVal() {
        return numVal;
    }
    public String getStringName()
    {
        return stringName;
    }
}
