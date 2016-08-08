package com.cruzj6.mha;

import android.support.v7.widget.CardView;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cruzj6.mha.activities.MainActivity;
import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.fragments.AppointmentsQueue;
import com.cruzj6.mha.helpers.TimeHelper;
import com.cruzj6.mha.models.AppointmentItem;
import com.cruzj6.mha.models.Days;
import com.cruzj6.mha.models.PillItem;
import com.cruzj6.mha.models.SimpleTimeItem;
import com.cruzj6.mha.models.TimesPerDayManager;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import dalvik.annotation.TestTarget;

/**
 * Created by Joey on 8/5/16.
 */
public class MainTestSuite extends ActivityInstrumentationTestCase2<MainActivity> {
    public MainTestSuite() {
        super(MainActivity.class);
    }

    DatabaseManager dbm;
    @Override
    protected void setUp() throws Exception{
        super.setUp();

    }

    @SmallTest
    public void testDatabasePillsAndTimeItems()
    {
        dbm = new DatabaseManager(getActivity());
        PillItem pi = new PillItem("Test Pill", "Take This", 5, new Date().getTime());

        long[] times = {0, 0, 0, 0, 0, 0, 0};
        times[0] = TimeHelper.getTimeUnix(5, 22, 0);
        pi.setTimesForDay(Days.TUESDAY, times);
        long id = dbm.savePill(pi);
        pi.setPillId(id);
        dbm.addMissedPill(pi);

        //Test object same
        PillItem copy = null;
        for(int i = 0; i < dbm.loadPillItems().size(); i++)
        {
            if (dbm.loadPillItems().get(i).getPillId() == id)
                 copy = dbm.loadPillItems().get(i);
        }
        boolean isSame = true;
        TimesPerDayManager piM = pi.getTimesManager();
        TimesPerDayManager cM = copy.getTimesManager();
        for(int i = 0 ; i < piM.getTimesPerDay().size(); i++)
        {
            if(piM.getTimesPerDay().get(i).getDay() != cM.getTimesPerDay().get(i).getDay())
            {
                isSame = false;
            }
            for(int j = 0; j < piM.getTimesPerDay().get(i).getTimesList().size(); j++)
            {
                SimpleTimeItem pS = piM.getTimesPerDay().get(i).getTimesList().get(j);
                SimpleTimeItem cS = cM.getTimesPerDay().get(i).getTimesList().get(j);
                if(pS.getMins() != cS.getMins() || pS.getHour24() != cS.getHour24() || pS.getHour() != cS.getHour())
                {
                    isSame = false;
                }
            }
        }
        if(copy.getPillId() != pi.getPillId() || !copy.getTitle().equals(pi.getTitle()))
        {
            isSame = false;
        }

        //Test same Object data
        assertTrue(isSame);

        //Test count
        assertTrue(dbm.loadPillsForDay(Days.TUESDAY.getNumVal()).size() > 0);

        //Test time converter and ID
        assertTrue(dbm.loadPillItemById(id).getTimesForDay(Days.TUESDAY)[0] == TimeHelper.getTimeUnix(5,22,0));

        //Test Removal
        dbm.deletePill(id);
        assertTrue(dbm.loadPillItemById(id) == null);
    }

    @SmallTest
    public void testDatabaseAppointmentsAndTimes()
    {
        List<AppointmentItem> apptItems = new DatabaseManager(getActivity()).loadAppointmentItems();

        //Sort testing
        Collections.sort(apptItems);
        for(int i = 0; i < apptItems.size(); i++)
        {
            assertTrue(apptItems.get(i).getApptId() != -1);

            //Test dates sorted properly
            for(int j = i + 1; j < apptItems.size(); j++) {
                assertTrue(apptItems.get(i).getApptDate() < apptItems.get(j).getApptDate());
            }
        }

        AppointmentItem it = new AppointmentItem("Appt", new Date().getTime(), 3, "no");
        long id = dbm.saveAppointment(it);

        //Check save
    }

    @SmallTest
    public void testAppointmentQueue()
    {
        LinearLayout aq = (LinearLayout)getActivity().findViewById(R.id.linearlayout_queue_scrollview);
        List<AppointmentItem> apptItems = new DatabaseManager(getActivity()).loadAppointmentItems();
        Collections.sort(apptItems);

        //Test we have titles for Appt queue
        assertTrue(aq != null);
    }


    @Override
    protected void tearDown() throws Exception{
        dbm.close();
    }
}
