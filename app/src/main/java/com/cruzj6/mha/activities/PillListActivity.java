package com.cruzj6.mha.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cruzj6.mha.adapters.PillListItemAdapter;
import com.cruzj6.mha.R;
import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.fragments.PillSettingsDialog;
import com.cruzj6.mha.helpers.TimeHelper;
import com.cruzj6.mha.models.Days;
import com.cruzj6.mha.models.ItemSettingsInvokeHandler;
import com.cruzj6.mha.models.PillItem;
import com.cruzj6.mha.models.SettingsTypes;

import java.util.Collections;
import java.util.List;

/**
 * Created by Joey on 6/3/16.
 */
public class PillListActivity extends ListActivity implements ItemSettingsInvokeHandler{

    private PillListItemAdapter adapter;
    private List<PillItem> pillsList;
    private int day;

    //Controls
    private ListView pillsListView;

    @Override
    protected void onCreate(Bundle savedBundleInstance)
    {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.activity_pill_list);
        pillsListView = (ListView)findViewById(R.id.listview_pills);

        //On click open the Pill settings dialog for the item clicked
        pillsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PillSettingsDialog psd = new PillSettingsDialog();
                Bundle b = new Bundle();
                b.putLong("id", pillsList.get(position).getPillId());
                b.putSerializable("mode", SettingsTypes.EDIT_EXISTING);
                psd.setItemSettingsInvokeHandler(PillListActivity.this);
                psd.setArguments(b);
                psd.show(getSupportFragmentManager(), "Pill Settings");
            }
        });

        //Get what day or all days this view is to be for
        day = getIntent().getExtras().getInt("day");
        setTitle("Medication for " +
                (day != -1 ? Days.values()[day].getStringName() : "All Days"));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        buildPillsAdapter();
    }

    public void buildPillsAdapter()
    {
        //Load up our pills for the selected day from the database, or all days, accordingly
        if(day != -1)
            pillsList = new DatabaseManager(this).loadPillsForDay(day);
        else
            pillsList = new DatabaseManager(this).loadPillItems();

        Collections.sort(pillsList);

        //Set up adapter with the list
        adapter = new PillListItemAdapter(this, 0, pillsList);
        pillsListView.setAdapter(adapter);
    }

    /*private void addPillTest()
    {
        PillItem newItem = new PillItem("Pill Name", "I WILL TAKE", 5, 1468326605);

        long[] times = {1468326605, TimeHelper.getTimeUnix(5, 30, 0)};
        newItem.setTimesForDay(Days.TUESDAY, times);
        new DatabaseManager(this).savePill(newItem);
        buildPillsAdapter();
    }*/

    @Override
    protected void onRemoveModeStart() {
        adapter.startRemoveMode();
    }

    @Override
    protected void onAddClick() {
        PillSettingsDialog psd = new PillSettingsDialog();
        Bundle b = new Bundle();
        b.putSerializable("mode", SettingsTypes.NEW_ITEM);
        psd.setItemSettingsInvokeHandler(PillListActivity.this);
        psd.setArguments(b);
        psd.show(getSupportFragmentManager(), "Pill Settings");
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

    @Override
    public void onItemSaved() {
        buildPillsAdapter();
    }
}
