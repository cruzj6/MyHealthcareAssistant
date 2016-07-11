package com.cruzj6.mha.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cruzj6.mha.R;
import com.cruzj6.mha.dataManagement.DatabaseManager;
import com.cruzj6.mha.models.AppointmentItem;
import com.cruzj6.mha.models.PillItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Joey on 6/6/16.
 */
public class PillsQueue extends Fragment {
    private HorizontalScrollView scrollView;

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
        View theView =  inflater.inflate(R.layout.fragment_queue, container, false);

        //Get the scrollview's linear layout
        LinearLayout pillsLayout = (LinearLayout) theView.findViewById(R.id.linearlayout_queue_scrollview);

        List<PillItem> pillItems = new DatabaseManager(getContext()).loadPillItems();
        for(PillItem pillItem : pillItems)
        {
            View pillQItem = inflater.inflate(R.layout.layout_pill_queue_item, container, false);
            TextView titleTextView = (TextView) pillQItem.findViewById(R.id.textview_pillq_item_title);


            titleTextView.setText(pillItem.getTitle());
            pillsLayout.addView(pillQItem);
        }

        return theView;
    }

    @Override
    public void onResume()
    {
        super.onResume();


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
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
