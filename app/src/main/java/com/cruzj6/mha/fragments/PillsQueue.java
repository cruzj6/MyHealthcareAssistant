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
import com.cruzj6.mha.models.TimesPerDayManager;
import com.cruzj6.mha.models.TimesPerDayManagerItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
            for (final PillItem pillItem : pillItems) {
                View pillQItem = getActivity().getLayoutInflater().inflate(R.layout.layout_pill_queue_item, null);
                TextView titleTextView = (TextView) pillQItem.findViewById(R.id.textview_pillq_item_title);
                TextView nextTakeTextView = (TextView) pillQItem.findViewById(R.id.textview_pillq_item_time);
                StringBuilder sb = new StringBuilder();
                sb.append("Take Next: ");
                
                //TODO: TimesPerDayManager get sort by time info
                Date nowDate = new Date();
                TimesPerDayManager timesManager = pillItem.getTimesManager();
                for(TimesPerDayManagerItem item : timesManager.getTimesPerDay())
                {
                    //Order by closest to today's date
                }
                pillQItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PillSettingsDialog psd = new PillSettingsDialog();
                        Bundle b = new Bundle();
                        b.putLong("id", pillItem.getPillId());
                        b.putSerializable("mode", SettingsTypes.EDIT_EXISTING);
                        psd.setItemSettingsInvokeHandler(PillsQueue.this);
                        psd.setArguments(b);
                        psd.show(getActivity().getSupportFragmentManager(), "Pill Settings");
                    }
                });

                titleTextView.setText(pillItem.getTitle());
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
