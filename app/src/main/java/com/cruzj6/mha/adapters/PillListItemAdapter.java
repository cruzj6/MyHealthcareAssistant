package com.cruzj6.mha.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cruzj6.mha.R;
import com.cruzj6.mha.helpers.TimeHelper;
import com.cruzj6.mha.models.PillItem;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Joey on 6/3/16.
 */
public class PillListItemAdapter extends RemovableItemListViewAdapter {

    public PillListItemAdapter(Context context, int resource, List<PillItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView (final int position, View convertView, ViewGroup parent)
    {
        final PillItem curItem = (PillItem) getItem(position);

        //Inflate and get view components
        LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View pillItemView = inf.inflate(R.layout.listviewitem_pill, null);
        LinearLayout notesLayout = (LinearLayout) pillItemView.findViewById(R.id.linearlayout_view_notes_layout);
        TextView titleTextView = (TextView) pillItemView.findViewById(R.id.textview_pill_title);
        TextView pillDaysTextView = (TextView) pillItemView.findViewById(R.id.textview_pill_days);
        TextView pillTimesToTake = (TextView) pillItemView.findViewById(R.id.textview_take_at_times);
        TextView refillByTextView = (TextView) pillItemView.findViewById(R.id.textview_refill_by);

        //Set up the super class's removal mode with the checkbox
        final CheckBox removeBox = (CheckBox) pillItemView.findViewById(R.id.checkbox_remove);
        setRemovalScan(removeBox, curItem);

        //Set up components
        titleTextView.setText(curItem.getTitle());

        //Check which days to take it
        StringBuilder daysSb = new StringBuilder();
        boolean diffTimes = false;
        long[] lastDays = null;
        long[] times = null;
        long[] cacheTimes = null;
        int numDays = 0;
        for(int i = 0; i < 7; i++)
        {
            times = curItem.getTimesForDay(i);
            if(times != null) {
                numDays++;

                if(cacheTimes == null) cacheTimes = times;
                //If we have not determined if these times differ yet, and we have something to compare
                if(!diffTimes && lastDays != null)
                {
                    //If different amount of times they do have differing times per day
                    if(lastDays.length != times.length)
                    {
                        diffTimes = true;
                    }
                    else {//If same amount check if they differ
                        for (int j = 0; j < times.length; j++) {

                            //These should be sorted
                            if(!TimeHelper.compareUnixDateTimes(times[j], lastDays[j]))
                            {
                                diffTimes = true;
                            }
                        }
                    }
                }

                //Switch to get the char for the day to show
                switch (i) {
                    case 0:
                        daysSb.append("S" + ",");
                        break;
                    case 1:
                        daysSb.append("M" + ",");

                        break;
                    case 2:
                        daysSb.append("T" + ",");

                        break;
                    case 3:
                        daysSb.append("W" + ",");

                        break;
                    case 4:
                        daysSb.append("R" + ",");

                        break;
                    case 5:
                        daysSb.append("F" + ",");

                        break;
                    case 6:
                        daysSb.append("Sa" + ",");

                        break;

                }
            }

            lastDays = times;
        }

        daysSb.deleteCharAt(daysSb.length() - 1);
        //Set the days to take text
        pillDaysTextView.setText(getContext()
                .getString(R.string.take, (numDays == 7 ? "Every Day" : daysSb.toString())));

        StringBuilder timesSb = new StringBuilder();
        SimpleDateFormat f = new SimpleDateFormat("hh:mm aaa");

        //Build string with each time
        if(!diffTimes)
        {
            for(long time : cacheTimes)
            {
                Date d = new Date((long)time*1000);
                timesSb.append(f.format(d) + ", ");
            }
            timesSb.deleteCharAt(timesSb.length() - 2);
        }

        //Finally set the text
        pillTimesToTake.setText(diffTimes ? "Times Differ Per Day" : timesSb.toString());

        //New instance with new format
        f = new SimpleDateFormat("MM/dd/yy");

        refillByTextView.setText(getContext()
                .getString(R.string.refill_by, f.format(new Date(curItem.getRefillDate()*1000))));
        notesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Pop up the notes in a dialog
                AlertDialog.Builder d = new AlertDialog.Builder(getContext());
                d.setTitle("Medication Notes");
                d.setMessage(curItem.getInstr());
                d.show();
            }
        });

        return pillItemView;
    }
}
