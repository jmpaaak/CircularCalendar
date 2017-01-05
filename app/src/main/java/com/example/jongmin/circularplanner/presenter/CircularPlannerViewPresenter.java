package com.example.jongmin.circularplanner.presenter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;

import java.util.Calendar;

import com.example.jongmin.circularplanner.CircularPlannerActivity;
import com.example.jongmin.circularplanner.helper.AlarmServiceThread;
import com.example.jongmin.circularplanner.helper.DBHelper;
import com.example.jongmin.circularplanner.helper.Plan;
import com.example.jongmin.circularplanner.model.PlanModel;
import com.example.jongmin.circularplanner.view.CircularPlannerView;

import java.util.ArrayList;

/**
 * Created by jongmin on 2016-12-04.
 */

public class CircularPlannerViewPresenter {

    private PlanModel mPlanModel;
    private CircularPlannerView mPlannerView;

    public CircularPlannerViewPresenter() {
        mPlanModel = new PlanModel();
    }

    public void setViewAndDBHelper(CircularPlannerView plannerView, DBHelper dbHelper) {
        this.mPlannerView = plannerView;
        mPlanModel.setDBHelper(dbHelper);
    }

    public Plan addPlan(String planName, long dateMillis, float startAngle,
                        float endAngle, int color, int selectedRepeatTerm, String repeatGroupId) {

        Plan planAdded = mPlanModel.dbAddPlan(planName, dateMillis, startAngle,
                endAngle, color, selectedRepeatTerm, repeatGroupId);

        AlarmServiceThread.plans = mPlanModel.getResultAll();

//        CircularPlannerActivity circularPlannerActivity = (CircularPlannerActivity) mPlannerView.getContext();
//        circularPlannerActivity.refreshAlarmService();

        return planAdded;

//        /* add alarm */
//        AlarmManager am = (AlarmManager) mPlannerView.getContext().getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(mPlannerView.getContext(), AlarmBroadcast.class);
//        intent.putExtra("planName", planName);
//
//        broadcastSenderArr.add(planAdded.id, PendingIntent.getBroadcast(mPlannerView.getContext(), planAdded.id, intent, 0));
//
//        Calendar calendar = Calendar.getInstance();
//
//        calendar.setTimeInMillis(dateMillis);
//        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
//                (int) Math.floor(startAngle/15), (int) ((startAngle/15 - (int) Math.floor(startAngle/15)) * 60), 0);
//
////        Log.i("Hour:", ((int) Math.floor(startAngle/15))+"");
////        Log.i("Minute:", (int) ((startAngle/15 - (int) Math.floor(startAngle/15)) * 60)+"");
//
//        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), broadcastSenderArr.get(planAdded.id));
    }

    public Plan addPlan(Plan p) {

        Plan planAdded = mPlanModel.dbAddPlan(p.planName, p.dateInMillis, p.startAngle,
                p.endAngle, p.color, p.repeatTerm, p.repeatGroupId);

        AlarmServiceThread.plans = mPlanModel.getResultAll();

        return planAdded;
    }

    public void updateAfterAddPlanToList(Plan planUpdated) {
        mPlannerView.updateAfterAddPlanToList(planUpdated);
    }


    public boolean updatePlan(int id, Plan updatePlan) {

        if(updatePlan.repeatGroupId == "") {
            mPlanModel.dbUpdatePlan(id, updatePlan.planName, updatePlan.startAngle, updatePlan.endAngle,
                    updatePlan.color, updatePlan.percentageOfAchieve,
                    updatePlan.repeatTerm, updatePlan.repeatGroupId);
        } else { // group update after checking duplicate

            // check updated time is already consisted
            ArrayList<Plan> plans = mPlanModel.getResultWithRepeatGroup(updatePlan);
            for(Plan p : plans) {
                // first condition is duplication check is skipped for same group of updated plan
                // three conditions needed,
                // 1. startAngle is in plan already added
                // 2. endAngle is in plan already added
                // 3. startAngle is smaller than startAngle is in plan already added
                //    && endAngle is bigger than endAngle is in plan already added
                if (!p.repeatGroupId.equals(updatePlan.repeatGroupId) &&

                        (
                            p.startAngle < updatePlan.endAngle && p.endAngle > updatePlan.endAngle ||
                            p.startAngle < updatePlan.startAngle && p.endAngle > updatePlan.startAngle ||
                            p.startAngle > updatePlan.startAngle && p.endAngle < updatePlan.endAngle
                        )

                    ) {

                    return false;
                }
            }

            for (Plan p : plans) {
                mPlanModel.dbUpdatePlan(p.id, updatePlan.planName, updatePlan.startAngle, updatePlan.endAngle,
                        updatePlan.color, p.percentageOfAchieve,
                        updatePlan.repeatTerm, updatePlan.repeatGroupId);
            }


        }

        AlarmServiceThread.plans = mPlanModel.getResultAll();

        return true;


//        CircularPlannerActivity circularPlannerActivity = (CircularPlannerActivity) mPlannerView.getContext();
//        circularPlannerActivity.refreshAlarmService();

//        /* delete alarm and add alarm*/
//        AlarmManager am = (AlarmManager) mPlannerView.getContext().getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(mPlannerView.getContext(), AlarmBroadcast.class);
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(mPlannerView.getContext(), id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        am.cancel(pendingIntent);
//
//        Intent intent2 = new Intent(mPlannerView.getContext(), AlarmBroadcast.class);
//        intent2.putExtra("planName", updatePlan.planName);
//        Log.i("planName", updatePlan.planName+" id:"+id);
//
//        mPlanModel.getBroadcastSenderMap().put(id, PendingIntent.getBroadcast(mPlannerView.getContext(), id, intent2, PendingIntent.FLAG_UPDATE_CURRENT));
//
//
//        Log.i("brod: ", mPlanModel.getBroadcastSenderMap().get(id).toString());
//
//        Calendar calendar = Calendar.getInstance();
//
//        calendar.setTimeInMillis(updatePlan.dateInMillis);
//        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
//                (int) Math.floor(updatePlan.startAngle/15), (int) ((updatePlan.startAngle/15 - (int) Math.floor(updatePlan.startAngle/15)) * 60), 0);
//
////        Log.i("Hour:", ((int) Math.floor(startAngle/15))+"");
////        Log.i("Minute:", (int) ((startAngle/15 - (int) Math.floor(startAngle/15)) * 60)+"");
//
//        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), mPlanModel.getBroadcastSenderMap().get(id));

    }

    public void deletePlan(Plan planDeleted) {

        if(planDeleted.repeatGroupId.equals(""))
        mPlanModel.dbDeletePlan(planDeleted.id);
        else {
            // delete all with repeatGroupId
            mPlanModel.dbDeletePlanWithRepeatGroupId(planDeleted);
        }

        AlarmServiceThread.plans = mPlanModel.getResultAll();

//        CircularPlannerActivity circularPlannerActivity = (CircularPlannerActivity) mPlannerView.getContext();
//        circularPlannerActivity.refreshAlarmService();

//        /* delete alarm */
//        AlarmManager am = (AlarmManager) mPlannerView.getContext().getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(mPlannerView.getContext(), AlarmBroadcast.class);
//
//        mPlanModel.getBroadcastSenderMap().remove(planDeleted.id);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(mPlannerView.getContext(), planDeleted.id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        am.cancel(pendingIntent);

    }


    public ArrayList<Plan> getPlansWithDateMillis(long selectedDateInMillis) {
        return mPlanModel.getResultWithDateInMillis(selectedDateInMillis);
    }

    public ArrayList<Plan> getResultWithRepeatGroup(Plan selectedPlan) {
        return mPlanModel.getResultWithRepeatGroup(selectedPlan);
    }

    public Plan getPlanSelected() {
        return mPlannerView.getPlannerInner().getPlanSelected();
    }

    public Calendar getCurCalendar() {
        return mPlannerView.getCurCalednar();
    }

    public ArrayList<Plan> getPlanList() {
        return mPlannerView.getPlanList();
    }

    public ArrayList<Plan> getResultAll() {
        return mPlanModel.getResultAll();
    }
}
