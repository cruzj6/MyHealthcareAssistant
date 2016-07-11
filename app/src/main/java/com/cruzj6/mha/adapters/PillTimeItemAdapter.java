package com.cruzj6.mha.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cruzj6.mha.R;
import com.cruzj6.mha.models.PillTimeItem;
import com.cruzj6.mha.models.RemovableItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Joey on 7/5/16.
 */
public class PillTimeItemAdapter extends RemovableItemListViewAdapter {
    public PillTimeItemAdapter(Context context, int resource, List<PillTimeItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView (final int position, View convertView, ViewGroup parent){
        PillTimeItem curItem = (PillTimeItem)getItem(position);
        View view = convertView;
        if(view == null)
        {
            LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.listviewitem_pill_time, null);
        }
        TextView pillTimeTextView = (TextView) view.findViewById(R.id.textview_pill_time);
        CheckBox remCb = (CheckBox) view.findViewById(R.id.checkbox_remove_pill_time);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, curItem.hourOfDay);
        c.set(Calendar.MINUTE, curItem.minute);

        SimpleDateFormat f = new SimpleDateFormat("hh:mm aaa");
        pillTimeTextView.setText(f.format(c.getTime()));

        //Set removal scan to remove
        setRemovalScan(remCb, curItem);

        return view;
    }

}
