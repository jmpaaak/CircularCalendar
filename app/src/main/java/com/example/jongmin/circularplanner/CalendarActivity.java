package com.example.jongmin.circularplanner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;

public class CalendarActivity extends AppCompatActivity {

    BottomSheetBehavior mBottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set calendar of unit month
        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
        Calendar c = Calendar.getInstance();
        calendarView.setDate(c.getTimeInMillis()); // temporally select today

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);


        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int year, int month, int dayOfMonth) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                // convert to Planner activity with Calendar
                Calendar calendar = new GregorianCalendar( year, month, dayOfMonth );
                Intent intentPlannerActivity = new Intent(CalendarActivity.this, CircularPlannerActivity.class);
                intentPlannerActivity.putExtra("selectedDate", calendar);
                startActivity(intentPlannerActivity);

            }
        });

        // direct call planner activity with today date
        Intent intentReceived = getIntent();
        if(intentReceived != null && intentReceived.getBooleanExtra("GO_TO_PLANNER_ACTIVITY_FLAG", false) == true) {
            // convert to Planner activity with Calendar
            Calendar calendar = new GregorianCalendar(
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    );
            Intent intentPlannerActivity = new Intent(CalendarActivity.this, CircularPlannerActivity.class);
            intentPlannerActivity.putExtra("selectedDate", calendar);
            startActivity(intentPlannerActivity);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_calendar, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
