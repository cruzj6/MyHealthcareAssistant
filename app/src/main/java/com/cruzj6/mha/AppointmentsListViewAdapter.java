package com.cruzj6.mha;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cruzj6.mha.dataManagement.DatabaseManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by Joey on 5/23/16.
 */
public class AppointmentsListViewAdapter extends ArrayAdapter<AppointmentItem>
{
    private boolean removeMode = false;

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
        final CheckBox itemChkBox = (CheckBox) itemView.findViewById(R.id.checkbox_remove_appt);
        final AppointmentItem thisItem = getItem(position);

        //Set up the checkbox to toggle removal
        itemChkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemChkBox.isChecked()) thisItem.removeMe = true;
                else thisItem.removeMe = false;
            }
        });


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
        apptLabel.setText(getItem(position).getAppointmentTitle());

        if(removeMode) itemChkBox.setVisibility(View.VISIBLE);
        else itemChkBox.setVisibility(View.INVISIBLE);

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

    public void startRemoveMode()
    {
        removeMode = true;


        //Unflag each item for removal in-case
        for(int i = 0; i < getCount() - 1; i++)
        {
            AppointmentItem curItem = getItem(i);
            curItem.removeMe = false;
        }

        notifyDataSetChanged();
    }

    public void endRemoveMode(Boolean save)
    {
        //If save mode, remove the ones that are flagged for removal
        if(save) {

            List<AppointmentItem> toRemove = new ArrayList<>();
            //Check each item for removal flag
            for(int i = 0; i < getCount() - 1; i++)
            {
                AppointmentItem curItem = getItem(i);
                if(curItem.removeMe){
                    new DatabaseManager(getContext()).deleteAppointment(curItem.getApptId());
                }
            }
        }

        //End remove mode
        removeMode = false;
        notifyDataSetChanged();
    }

}
