package com.example.jongmin.circularplanner.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by jongmin on 2016-12-03.
 */

public class DBHelper extends SQLiteOpenHelper {

    private Context context;


    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        this.context = context;
    }

//    public void setBroadcastSenderMap(ArrayList<Plan> plans) {
//        broadcastSenderMap = new HashMap<>();
//
//        /* add alarm for all plans */
//        for(Plan planAdded : plans) {
//            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            Intent intent = new Intent(context, AlarmBroadcast.class);
//            intent.putExtra("planName", planAdded.planName);
//
//            broadcastSenderMap.put(planAdded.id, PendingIntent.getBroadcast(context, planAdded.id, intent, PendingIntent.FLAG_UPDATE_CURRENT));
//
//            Calendar calendar = Calendar.getInstance();
//
//            calendar.setTimeInMillis(planAdded.dateInMillis);
//            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
//                    (int) Math.floor(planAdded.startAngle / 15), (int) ((planAdded.startAngle / 15 - (int) Math.floor(planAdded.startAngle / 15)) * 60), 0);
//
//            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), broadcastSenderMap.get(planAdded.id));
//        }
//    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블 생성
        db.execSQL("CREATE TABLE PLANS (_id INTEGER PRIMARY KEY AUTOINCREMENT, planName TEXT, dateInMillis LONG, startAngle REAL, " +
                "endAngle REAL, color INTEGER, percentageOfAchieve INTEGER, repeatTerm INTEGER, repeatGroupId TEXT);");
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Plan insert(String planName, long dateInMillis, float startAngle,
                       float endAngle, int color, int selectedRepeatTerm, String repeatGroupId) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO PLANS VALUES(null, '" + planName + "', " + dateInMillis + ", " + startAngle + ", "
                + endAngle + ", " + color + ", 0, " + selectedRepeatTerm + ", '" + repeatGroupId + "');");

        Cursor cursor = db.rawQuery("SELECT * FROM PLANS ORDER BY _id DESC LIMIT 1;", null);

        Plan planAdded = new Plan();

        while (cursor.moveToNext()) {
            planAdded.id = cursor.getInt(0);
            planAdded.planName = cursor.getString(1);
            planAdded.dateInMillis = cursor.getLong(2);
            planAdded.startAngle = cursor.getFloat(3);
            planAdded.endAngle = cursor.getFloat(4);
            planAdded.color = cursor.getInt(5);
            planAdded.percentageOfAchieve = cursor.getInt(6);
            planAdded.repeatTerm = cursor.getInt(7);
            planAdded.repeatGroupId = cursor.getString(8);

            db.close();
        }

        Log.i("plan added name: ",""+planAdded.planName);

        return planAdded;
    }

    public Plan update(int id, String planNameUpdated, float startAngleUpdated,
                       float endAngleUpdated, int colorUpdated,
                       int percentageOfAchieveUpdated,
                       int selectedRepeatTermUpdated, String repeatGroupIdUpdated) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("UPDATE PLANS SET planName='" + planNameUpdated + "', startAngle=" + startAngleUpdated +
                ", endAngle=" + endAngleUpdated + ", color=" + colorUpdated + ", percentageOfAchieve=" + percentageOfAchieveUpdated +
                ", repeatTerm=" + selectedRepeatTermUpdated + ", repeatGroupId='" + repeatGroupIdUpdated +
                "' WHERE _id=" + id + ";");


        Cursor cursor = db.rawQuery("SELECT * FROM PLANS WHERE planName='" + planNameUpdated + "' AND startAngle=" + startAngleUpdated + ";", null);
        Plan plan = new Plan();
        cursor.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });
        while (cursor.moveToNext()) { /* only updated one */
            plan.id = cursor.getInt(0);
            plan.planName = cursor.getString(1);
            plan.dateInMillis = cursor.getLong(2);
            plan.startAngle = cursor.getFloat(3);
            plan.endAngle = cursor.getFloat(4);
            plan.color = cursor.getInt(5);
            plan.percentageOfAchieve = cursor.getInt(6);
            plan.repeatTerm = cursor.getInt(7);
            plan.repeatGroupId = cursor.getString(8);

            db.close();
        }

        return plan;
    }

    public void delete(int id) {
        SQLiteDatabase db = getWritableDatabase();

        // 입력한 id와 일치하는 행 삭제
        db.execSQL("DELETE FROM PLANS WHERE _id=" + id + ";");

        db.close();
    }

    public void deleteWithRepeatGroupId(Plan firstPlanDeleted) {
        SQLiteDatabase db = getWritableDatabase();

        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(firstPlanDeleted.dateInMillis);

        // 입력한 repeatGroupId와 일치하고 선택 목표와 함께 이후 목표들의 행 삭제
        db.execSQL("DELETE FROM PLANS WHERE repeatGroupId='" + firstPlanDeleted.repeatGroupId + "' AND dateInMillis >=" +
               + firstPlanDeleted.dateInMillis + ";");

        db.close();
    }

    public ArrayList<Plan> getResultWithDateInMillis(long dateInMillis) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Plan> arrayList = new ArrayList<Plan>();

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용
        Cursor cursor = db.rawQuery("SELECT * FROM PLANS WHERE dateInMillis='" + dateInMillis + "' ORDER BY startAngle ASC;", null);
        while (cursor.moveToNext()) {

            Plan plan = new Plan();
            plan.id = cursor.getInt(0);
            plan.planName = cursor.getString(1);
            plan.dateInMillis = cursor.getLong(2);
            plan.startAngle = cursor.getFloat(3);
            plan.endAngle = cursor.getFloat(4);
            plan.color = cursor.getInt(5);
            plan.percentageOfAchieve = cursor.getInt(6);
            plan.repeatTerm = cursor.getInt(7);
            plan.repeatGroupId = cursor.getString(8);

            arrayList.add(plan);
        }

        return arrayList;
    }

    public ArrayList<Plan> getResultWithRepeatGroupId(Plan planSelected) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Plan> arrayList = new ArrayList<Plan>();

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용
        Cursor cursor = db.rawQuery("SELECT * FROM PLANS WHERE repeatGroupId='" + planSelected.repeatGroupId
                + "' AND dateInMillis >=" +
                + planSelected.dateInMillis + " ORDER BY dateInMillis ASC;", null);
        while (cursor.moveToNext()) {

            Plan plan = new Plan();
            plan.id = cursor.getInt(0);
            plan.planName = cursor.getString(1);
            plan.dateInMillis = cursor.getLong(2);
            plan.startAngle = cursor.getFloat(3);
            plan.endAngle = cursor.getFloat(4);
            plan.color = cursor.getInt(5);
            plan.percentageOfAchieve = cursor.getInt(6);
            plan.repeatTerm = cursor.getInt(7);
            plan.repeatGroupId = cursor.getString(8);

            arrayList.add(plan);
        }

        return arrayList;
    }

    public ArrayList<Plan> getResultAll() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Plan> arrayList = new ArrayList<>();

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용
        Cursor cursor = db.rawQuery("SELECT * FROM PLANS ORDER BY dateInMillis ASC, startAngle ASC;", null);
        while (cursor.moveToNext()) {

            Plan plan = new Plan();
            plan.id = cursor.getInt(0);
            plan.planName = cursor.getString(1);
            plan.dateInMillis = cursor.getLong(2);
            plan.startAngle = cursor.getFloat(3);
            plan.endAngle = cursor.getFloat(4);
            plan.color = cursor.getInt(5);
            plan.percentageOfAchieve = cursor.getInt(6);
            plan.repeatTerm = cursor.getInt(7);
            plan.repeatGroupId = cursor.getString(8);

            arrayList.add(plan);
        }

        return arrayList;
    }
}

