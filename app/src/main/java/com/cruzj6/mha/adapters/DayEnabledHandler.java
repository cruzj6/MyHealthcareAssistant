package com.cruzj6.mha.adapters;

import com.cruzj6.mha.models.Days;

/**
 * Created by Joey on 6/24/16.
 */
public interface DayEnabledHandler {
    void onDayEnabledChanged(Days day, boolean isEnabled);
}
