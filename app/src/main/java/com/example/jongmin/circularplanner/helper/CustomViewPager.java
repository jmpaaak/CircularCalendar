package com.example.jongmin.circularplanner.helper;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.example.jongmin.circularplanner.view.CircularPlannerView;

/**
 * Created by jongmin on 2016-12-02.
 */

public class CustomViewPager extends ViewPager {

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false; // this makes paging from border of circular planner
    }
}