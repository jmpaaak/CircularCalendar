package com.example.jongmin.circularplanner.helper;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.example.jongmin.circularplanner.PlannerFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    private Calendar mCalendar; // calendar of selected date
    private int dayOfWeek;
    private int seqNum;

    public SectionsPagerAdapter(FragmentManager fm, Calendar calendarSelected) {
        super(fm);
        mCalendar = calendarSelected;

        dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
        seqNum = 0;

        if (Calendar.MONDAY == dayOfWeek) seqNum = 0;
        else if (Calendar.TUESDAY == dayOfWeek) seqNum = -1;
        else if (Calendar.WEDNESDAY == dayOfWeek) seqNum = -2;
        else if (Calendar.THURSDAY == dayOfWeek) seqNum = -3;
        else if (Calendar.FRIDAY == dayOfWeek) seqNum = -4;
        else if (Calendar.SATURDAY == dayOfWeek) seqNum = -5;
        else if (Calendar.SUNDAY == dayOfWeek) seqNum = -6;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).

        return PlannerFragment.newInstance(position, seqNum, mCalendar);
    }

    @Override
    public int getCount() {
        // Show 7 total pages.
        return 7;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        Calendar copy = new GregorianCalendar();
        copy.setTime(mCalendar.getTime());

        copy.add(Calendar.DATE, seqNum+position);
        switch (position) {
            case 0:
                return copy.get(Calendar.DAY_OF_MONTH)+"\n월";
            case 1:
                return copy.get(Calendar.DAY_OF_MONTH)+"\n화";
            case 2:
                return copy.get(Calendar.DAY_OF_MONTH)+"\n수";
            case 3:
                return copy.get(Calendar.DAY_OF_MONTH)+"\n목";
            case 4:
                return copy.get(Calendar.DAY_OF_MONTH)+"\n금";
            case 5:
                return copy.get(Calendar.DAY_OF_MONTH)+"\n토";
            case 6:
                return copy.get(Calendar.DAY_OF_MONTH)+"\n일";
        }
        return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    public int getSequenceNum() {
        return seqNum;
    }
}