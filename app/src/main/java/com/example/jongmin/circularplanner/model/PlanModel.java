package com.example.jongmin.circularplanner.model;
import android.app.PendingIntent;

import com.example.jongmin.circularplanner.helper.DBHelper;
import com.example.jongmin.circularplanner.helper.Plan;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jongmin on 2016-12-03.
 */
public class PlanModel {

    private DBHelper dbHelper;

    public Plan dbAddPlan(String planName, long dateMillis, float startAngle,
                          float endAngle, int color, int selectedRepeatTerm, String repeatGroupId) {
        return dbHelper.insert(planName, dateMillis, startAngle, endAngle, color, selectedRepeatTerm, repeatGroupId);
    }

    public Plan dbUpdatePlan(int id, String planNameUpdated, float startAngleUpdated, float endAngleUpdated,
                                      int colorUpdated, int percentageOfAchieve,
                                      int selectedRepeatTermUpdate, String repeatGroupIdUpdated) {
        Plan plan = dbHelper.update(id, planNameUpdated, startAngleUpdated, endAngleUpdated,
                colorUpdated, percentageOfAchieve, selectedRepeatTermUpdate, repeatGroupIdUpdated);

        return plan;
    }

    public void dbDeletePlan(int id) {
        dbHelper.delete(id);
    }

    public void dbDeletePlanWithRepeatGroupId(Plan firstPlanDeleted) {
        dbHelper.deleteWithRepeatGroupId(firstPlanDeleted);
    }

    public ArrayList<Plan> getResultWithDateInMillis(long dateInMillis) {
        return dbHelper.getResultWithDateInMillis(dateInMillis);
    }

    public ArrayList<Plan> getResultWithRepeatGroup(Plan planSelected) {
        return dbHelper.getResultWithRepeatGroupId(planSelected);
    }

    public ArrayList<Plan> getResultAll() {
        return dbHelper.getResultAll();
    }

    public void deleteWithRepeatGroupId(Plan selected) {
        dbHelper.deleteWithRepeatGroupId(selected);
    }


    // setters and getters

    public void setDBHelper(DBHelper DBHelper) {
        this.dbHelper = DBHelper;
    }


}
