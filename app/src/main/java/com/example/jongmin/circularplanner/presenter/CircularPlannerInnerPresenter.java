package com.example.jongmin.circularplanner.presenter;

import android.util.Log;

import com.example.jongmin.circularplanner.helper.DBHelper;
import com.example.jongmin.circularplanner.helper.Plan;
import com.example.jongmin.circularplanner.model.PlanModel;
import com.example.jongmin.circularplanner.view.CircularPlannerInnerView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by jongmin on 2016-12-03.
 */

public class CircularPlannerInnerPresenter {

    private PlanModel mPlanModel;
    private CircularPlannerInnerView mPlannerInnerView;

    public CircularPlannerInnerPresenter() {
        mPlanModel = new PlanModel();
    }

    public void setViewAndDBHelper(CircularPlannerInnerView mPlannerInnerView, DBHelper dbHelper) {
        this.mPlannerInnerView = mPlannerInnerView;
        mPlanModel.setDBHelper(dbHelper);
    }

    public void addAchievementAtSelectedPlan(int percentageOfAchieve) {
        Plan sPlan = mPlannerInnerView.getPlanSelected();
        Plan newPlan = mPlanModel.dbUpdatePlan(sPlan.id, sPlan.planName, sPlan.startAngle, sPlan.endAngle,
                sPlan.color, percentageOfAchieve, sPlan.repeatTerm, sPlan.repeatGroupId);
        mPlannerInnerView.setPlanSelected(newPlan);
    }

    public ArrayList<Plan> getPlansWithDateMillis(long selectedDateInMillis) {
        return mPlanModel.getResultWithDateInMillis(selectedDateInMillis);
    }

//    public DBHelper.Plan getPlanFirstInGroup() {
//        DBHelper.Plan curPlan = mPlannerInnerView.getPlanSelected();
//        ArrayList planGroup = mPlanModel.getResultWithRepeatGroup(curPlan.repeatGroupId);
//
//        Calendar c = Calendar.getInstance();
//        c.setTimeInMillis(((DBHelper.Plan) planGroup.get(0)).dateInMillis);
//
//        // set plan selected with first plan in group belong to date
//        return ((DBHelper.Plan) planGroup.get(0));
//    }

    public CircularPlannerInnerView getView() {
        return mPlannerInnerView;
    }
}

// usage : new ColorPickerDialog(FingerPaintActivity.this, FingerPaintActivity.this, mPaint.getColor()).show();