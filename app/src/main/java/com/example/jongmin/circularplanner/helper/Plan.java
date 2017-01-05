package com.example.jongmin.circularplanner.helper;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by jongmin on 2016-12-16.
 */
public class Plan implements Parcelable {
    public int id;
    public String planName;
    public long dateInMillis;
    public float startAngle;
    public float endAngle;
    public int color;
    public int percentageOfAchieve;
    public int repeatTerm;
    public String repeatGroupId;

    public Plan() {}

    public Plan(Parcel parcel) {
        id = parcel.readInt();
        planName = parcel.readString();
        dateInMillis = parcel.readLong();
        startAngle = parcel.readFloat();
        endAngle = parcel.readFloat();
        color = parcel.readInt();
        percentageOfAchieve = parcel.readInt();
        repeatTerm = parcel.readInt();
        repeatGroupId = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(planName);
        parcel.writeLong(dateInMillis);
        parcel.writeFloat(startAngle);
        parcel.writeFloat(endAngle);
        parcel.writeInt(color);
        parcel.writeInt(percentageOfAchieve);
        parcel.writeInt(repeatTerm);
        parcel.writeString(repeatGroupId);
    }

    //Parcelable 객체로 구현하기 위한 Parcelable Method ArrayList구현 등..
    public static final Creator<Plan> CREATOR = new Creator<Plan>() {

        @Override
        public Plan createFromParcel(Parcel source) {
            return new Plan(source);
        }

        @Override
        public Plan[] newArray(int size) {
            return new Plan[size];
        }
    };

    public static int getMinuteUnitFive(float angle) {
        /* represent of minute by unit 5 */
        int unitMinute = (int) ((angle / 15 - (int) Math.floor(angle / 15)) * 60);

        Log.i("minute: ",""+unitMinute);


        if(unitMinute % 10 > 3 && unitMinute % 10  <= 7) {
            unitMinute = (int) (Math.floor(unitMinute / 10) * 10);
            unitMinute += 5;
        } else {
            unitMinute = Math.round(((float)unitMinute) / 10) * 10;
            if(unitMinute == 60)
                unitMinute = 55;
        }

        Log.i("angle: ",""+angle);
        Log.i("unitMinute: ",""+unitMinute);

        return unitMinute;
    }

}
