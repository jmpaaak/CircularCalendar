package com.example.jongmin.circularplanner.helper;

/**
 * Created by jongmin on 2016-12-16.
 */

import android.os.Handler;
import android.os.Message;

import com.example.jongmin.circularplanner.model.PlanModel;

import java.util.ArrayList;
import java.util.Calendar;

public class AlarmServiceThread extends Thread{
    private Handler handler;
    private boolean isRun = true;

    public static ArrayList<Plan> plans;

    public AlarmServiceThread(Handler handler, ArrayList plans){
        this.handler = handler;
        this.plans = plans;
    }

    public void stopForever(){
        synchronized (this) {
            this.isRun = false;
        }
    }

    public void run() {

        while(isRun){

            for(Plan p : plans) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(p.dateInMillis);

                if(calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                        calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                        calendar.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) { // check plan is today's

                    calendar.setTimeInMillis(p.dateInMillis);

                    /* represent of minute by unit 5 */
                    int curMinute = Plan.getMinuteUnitFive(p.startAngle);

                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                            (int) Math.floor(p.startAngle / 15), curMinute, 0);

                    Calendar curTimeCalendar = Calendar.getInstance();
                    curTimeCalendar.set(curTimeCalendar.get(Calendar.YEAR), curTimeCalendar.get(Calendar.MONTH), curTimeCalendar.get(Calendar.DAY_OF_MONTH),
                            curTimeCalendar.get(Calendar.HOUR_OF_DAY), curTimeCalendar.get(Calendar.MINUTE), 0);

                    if (calendar.get(Calendar.HOUR_OF_DAY) == curTimeCalendar.get(Calendar.HOUR_OF_DAY)
                            && calendar.get(Calendar.MINUTE) == curTimeCalendar.get(Calendar.MINUTE)) {
                        Message msg = handler.obtainMessage();
                        msg.obj = p.planName;
                        handler.sendMessage(msg);
                    }
                }

            }

            try {
                Thread.sleep(60000); // 1분씩 쉰다.
            } catch (Exception e) {}

        }
    }

}