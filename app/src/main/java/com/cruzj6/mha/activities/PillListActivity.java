package com.cruzj6.mha.activities;

import android.os.Bundle;
import android.widget.ListView;

import com.cruzj6.mha.adapters.PillListItemAdapter;
import com.cruzj6.mha.R;
import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.helpers.TimeHelper;
import com.cruzj6.mha.models.Days;
import com.cruzj6.mha.models.PillItem;

import java.util.List;

/**
 * Created by Joey on 6/3/16.
 */
public class PillListActivity extends ListActivity {

    private PillListItemAdapter adapter;
    private List<PillItem> pillsList;
    private int day;

    //Controls
    private static ListView pillsListView;

    @Override
    protected void onCreate(Bundle savedBundleInstance)
    {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.activity_pill_list);
        pillsListView = (ListView)findViewById(R.id.listview_pills);

        //Get what day or all days this view is to be for
        day = getIntent().getExtras().getInt("day");

        //Build our adapter
        buildPillsAdapter();
    }

    private void buildPillsAdapter()
    {
        //Load up our pills for the selected day from the database, or all days, accordingly
        if(day != -1)
            pillsList = new DatabaseManager(this).loadPillsForDay(day);
        else
            pillsList = new DatabaseManager(this).loadPillItems();

        //Set up adapter with the list
        adapter = new PillListItemAdapter(this, 0, pillsList);

        pillsListView.setAdapter(adapter);
    }

    private void addPillTest()
    {
        PillItem newItem = new PillItem("Pill Name", "I WILL TAKE", 5, -1, 1468326605);

        long[] times = {1468326605, TimeHelper.getTimeUnix(5, 30, 0)};
        newItem.setTimesForDay(Days.TUESDAY, times);
        new DatabaseManager(this).savePill(newItem);
        buildPillsAdapter();
    }

    @Override
    protected void onRemoveModeStart() {
        adapter.startRemoveMode();
    }

    @Override
    protected void onAddClick() {
        //TODO: Open the add view
        addPillTest();
    }

    @Override
    protected void onConfirmRemove() {
        adapter.endRemoveMode(true);

        //Rebuild, it may have changed
        buildPillsAdapter();
    }

    @Override
    protected void onRemoveModeEnd() {
        adapter.endRemoveMode(false);
    }
}
