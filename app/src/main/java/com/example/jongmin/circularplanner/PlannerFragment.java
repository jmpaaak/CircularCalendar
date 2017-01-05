package com.example.jongmin.circularplanner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jongmin.circularplanner.view.CircularPlannerInnerView;
import com.example.jongmin.circularplanner.view.CircularPlannerView;

/**
 * Created by jongmin on 2016-11-30.
 */
public class PlannerFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_POS_NUMBER = "position_number";

    private static Calendar calendarInSelectedDate;
    private static int mSeqNum;

    private Calendar mCalendar;
    private CircularPlannerView mPlannerView;
    private CircularPlannerInnerView mPlannerInnerView;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlannerFragment newInstance(int position, int seqNum, Calendar calendarSelected) {

        PlannerFragment fragment = new PlannerFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_POS_NUMBER, position);
        fragment.setArguments(args);

        mSeqNum = seqNum;
        calendarInSelectedDate = calendarSelected;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_circular_planner, container, false);

        mPlannerInnerView = (CircularPlannerInnerView) rootView.findViewById(R.id.inner_planner);
        mPlannerInnerView.setDateTextContainer((LinearLayout) rootView.findViewById(R.id.date_text_container));
        mPlannerInnerView.setPlanNameTextView((TextView) rootView.findViewById(R.id.plan_name));
        mPlannerInnerView.setPlanTimeTextView((TextView) rootView.findViewById(R.id.plan_time));

        int position = getArguments().getInt(ARG_POS_NUMBER);

        Calendar copy = new GregorianCalendar();
        copy.setTime(calendarInSelectedDate.getTime());

        copy.add(Calendar.DATE, position+mSeqNum);

        mCalendar = copy; // set calender obj at each fragment
        mPlannerView = (CircularPlannerView) rootView.findViewById(R.id.planner);

        // set date each circular planner

        String dayLongName = copy.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        TextView textView = (TextView) rootView.findViewById(R.id.day_text);
        textView.setText(dayLongName);

        textView = (TextView) rootView.findViewById(R.id.year);
        textView.setText(copy.get(Calendar.YEAR)+"");

        textView = (TextView) rootView.findViewById(R.id.month_day);
        SimpleDateFormat dateFormat = new SimpleDateFormat("M.d");
        textView.setText(dateFormat.format(copy.getTime()));

        mPlannerView.setCurCalendar(mCalendar);

        return rootView;
    }

    // getters and setters

    public Calendar getCurFragCalendar() {
        return mCalendar;
    }

    public CircularPlannerView getPlannerView() {
        return mPlannerView;
    }

    public CircularPlannerInnerView getPlannerInnerView() {
        return mPlannerInnerView;
    }
}
