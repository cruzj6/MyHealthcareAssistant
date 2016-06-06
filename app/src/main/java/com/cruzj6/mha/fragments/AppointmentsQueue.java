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

import com.cruzj6.mha.models.AppointmentItem;
import com.cruzj6.mha.R;
import com.cruzj6.mha.dataManagement.DatabaseManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AppointmentsQueue.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AppointmentsQueue#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AppointmentsQueue extends Fragment {
    private static HorizontalScrollView scrollView;

    private OnFragmentInteractionListener mListener;

    public AppointmentsQueue() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static AppointmentsQueue newInstance(String param1, String param2) {
        AppointmentsQueue fragment = new AppointmentsQueue();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //TODO: Sort by date
        //Inflate the view
        View theView =  inflater.inflate(R.layout.fragment_appointments_queue, container, false);

        //Get the scrollview's linear layout
        LinearLayout apptsLayout = (LinearLayout) theView.findViewById(R.id.linearlayout_appts_scrollview);

        //Get our appointment data from the database and go through each one
        List<AppointmentItem> apptsList = new DatabaseManager(getContext()).loadAppointmentItems();
        for(AppointmentItem item : apptsList)
        {
            final AppointmentItem apptItemFinal = item;
            //Inflate the view for the item and get each control/view
            View layoutItem = getActivity().getLayoutInflater().inflate(R.layout.layout_appointment_queue_item, null);
            TextView itemTitleLabel = (TextView) layoutItem.findViewById(R.id.textview_apptq_item_title);
            TextView itemDateLabel = (TextView) layoutItem.findViewById(R.id.textview_apptq_item_date);
            TextView itemTimeLabel = (TextView) layoutItem.findViewById(R.id.textview_apptq_item_time);

            //Convert our date from out item, and format and set it for the date and time
            Date date = new Date(item.getApptDate()*1000);
            SimpleDateFormat f = new SimpleDateFormat("MM/dd/yyyy");
            itemDateLabel.setText(f.format(date));
            f = new SimpleDateFormat("hh:mm aaa");
            itemTimeLabel.setText(f.format(date));

            //Set the title
            itemTitleLabel.setText(item.getAppointmentTitle());


            layoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Give it the id of this item so it can get it from the db to edit
                    AppointmentSettingsDialog dialog = new AppointmentSettingsDialog();
                    Bundle args = new Bundle();
                    args.putSerializable("mode", AppointmentSettingsDialog.SettingsTypes.EDIT_EXISTING);
                    args.putLong("id", apptItemFinal.getApptId());
                    dialog.setArguments(args);
                    dialog.show(((AppCompatActivity)getContext()).getSupportFragmentManager(), "Appointment Settings");
                }
            });
            //Finally add it to the layout
            apptsLayout.addView(layoutItem);
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
        mListener = null;
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
