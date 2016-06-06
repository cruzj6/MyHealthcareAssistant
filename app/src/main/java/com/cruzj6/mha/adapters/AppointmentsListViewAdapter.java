package com.cruzj6.mha.adapters;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cruzj6.mha.fragments.AppointmentSettingsDialog;
import com.cruzj6.mha.R;
import com.cruzj6.mha.models.AppointmentItem;

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
            f = new SimpleDateFormat("MM/dd/yy");

            //Set up the labwork label with this data
            labworkLabel.setVisibility(View.VISIBLE);
            labworkLabel.setText(labworkString + f.format(c.getTime()));
        }

        //Set the appointment title
        apptLabel.setText(thisItem.getAppointmentTitle());

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Give it the id of this item so it can get it from the db to edit
                AppointmentSettingsDialog dialog = new AppointmentSettingsDialog();
                Bundle args = new Bundle();
                args.putSerializable("mode", AppointmentSettingsDialog.SettingsTypes.EDIT_EXISTING);
                args.putLong("id", thisItem.getApptId());
                dialog.setArguments(args);
                dialog.show(((AppCompatActivity)getContext()).getSupportFragmentManager(), "Appointment Settings");
            }
        });

        return itemView;
    }

}
