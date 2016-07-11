package com.cruzj6.mha.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cruzj6.mha.R;
import com.cruzj6.mha.fragments.PillSettingsDialog;
import com.cruzj6.mha.helpers.TimeHelper;
import com.cruzj6.mha.models.ItemSettingsInvokeHandler;
import com.cruzj6.mha.models.PillItem;
import com.cruzj6.mha.models.SettingsTypes;

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
        View pillItemView = convertView;
        if(pillItemView == null)
            pillItemView = inf.inflate(R.layout.listviewitem_pill, null);
        ImageButton notesBtn = (ImageButton) pillItemView.findViewById(R.id.button_show_item_notes);
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
        boolean diffTimes = TimeHelper.checkSameTimesEachDay(curItem);
        long[] times = null;
        long[] cachedTimes = null;
        int numDays = 0;
        for(int i = 0; i < 7; i++)
        {
            if (curItem.getTimesForDay(i) != null) {
                numDays++;
                cachedTimes = curItem.getTimesForDay(i);

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
        }

        if(daysSb.length() > 1) daysSb.deleteCharAt(daysSb.length() - 1);

        //Set the days to take text
        pillDaysTextView.setText(getContext()
                .getString(R.string.take, (numDays == 7 ? "Every Day" : daysSb.toString())));

        StringBuilder timesSb = new StringBuilder();
        SimpleDateFormat f = new SimpleDateFormat("hh:mm aaa");

        //Build string with each time
        if(!diffTimes)
        {
            for(long time : cachedTimes)
            {
                Date d = new Date((long)time*1000);
                timesSb.append(f.format(d) + ", ");
            }
            timesSb.deleteCharAt(timesSb.length() - 2);
        }

        //Finally set the text
        pillTimesToTake.setText(diffTimes ? "Times Differ Per Day" : timesSb.toString());

        //New instance with new format
        f.applyPattern("MM/dd/yy");

        refillByTextView.setText(getContext()
                .getString(R.string.refill_by, f.format(new Date(curItem.getRefillDate()*1000))));
        notesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Pop up the notes in a dialog
                AlertDialog.Builder d = new AlertDialog.Builder(getContext());
                d.setTitle("Medication Notes");
                d.setMessage(curItem.getInstr());
                d.show();
            }
        });

        pillItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PillSettingsDialog psd = new PillSettingsDialog();
                Bundle b = new Bundle();
                b.putLong("id", curItem.getPillId());
                b.putSerializable("mode", SettingsTypes.EDIT_EXISTING);
                psd.setItemSettingsInvokeHandler((ItemSettingsInvokeHandler)getContext());
                psd.setArguments(b);
                psd.show(((AppCompatActivity)getContext()).getSupportFragmentManager(), "Pill Settings");
            }
        });

        return pillItemView;
    }
}
