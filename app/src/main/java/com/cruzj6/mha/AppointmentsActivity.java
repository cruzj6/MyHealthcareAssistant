package com.cruzj6.mha;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.cruzj6.mha.dataManagement.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsActivity extends AppCompatActivity {

    private static ListView apptsListView;
    private static DatabaseManager dbm;
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
    }

    public void buildApptsList()
    {
        List<AppointmentItem> apptItems = new ArrayList<AppointmentItem>();

        //Load from the database
        apptItems = dbm.loadAppointmentItems();

        //Set up the adapter
        apptAdapter = new AppointmentsListViewAdapter(this, 0, apptItems);
        apptsListView.setAdapter(apptAdapter);
    }

    public void addAppointment(View view)
    {
        //TODO: Get from user
        AppointmentItem newAppt = new AppointmentItem("Dr. test", 1466598600, 3, "THIS IS MY NOTES WOOO", 3);
        new DatabaseManager(this).saveAppointment(newAppt);
        this.buildApptsList();
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_home:
                Intent homeIntent = new Intent(AppointmentsActivity.this, MainActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                break;

            case R.id.action_remove_appts:

                if(!removeMode){
                    removeMode = true;

                    startRemoveActionMode();
                    //Start the remove mode
                    apptAdapter.startRemoveMode();
                }
                break;
            case R.id.action_add_appointment:
                AppointmentSettingsDialog dialog = new AppointmentSettingsDialog();
                Bundle args = new Bundle();
                args.putSerializable("mode", AppointmentSettingsDialog.SettingsTypes.NEW_APPOINTMENT);
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "Appointment Settings");
                break;

            default:
                break;
        }
        return true;
    }

    private void startRemoveActionMode()
    {
        removeActionMode = startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.setTitle("Delete Appointments");
                MenuInflater inf = mode.getMenuInflater();
                inf.inflate(R.menu.delete_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.action_confirm_delete:
                        removeMode = false;

                        //Remove mode end, save as true
                        apptAdapter.endRemoveMode(true);

                        //Reload from database and change icons
                        buildApptsList();

                        //End this mode
                        endRemoveActionMode();
                        return true;
                    default:
                        return false;
                }

            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                removeMode = false;

                //Remove mode end, save as true
                apptAdapter.endRemoveMode(false);

                //Reload from database and change icons
                buildApptsList();
            }
        });
    }

    private void endRemoveActionMode()
    {
        if(removeActionMode != null) {
            removeActionMode.finish();
        }
    }

}
