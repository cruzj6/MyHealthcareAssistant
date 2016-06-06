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
import com.cruzj6.mha.models.PillItem;

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

        //Set up the super class's removal mode with the checkbox
        final CheckBox removeBox = (CheckBox) pillItemView.findViewById(R.id.checkbox_remove);
        setRemovalScan(removeBox, curItem);

        //Set up components
        titleTextView.setText(curItem.getTitle());
        notesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Pop up the notes in a dialog
                AlertDialog.Builder d = new AlertDialog.Builder(getContext());
                d.setTitle("Appointment Notes");
                d.setMessage(curItem.getNotes());
            }
        });

        return pillItemView;
    }
}
