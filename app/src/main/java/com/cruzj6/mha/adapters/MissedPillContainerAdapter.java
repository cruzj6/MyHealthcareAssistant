package com.cruzj6.mha.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cruzj6.mha.R;
import com.cruzj6.mha.models.MissedPillContainer;
import com.cruzj6.mha.models.RemovableItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Joey on 8/2/16.
 */
public class MissedPillContainerAdapter extends RemovableItemListViewAdapter {

    public MissedPillContainerAdapter(Context context, int resource, List<MissedPillContainer> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView (final int position, View convertView, ViewGroup parent) {
        MissedPillContainer item = (MissedPillContainer) getItem(position);

        View view = convertView;
        if(view == null) {
            LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.listviewitem_missed_med, null);
        }

        //Get component refs
        TextView missedTitleTextView = (TextView) view.findViewById(R.id.textview_missed_title);
        TextView missedDetailsTextView = (TextView) view.findViewById(R.id.textview_missed_details);

        //Missed details
        SimpleDateFormat f = new SimpleDateFormat("MM/dd/yy\nhh:mm aaa");
        missedDetailsTextView.setText("Missed: " + f.format(new Date(item.getMissedTime()*1000)));
        missedTitleTextView.setText(item.getPillName());

        //Super removal setup
        setRemovalScan((CheckBox)view.findViewById(R.id.checkbox_remove_missed_pill), item);

        return view;
    }

}
