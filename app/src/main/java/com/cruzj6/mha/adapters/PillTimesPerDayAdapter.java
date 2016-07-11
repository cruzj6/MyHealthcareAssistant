package com.cruzj6.mha.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.cruzj6.mha.R;
import com.cruzj6.mha.models.Days;
import com.cruzj6.mha.models.OnPillDayEnabledHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Joey on 6/20/16.
 */
public class PillTimesPerDayAdapter extends ArrayAdapter<long[]>{

    private DayEnabledHandler listener;
    private long[] globalTimes;
    private boolean isSameTimesEachDaySet = false;
    private HashMap<Days, Boolean> enabledForDay = new HashMap<>();

    public PillTimesPerDayAdapter(Context context, int resource, List<long[]> objects, DayEnabledHandler listener) {
        super(context, resource, objects);
        this.listener = listener;
    }

    @Override
    public View getView (final int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dayView = (View) convertView;
        if(dayView == null)
            dayView = inf.inflate(R.layout.layout_pill_daytime_item, null);
        TextView dayNameView = (TextView) dayView.findViewById(R.id.textview_day_name);
        final TextView dayTimesView = (TextView) dayView.findViewById(R.id.textview_day_times);
        final CheckBox dayEnabledChk = (CheckBox) dayView.findViewById(R.id.checkbox_day_enabled);
        switch(Days.values()[position])
        {
            case SUNDAY:
                dayNameView.setText(R.string.sunday);
                break;

            case MONDAY:
                dayNameView.setText(R.string.monday);
                break;

            case TUESDAY:
                dayNameView.setText(R.string.tuesday);
                break;

            case WEDNESDAY:
                dayNameView.setText(R.string.wednesday);
                break;

            case THURSDAY:
                dayNameView.setText(R.string.thursday);
                break;

            case FRIDAY:
                dayNameView.setText(R.string.friday);
                break;

            case SATURDAY:
                dayNameView.setText(R.string.saturday);
                break;

            default:
                dayNameView.setText(R.string.sw_error);
        }

        //When day is enabled
        dayEnabledChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                listener.onDayEnabledChanged(Days.values()[position], isChecked);
                long[] myTimes = getItem(position);
                if(isChecked && myTimes != null && myTimes.length > 0)
                {
                    setTimesText(dayTimesView, myTimes);
                }
                else setTimesText(dayTimesView, null);
            }
        });

        //Set up times text
        final long[] times = getItem(position);
        setTimesText(dayTimesView, times);
        if(times != null)
        {
            if(!dayEnabledChk.isChecked())
                dayEnabledChk.setChecked(true);
        }
        else {
            if(dayEnabledChk.isChecked())
                dayEnabledChk.setChecked(false);
        }


        return dayView;
    }

    private void setTimesText(TextView dayTimesView, long[] times)
    {
        SimpleDateFormat f = new SimpleDateFormat("hh:mm aaa");
        StringBuilder sb = new StringBuilder();
        //sb.append("Times: ");
        if(times != null) {
            for (long time : times) {
                sb.append(f.format(new Date(time * 1000)));
                sb.append(", ");
            }
        }
        if(sb.length() > 2) sb.deleteCharAt(sb.length() - 2);
        dayTimesView.setText(sb.toString());
    }

}
