package com.cruzj6.mha.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cruzj6.mha.R;
import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.models.AppointmentItem;
import com.cruzj6.mha.models.Days;
import com.cruzj6.mha.models.ItemSettingsInvokeHandler;
import com.cruzj6.mha.models.PillItem;
import com.cruzj6.mha.models.SettingsTypes;
import com.cruzj6.mha.models.SimpleTimeItem;
import com.cruzj6.mha.models.TimesPerDayManager;
import com.cruzj6.mha.models.TimesPerDayManagerItem;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Joey on 6/6/16.
 * Controls the Pills Queue scrolling view fragment on the dashboard
 */
public class PillsQueue extends Fragment implements ItemSettingsInvokeHandler{
    private final static String TAG = "PillsQueue";

    public PillsQueue(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //TODO: Sort by when to be taken
        //Inflate the view
        return inflater.inflate(R.layout.fragment_queue, container, false);
    }

    private void buildPillsQueue()
    {
        try {
            //Get the scrollview's linear layout
            LinearLayout pillsLayout = (LinearLayout) getView().findViewById(R.id.linearlayout_queue_scrollview);

            List<PillItem> pillItems = new DatabaseManager(getContext()).loadPillItems();

            //Sort by which one has date first
            Collections.sort(pillItems);

            //Cut out days after/during today, sort by relative to today's day of week
            DateTime today = new DateTime(new Date());
            int dayStoSa = today.getDayOfWeek() == 7 ? 0 : today.getDayOfWeek();
            for (final PillItem pillItem : pillItems) {
                View pillQItem = getActivity().getLayoutInflater().inflate(R.layout.layout_pill_queue_item, null);
                TextView titleTextView = (TextView) pillQItem.findViewById(R.id.textview_pillq_item_title);
                TextView daysToTakeTextView = (TextView) pillQItem.findViewById(R.id.textview_pillq_item_date);
                TextView nextTakeTextView = (TextView) pillQItem.findViewById(R.id.textview_pillq_item_time);
                StringBuilder sb = new StringBuilder();
                sb.append("Take On: ");

                pillQItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PillSettingsDialog psd = new PillSettingsDialog();
                        Bundle b = new Bundle();
                        b.putLong("id", pillItem.getPillId());
                        b.putSerializable("mode", SettingsTypes.EDIT_EXISTING);
                        psd.setItemSettingsInvokeHandler(PillsQueue.this);
                        psd.setArguments(b);
                        psd.show(getActivity().getSupportFragmentManager(), "Medication Settings");
                    }
                });

                //Now build our data to show to the user
                for(TimesPerDayManagerItem item : pillItem.getTimesManager().getTimesPerDay())
                {
                    //If there are times for that day add it to string
                    if(item.getTimesList().size() > 0)
                    {
                        sb.append(item.getDay().getStringName().charAt(0));
                        sb.append(item.getDay().getStringName().charAt(1));
                        sb.append(",");
                    }
                }
                sb.deleteCharAt(sb.length() - 1);
                daysToTakeTextView.setText(sb.toString());
                titleTextView.setText(pillItem.getTitle());

                //TODO: Need to account for current day
                StringBuilder nextSb = new StringBuilder();
                List<TimesPerDayManagerItem> tpdItems = pillItem.getTimesManager().getTimesPerDay();
                TimesPerDayManagerItem nextDayItem = null;
                for(TimesPerDayManagerItem item : tpdItems)
                {
                    if(item.getDay().getNumVal() >= dayStoSa && item.getTimesList().size() > 0)
                    {
                        nextDayItem = item;
                        break;
                    }
                }
                int k = 0;
                while(nextDayItem == null && k < tpdItems.size())
                {
                    if(tpdItems.get(k).getTimesList().size() > 0)
                        nextDayItem = tpdItems.get(k);
                    k++;
                }
                nextSb.append("Take Next On: ");

                SimpleTimeItem nextTime = null;

                if(nextDayItem != null) {
                    if (nextDayItem.getDay().getNumVal() != dayStoSa)
                        nextSb.append(nextDayItem.getDay().getStringName());
                    else {
                        //Check if it's today or next week, either to put today or day of week name
                        if (nextDayItem.getTimesList().get(0).getHour24() > today.getHourOfDay())
                            nextSb.append("Today");
                        else if (nextDayItem.getTimesList().get(0).getHour24() < today.getHourOfDay()) {
                            //Check if there is a time that is later on today
                            for (SimpleTimeItem timeItem : nextDayItem.getTimesList()) {
                                //Later hour
                                if (timeItem.getHour24() > today.getHourOfDay()) {
                                    nextTime = timeItem;
                                    break;
                                } else if (timeItem.getHour24() == today.getHourOfDay()) {
                                    //Later minutes of hour
                                    if (timeItem.getMins() >= today.getMinuteOfHour()) {
                                        nextTime = timeItem;
                                        break;
                                    }
                                }
                            }
                            //If we didnt get a later time today, we need to check the next day we take it
                            if (nextTime == null) {
                                if (tpdItems.size() > 1) {
                                    boolean gotDay = false;
                                    //Check rest of week
                                    for (int i = dayStoSa + 1; i < tpdItems.size(); i++) {
                                        if (tpdItems.get(i).getTimesList().size() > 0) {
                                            nextDayItem = tpdItems.get(i);
                                            gotDay = true;
                                            break;
                                        }
                                    }

                                    //Check next week if we didnt find a day
                                    if (!gotDay) {
                                        for (int i = 0; i <= dayStoSa; i++) {
                                            if (tpdItems.get(i).getTimesList().size() > 0) {
                                                nextDayItem = tpdItems.get(i);
                                                break;
                                            }
                                        }
                                    }
                                }
                                nextSb.append(nextDayItem.getDay().getStringName());
                            } else nextSb.append("Today"); //Later time today, append today
                        } else //Compare mins
                        {
                            if (nextDayItem.getTimesList().get(0).getMins() > today.getMinuteOfHour()) {
                                nextSb.append("Today");
                            } else
                                nextSb.append(nextDayItem.getDay().getStringName());
                        }
                    }

                    if (nextDayItem.getTimesList().size() > 0) {
                        nextSb.append(" at ");
                        if (nextTime == null) nextTime = nextDayItem.getTimesList().get(0);
                        String minsString = (nextTime.getMins() >= 10 ? nextTime.getMins() + "" :
                                "0" + nextTime.getMins());
                        nextSb.append(nextTime.getHour() + ":" + minsString +
                                (nextTime.getHour24() > 12 ? "pm" : "am"));
                    }

                    //Set the next time to take text
                    nextTakeTextView.setText(nextSb.toString());
                }

                //nextTakeTextView.setText("Take Next: " + );
                pillsLayout.addView(pillQItem);
            }
        }
        catch(NullPointerException e)
        {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        buildPillsQueue();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onItemSaved() {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
