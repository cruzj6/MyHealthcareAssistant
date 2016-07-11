package com.cruzj6.mha.activities;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cruzj6.mha.fragments.AppointmentSettingsDialog;
import com.cruzj6.mha.adapters.AppointmentsListViewAdapter;
import com.cruzj6.mha.R;
import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.models.AppointmentItem;
import com.cruzj6.mha.models.ItemSettingsInvokeHandler;
import com.cruzj6.mha.models.SettingsTypes;

import java.util.List;

public class AppointmentsActivity extends ListActivity implements ItemSettingsInvokeHandler {

    private List<AppointmentItem> apptItems;

    private ListView apptsListView;
    private DatabaseManager dbm;
    private Menu abMenu;
    private AppointmentsListViewAdapter apptAdapter;
    private boolean removeMode = false;
    private ActionMode removeActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        //Set up database manager
        dbm= new DatabaseManager(this);

        //Set title for Action Bar
        getSupportActionBar().setTitle(R.string.appointments_header_string);

        //Set up the list view
        apptsListView = (ListView) findViewById(R.id.appts_listView);
        buildApptsList();
        apptsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Give it the id of this item so it can get it from the db to edit
                AppointmentSettingsDialog dialog = new AppointmentSettingsDialog();
                Bundle args = new Bundle();
                dialog.setOnSaveHandler(AppointmentsActivity.this);
                args.putSerializable("mode", SettingsTypes.EDIT_EXISTING);
                args.putLong("id", apptItems.get(position).getApptId());
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "Appointment Settings");
            }
        });

    }

    public void buildApptsList()
    {
        //Load from the database
        apptItems = dbm.loadAppointmentItems();

        //Set up the adapter
        apptAdapter = new AppointmentsListViewAdapter(this, 0, apptItems);
        apptsListView.setAdapter(apptAdapter);
    }

    public void addAppointment(View view)
    {
        //Test Method
        AppointmentItem newAppt = new AppointmentItem("Dr. test", 1466598600, 3, "THIS IS MY NOTES WOOO", 3);
        new DatabaseManager(this).saveAppointment(newAppt);
        this.buildApptsList();
    }

    @Override
    protected void onRemoveModeStart() {
        //Start the remove mode
        apptAdapter.startRemoveMode();
    }

    @Override
    protected void onAddClick() {
        AppointmentSettingsDialog dialog = new AppointmentSettingsDialog();
        dialog.setOnSaveHandler(this);
        Bundle args = new Bundle();
        args.putSerializable("mode", SettingsTypes.NEW_ITEM);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "Appointment Settings");
    }

    @Override
    protected void onConfirmRemove() {
        //Remove mode end, save as true
        apptAdapter.endRemoveMode(true);

        //Reload from database and change icons
        buildApptsList();
    }

    @Override
    protected void onRemoveModeEnd() {
        //Remove mode end, save as true
        apptAdapter.endRemoveMode(false);

        //Reload from database and change icons
        buildApptsList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        abMenu = menu;
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.appts_act_menu, menu);
        return true;
    }

    @Override
    public void onItemSaved() {
        buildApptsList();
    }
}
