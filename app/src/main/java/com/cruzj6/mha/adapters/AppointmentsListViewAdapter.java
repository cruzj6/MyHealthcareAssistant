package com.cruzj6.mha.adapters;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cruzj6.mha.fragments.AppointmentSettingsDialog;
import com.cruzj6.mha.R;
import com.cruzj6.mha.models.AppointmentItem;
import com.cruzj6.mha.models.ItemSettingsInvokeHandler;
import com.cruzj6.mha.models.SettingsTypes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Joey on 5/23/16.
 */
public class AppointmentsListViewAdapter extends RemovableItemListViewAdapter
{
    public AppointmentsListViewAdapter(Context context, int resource, List<AppointmentItem> objects) {
        super(context, resource, objects);

    }

    @Override
    public View getView (final int position, View convertView, ViewGroup parent)
    {
        //Get our views
        LayoutInflater inf = LayoutInflater.from(getContext());
        View itemView = inf.inflate(R.layout.listviewitem_appt, null);
        ImageButton notesBtn = (ImageButton) itemView.findViewById(R.id.button_show_item_notes);
        TextView apptLabel = (TextView) itemView.findViewById(R.id.textview_label);
        TextView apptSubLabel = (TextView) itemView.findViewById(R.id.textview_label_sub);

        //Final to save reference for async click listener
        final CheckBox itemChkBox = (CheckBox) itemView.findViewById(R.id.checkbox_remove);
        final AppointmentItem thisItem = (AppointmentItem) getItem(position);

        //Call super class's removal setup method
        setRemovalScan(itemChkBox, thisItem);

        //Create date item from the unix time and set to the sublabel
        Date apptDate = new Date((long)thisItem.getApptDate() * 1000);
        SimpleDateFormat f = new SimpleDateFormat("EEE MM/dd/yy hh:mm aaa");
        apptSubLabel.setText(f.format(apptDate));

        //Set up labwork label if needed
        if(thisItem.getRequiresLabWork())
        {
            String labworkString = getContext().getString(R.string.labwork_req);
            TextView labworkLabel = (TextView) itemView.findViewById(R.id.textview_labwork_req);

            //Get what day the labwork needs to be done by
            Calendar c = Calendar.getInstance();
            c.setTime(apptDate);
            long timeAdd = thisItem.getLabworkDaysBefore();
            c.add(Calendar.DATE, -(int)timeAdd);
            f.applyPattern("MM/dd/yy");

            //Set up the labwork label with this data
            labworkLabel.setVisibility(View.VISIBLE);
            labworkLabel.setText(labworkString + f.format(c.getTime()));
        }

        //Set the appointment title
        apptLabel.setText(thisItem.getAppointmentTitle());

        //See if we need notes button
        notesBtn.setVisibility(thisItem.getNotes().equals("") ? View.INVISIBLE : View.VISIBLE);
        notesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
                ad.setMessage(thisItem.getNotes());
                ad.setTitle("Appointment Notes");
                ad.show();
            }
        });

        return itemView;
    }

}
