package com.cruzj6.mha.activities;

import android.os.Bundle;

import com.cruzj6.mha.adapters.PillListItemAdapter;
import com.cruzj6.mha.R;

/**
 * Created by Joey on 6/3/16.
 */
public class PillListActivity extends ListActivity {

    private PillListItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedBundleInstance)
    {
        super.onCreate(savedBundleInstance);
        setContentView(R.layout.activity_pill_list);
    }


    @Override
    protected void onRemoveModeStart() {

    }

    @Override
    protected void onAddClick() {

    }

    @Override
    protected void onConfirmRemove() {

    }

    @Override
    protected void onRemoveModeEnd() {

    }
}
