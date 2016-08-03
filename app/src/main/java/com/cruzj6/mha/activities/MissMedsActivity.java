package com.cruzj6.mha.activities;

import android.os.Bundle;
import android.widget.ListView;

import com.cruzj6.mha.R;
import com.cruzj6.mha.adapters.MissedPillContainerAdapter;
import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.models.MissedPillContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joey on 8/2/16.
 */
public class MissMedsActivity extends NoAddListActivity {

    private ListView missedListView;
    private MissedPillContainerAdapter missedAdapter;
    private List<MissedPillContainer> missedMedsList = new ArrayList<>();
    private static final String TAG = "MissedMedsActivity";

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.activity_missed_pills_log);

        //Get listview handle
        missedListView = (ListView)findViewById(R.id.listview_missed_pills);
        missedMedsList = new DatabaseManager(this).loadAllMissedPills();
        missedAdapter = new MissedPillContainerAdapter(this, 0, missedMedsList);
        missedListView.setAdapter(missedAdapter);
    }

    /**
     * Reload missed list view from the database
     */
    private void refreshList()
    {
        missedMedsList = new DatabaseManager(this).loadAllMissedPills();
        missedAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onRemoveModeStart() {
        missedAdapter.startRemoveMode();
    }

    @Override
    protected void onConfirmRemove() {
        missedAdapter.endRemoveMode(true);
        refreshList();
    }

    @Override
    protected void onRemoveModeEnd() {
        missedAdapter.endRemoveMode(false);
        refreshList();
    }
}
