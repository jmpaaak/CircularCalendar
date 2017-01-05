package com.example.jongmin.circularplanner;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jongmin.circularplanner.helper.AlarmService;
import com.example.jongmin.circularplanner.helper.DBHelper;
import com.example.jongmin.circularplanner.helper.Plan;
import com.example.jongmin.circularplanner.helper.SectionsPagerAdapter;
import com.example.jongmin.circularplanner.model.PlanModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;

public class CircularPlannerActivity extends AppCompatActivity {

    private static ComponentName alarmServiceComponet;
    private static final String KEY_PUSH_ON = "IS_PUSH_ON";
    private static final String SVC_COMP_NAME = "SERVICE_COMPONET_NAME";

    private SharedPreferences sharedPreferences; // for Preferencing

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private Calendar calendarSelected;

    private final DBHelper dbHelper = new DBHelper(this, "Plans.db", null, 1);
    private PlanModel planModel;
    private ArrayList<Plan> plans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circular_planner);

        Intent intent = getIntent();

        calendarSelected = (Calendar) intent.getSerializableExtra("selectedDate");
        if(calendarSelected == null)
            calendarSelected = Calendar.getInstance();

//        View plannerViewContainer = findViewById(R.id.container);
//        plannerViewContainer.findViewById(R.id.);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Create the adapter that will return a fragment for each of the seven
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), calendarSelected);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(Math.abs(mSectionsPagerAdapter.getSequenceNum()));

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                PlannerFragment curPlannerFrag = (PlannerFragment) mSectionsPagerAdapter.getRegisteredFragment(position);
                curPlannerFrag.getPlannerView().updateAfterUpdatePlanAtList();
                curPlannerFrag.getPlannerInnerView().setPlanUnselected();
            }

            @Override
            public void onPageSelected(int position) {
                PlannerFragment curPlannerFrag = (PlannerFragment) mSectionsPagerAdapter.getRegisteredFragment(position);
                curPlannerFrag.getPlannerView().updateAfterUpdatePlanAtList();
                curPlannerFrag.getPlannerInnerView().setPlanUnselected();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // set tab indicator Saturday and Sunday text color
        int wantedTabIndex = 5;
        TextView tv = (TextView)(((LinearLayout)((LinearLayout)tabLayout.getChildAt(0)).getChildAt(wantedTabIndex)).getChildAt(1));
        tv.setTextColor(Color.BLUE);
        wantedTabIndex = 6;
        tv = (TextView)(((LinearLayout)((LinearLayout)tabLayout.getChildAt(0)).getChildAt(wantedTabIndex)).getChildAt(1));
        tv.setTextColor(Color.RED);


        // get all plans in db to register notification time
        planModel = new PlanModel();
        planModel.setDBHelper(dbHelper);
        plans = planModel.getResultAll();

        sharedPreferences = getSharedPreferences("PushOn", MODE_PRIVATE);
        if(!sharedPreferences.contains(KEY_PUSH_ON)) { // if pref is null init push on true
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_PUSH_ON, true);

            // background alarm service start when just first time app is started
            Intent intentForAlarmService = new Intent(getApplicationContext(), AlarmService.class);
            intentForAlarmService.putParcelableArrayListExtra("PLAN_ARRAY", plans);
            alarmServiceComponet = startService(intentForAlarmService);

            Gson gson = new Gson();
            String json = gson.toJson(alarmServiceComponet);
            editor.putString(SVC_COMP_NAME, json);

            editor.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cirular_planner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.db_reset) {
            this.deleteDatabase("Plans.db"); // reference to reset db
            finish();
            return true;
        }

        if (id == R.id.push_on_off) {

            Snackbar snackbar;

            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();

            if (!sharedPreferences.getBoolean(KEY_PUSH_ON, true)) {
                editor.putBoolean(KEY_PUSH_ON, !sharedPreferences.getBoolean(KEY_PUSH_ON, true)); // assign opposite value
                snackbar = Snackbar.make(findViewById(R.id.planner_container), "푸쉬알람이 설정되었습니다.", Snackbar.LENGTH_LONG);
                Intent intentForAlarmService = new Intent(getApplicationContext(), AlarmService.class);
                intentForAlarmService.putParcelableArrayListExtra("PLAN_ARRAY", plans);
                alarmServiceComponet = startService(intentForAlarmService);

                String json = gson.toJson(alarmServiceComponet);
                editor.putString(SVC_COMP_NAME, json);

            } else {
                editor.putBoolean(KEY_PUSH_ON, !sharedPreferences.getBoolean(KEY_PUSH_ON, true)); // assign opposite value
                String json = sharedPreferences.getString(SVC_COMP_NAME, null);
                alarmServiceComponet = gson.fromJson(json, ComponentName.class);

                Intent intentForAlarmService = new Intent();
                intentForAlarmService.setComponent(alarmServiceComponet);
                stopService(intentForAlarmService); // stop foreground service
                snackbar = Snackbar.make(findViewById(R.id.planner_container), "푸쉬알람이 해제되었습니다.", Snackbar.LENGTH_LONG);
            }

            editor.commit();


            View mView = snackbar.getView();
            TextView textView = (TextView) mView.findViewById(android.support.design.R.id.snackbar_text);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            else
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
            snackbar.show();

            return true;
        }

        if (id == android.R.id.home)
            finish(); // close this activity and return to preview activity (if there is any)

        return super.onOptionsItemSelected(item);
    }

    // getters and setters
    public Fragment getRegisteredFragment() {
        return mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
    }

    public void refreshAlarmService() {

        plans = planModel.getResultAll(); //  set all plans updated

        if (sharedPreferences.getBoolean(KEY_PUSH_ON, true)) {

            Gson gson = new Gson();
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // remove alarm service

            String json = sharedPreferences.getString(SVC_COMP_NAME, null);
            alarmServiceComponet = gson.fromJson(json, ComponentName.class);

            Intent intentForAlarmServiceEnd = new Intent();
            intentForAlarmServiceEnd.setComponent(alarmServiceComponet);
            stopService(intentForAlarmServiceEnd); // stop foreground service
            // end of remove

            // add new service
            Intent intentForAlarmServiceStart = new Intent(getApplicationContext(), AlarmService.class);
            intentForAlarmServiceStart.putParcelableArrayListExtra("PLAN_ARRAY", plans);
            alarmServiceComponet = startService(intentForAlarmServiceStart);
            json = gson.toJson(alarmServiceComponet);
            editor.putString(SVC_COMP_NAME, json);
            // end of add

            editor.commit();

        }

    }

    public DBHelper getDBHelper() {
        return dbHelper;
    }
}
