package com.example.jongmin.circularplanner.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.example.jongmin.circularplanner.CalendarActivity;
import com.example.jongmin.circularplanner.R;


public class AlarmService extends Service {
    NotificationManager Notifi_M;
    AlarmServiceThread thread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        thread = new AlarmServiceThread(handler, intent.getParcelableArrayListExtra("PLAN_ARRAY"));
        thread.start();

        Intent intentAppStart = new Intent(AlarmService.this, CalendarActivity.class);
        intent.putExtra("GO_TO_PLANNER_ACTIVITY_FLAG", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(AlarmService.this, 0, intentAppStart, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder bBuilder = new NotificationCompat.Builder(
                getBaseContext()).setSmallIcon(R.drawable.push_small_icon)
                .setContentTitle("Circular Plandar")
                .setPriority(Notification.PRIORITY_MIN)
                .setContentText("목표를 잊지 말고 체크하세요!")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(false);
        Notification notification = bBuilder.build();
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;

        startForeground(startId, notification);

        return START_STICKY;
    }

    // 서비스가 종료될 때 할 작업

    public void onDestroy() {
        thread.stopForever();
        thread = null; // 쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent(AlarmService.this, CalendarActivity.class);
            intent.putExtra("GO_TO_PLANNER_ACTIVITY_FLAG", true);
            PendingIntent pendingIntent = PendingIntent.getActivity(AlarmService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder builder = new Notification.Builder(getApplication());
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.push_icon))
                    .setSmallIcon(R.drawable.push_small_icon)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle((String) msg.obj).setContentText("목표를 시작할 준비가 끝나셨나요?")
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT < 16) {
                Notifi_M.notify(777, builder.getNotification());
            } else {
                Notifi_M.notify(777, builder.build());
            }
        }
    }
}
