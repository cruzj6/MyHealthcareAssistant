package com.cruzj6.mha.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cruzj6.mha.R;

import org.w3c.dom.Text;

/**
 * Created by Joey on 8/2/16.
 */
public abstract class NoAddListActivity extends AppCompatActivity {

    private Menu abMenu;
    private ActionMode removeActionMode;
    private boolean removeMode = false;

    //Abstract methods
    protected abstract void onRemoveModeStart();
    protected abstract void onConfirmRemove();
    protected abstract void onRemoveModeEnd();

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        abMenu = menu;
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.missed_pills_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                break;

            case R.id.action_remove:

                if(!removeMode){
                    removeMode = true;

                    startRemoveActionMode();
                    Toast.makeText(this, R.string.select_then_tap_trash, Toast.LENGTH_SHORT).show();
                    onRemoveModeStart();
                }
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
                        onConfirmRemove();

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
                onRemoveModeEnd();
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
