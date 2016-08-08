package com.cruzj6.mha.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.cruzj6.mha.models.AppointmentItem;
import com.cruzj6.mha.models.RemovableItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joey on 6/6/16.
 */
public class RemovableItemListViewAdapter extends ArrayAdapter<RemovableItem> {

    private boolean removeMode = false;

    public RemovableItemListViewAdapter(Context context, int resource, List<? extends RemovableItem> objects) {
        super(context, resource, (List<RemovableItem>)objects);
    }

    protected final void setRemovalScan(final CheckBox itemRemoveChkBox, final RemovableItem theItem)
    {
        //Set up the checkbox to toggle removal
        itemRemoveChkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemRemoveChkBox.isChecked()) theItem.setShouldRemove(true);
                else theItem.setShouldRemove(false);
            }
        });

        //If we are in remove mode
        if(removeMode) {itemRemoveChkBox.setVisibility(View.VISIBLE);}
        else itemRemoveChkBox.setVisibility(View.GONE);
    }

    public final void startRemoveMode()
    {
        removeMode = true;


        //Unflag each item for removal in-case
        for(int i = 0; i < getCount() - 1; i++)
        {
            RemovableItem curItem = getItem(i);
            curItem.setShouldRemove(false);
        }

        notifyDataSetChanged();
    }

    public final void endRemoveMode(Boolean save)
    {

        List<AppointmentItem> toRemove = new ArrayList<>();
        //Check each item for removal flag
        for(int i = 0; i < getCount(); i++)
        {
            RemovableItem curItem = getItem(i);
            //If save mode, remove the ones that are flagged for removal
            if(save) {
                if(curItem.getShouldRemove()){
                    curItem.removeFromDatabase(getContext());
                }
            }
            else
                curItem.setShouldRemove(false);
        }


        //End remove mode
        removeMode = false;
        notifyDataSetChanged();
    }
}
