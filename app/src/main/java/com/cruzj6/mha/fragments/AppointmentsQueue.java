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
import com.cruzj6.mha.models.ItemSettingsInvokeHandler;
import com.cruzj6.mha.models.SettingsTypes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
public class AppointmentsQueue extends Fragment implements ItemSettingsInvokeHandler{
    private HorizontalScrollView scrollView;

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
        View theView =  inflater.inflate(R.layout.fragment_queue, container, false);

        return theView;
    }

    private void buildAppointmentQueue()
    {
        View theView = getView();

        //Get the scrollview's linear layout
        LinearLayout apptsLayout = (LinearLayout) theView.findViewById(R.id.linearlayout_queue_scrollview);
        apptsLayout.removeAllViews();
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
            f.applyPattern("hh:mm aaa");
            itemTimeLabel.setText(f.format(date));

            //Set the title
            itemTitleLabel.setText(item.getAppointmentTitle());


            layoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Give it the id of this item so it can get it from the db to edit
                    AppointmentSettingsDialog dialog = new AppointmentSettingsDialog();
                    dialog.setOnSaveHandler(AppointmentsQueue.this);
                    Bundle args = new Bundle();
                    args.putSerializable("mode", SettingsTypes.EDIT_EXISTING);
                    args.putLong("id", apptItemFinal.getApptId());
                    dialog.setArguments(args);
                    dialog.show(((AppCompatActivity)getContext()).getSupportFragmentManager(), "Appointment Settings");
                }
            });

            //Set up labwork label if needed
            if(item.getRequiresLabWork())
            {
                String labworkString = getString(R.string.labwork);
                TextView labworkLabel = (TextView) layoutItem.findViewById(R.id.textview_labwork_by);

                //Get what day the labwork needs to be done by
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                long timeAdd = item.getLabworkDaysBefore();
                c.add(Calendar.DATE, -(int)timeAdd);
                f.applyPattern("MM/dd/yy");

                //Set up the labwork label with this data
                labworkLabel.setVisibility(View.VISIBLE);
                labworkLabel.setText(labworkString + " " + f.format(c.getTime()));
            }

            //Finally add it to the layout
            apptsLayout.addView(layoutItem);
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();
        buildAppointmentQueue();

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

    @Override
    public void onItemSaved() {
        buildAppointmentQueue();
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
