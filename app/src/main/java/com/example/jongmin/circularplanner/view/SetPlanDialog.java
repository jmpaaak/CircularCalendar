package com.example.jongmin.circularplanner.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jongmin.circularplanner.CircularPlannerActivity;
import com.example.jongmin.circularplanner.R;
import com.example.jongmin.circularplanner.helper.DBHelper;
import com.example.jongmin.circularplanner.helper.Plan;
import com.example.jongmin.circularplanner.presenter.CircularPlannerInnerPresenter;
import com.example.jongmin.circularplanner.presenter.CircularPlannerViewPresenter;

import java.util.UUID;

/**
 * Created by jongmin on 2016-12-03.
 */

public class SetPlanDialog extends Dialog {

    private final long dateInMillis;
    private final float startAngle;
    private final float endAngle;
    private Context context;

    private final String[] repeatTextArray = {"반복 없음", "매일 반복","매주 반복","매달 반복"};

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    private OnColorChangedListener mListener;
    private int mInitialColor;

    private enum RepeatTerm { NONE, DAY, WEEK, MONTH };
    private  int mPlanColor;
    private String mPlanName;
    private int selectedRepeatTerm;

    CircularPlannerViewPresenter mPresenter;

    private ProgressDialog loadingDialog; // 로딩화면

    private Calendar copyCurCalendar;

    private LinearLayout dialogLinearLayout;

    private Plan planUpdated;

    public SetPlanDialog(Context context, int initialColor, CircularPlannerViewPresenter presenter,
                         long dateInMillis, float startAngle, float endAngle) {
        super(context);
        mInitialColor = initialColor;
        mPresenter = presenter;

        this.dateInMillis = dateInMillis;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
        this.context = context;

        // init 0 at selectedRepeatTerm
        this.selectedRepeatTerm = 0;

        this.planUpdated = null;
    }

    // construct for updating
    public SetPlanDialog(Context context, CircularPlannerViewPresenter presenter,
                          Plan planUpdated) {
        super(context);
        mPresenter = presenter;

        this.mInitialColor = planUpdated.color;
        this.mPlanColor = planUpdated.color;
        this.mPlanName = planUpdated.planName;
        this.dateInMillis = planUpdated.dateInMillis;
        this.startAngle = planUpdated.startAngle;
        this.endAngle = planUpdated.endAngle;
        this.context = context;

        // init 0 at selectedRepeatTerm
        this.selectedRepeatTerm = planUpdated.repeatTerm;

        this.planUpdated = planUpdated;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set plan color
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(int color) {
                mPlanColor = color;
            }
        };

        dialogLinearLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.set_plan_dialog, null);

        // add color picker view to dialog view
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lParams.gravity = Gravity.CENTER;

        /* init spinner */
        final Spinner repeatSpinner = (Spinner) dialogLinearLayout.findViewById(R.id.repeat_spinner);


        ArrayAdapter adapter = new ArrayAdapter(context, R.layout.spin, repeatTextArray);
        adapter.setDropDownViewResource(R.layout.spin_dropdown);

        repeatSpinner.setAdapter(adapter);

        repeatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedRepeatTerm = repeatSpinner.getSelectedItemPosition();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if(planUpdated != null) {
            EditText planNameEditText = ((EditText) dialogLinearLayout.findViewById(R.id.plan_name_text));
            planNameEditText.setText(planUpdated.planName);

            repeatSpinner.setSelection(planUpdated.repeatTerm);
        }

        ColorPickerView colorPickerView = new ColorPickerView(getContext(), l, mInitialColor);
        colorPickerView.setLayoutParams(lParams);
        dialogLinearLayout.addView(colorPickerView, 1);


        // button event listener
        Button buttonSet = (Button) dialogLinearLayout.findViewById(R.id.button_set);
        Button buttonCancel = (Button) dialogLinearLayout.findViewById(R.id.button_cancel);
        buttonSet.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    // set plan name at dialog view member
                    mPlanName = ((EditText) dialogLinearLayout.findViewById(R.id.plan_name_text)).getText().toString();

                    if(selectedRepeatTerm == RepeatTerm.NONE.ordinal()) {

                        // before add plan, erases original plans
                        if(planUpdated != null) {
                            mPresenter.deletePlan(mPresenter.getPlanSelected());
                            planUpdated = null;
                        }

                        // add only one plan
                        Plan planAdded = mPresenter.addPlan(mPlanName, dateInMillis, startAngle,
                                endAngle, mPlanColor, selectedRepeatTerm, "");

                        mPresenter.updateAfterAddPlanToList(planAdded); // invalidate planner inner

                        dismiss();

                    }
                    else {

                        // make unique string for repeatGroupId
                        final String repeatGroupId = UUID.randomUUID().toString();

                        Calendar curCalendar = mPresenter.getCurCalendar();
                        long curCalendarTimeMillis = curCalendar.getTimeInMillis();

                        copyCurCalendar = Calendar.getInstance();
                        copyCurCalendar.setTimeInMillis(curCalendarTimeMillis);
                        copyCurCalendar.add(Calendar.YEAR, 1);
                        final int nextYear = curCalendar.get(Calendar.YEAR); // limit of add plan is to next year
                        copyCurCalendar.add(Calendar.YEAR, -1); // reset year

                        /* ProgressDialog */
                        loadingDialog = ProgressDialog.show(context, null,
                                "목표 추가 중.....", true, false);

                        /* add plans iteratively belong to repeat term */

                        /*** DAY REPEAT ***/
                        if (selectedRepeatTerm == RepeatTerm.DAY.ordinal()) {

                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    boolean endFlag = false;
                                    Calendar copy2CurCalendar = Calendar.getInstance();
                                    copy2CurCalendar.setTimeInMillis(copyCurCalendar.getTimeInMillis());
                                    Plan planFirstAdded = null;

                                    while(copyCurCalendar.get(Calendar.YEAR) == nextYear) {

                                        // check loop for finding overlapping plan
                                        while(copy2CurCalendar.get(Calendar.YEAR) == nextYear) {

                                            ArrayList<Plan> plans;

                                            if(planUpdated == null)
                                                plans = mPresenter.getPlansWithDateMillis(copy2CurCalendar.getTimeInMillis());
                                            else
                                                plans = mPresenter.getResultWithRepeatGroup(planUpdated);

                                            for (Plan p : plans) {

                                                // first condition is duplication check is skipped for same group of updated plan
                                                // three conditions needed,
                                                // 1. startAngle is in plan already added
                                                // 2. endAngle is in plan already added
                                                // 3. startAngle is smaller than startAngle is in plan already added
                                                //    && endAngle is bigger than endAngle is in plan already added
                                                if ( ( planUpdated == null || !planUpdated.repeatGroupId.equals(p.repeatGroupId) ) &&

                                                        (
                                                            p.startAngle < endAngle && p.endAngle > endAngle ||
                                                            p.startAngle < startAngle && p.endAngle > startAngle ||
                                                            p.startAngle > startAngle && p.endAngle < endAngle
                                                        )

                                                    ) {

                                                    Message msg = handler.obtainMessage();
                                                    msg.arg1 = 1; // set and int data
                                                    handler.sendMessage(msg);

                                                    endFlag = true;
                                                    break;
                                                }
                                            }
                                            copy2CurCalendar.add(Calendar.DAY_OF_MONTH, 1);
                                        }

                                        if (endFlag) break; // end add plans loop

                                        // if update dialog is on, before add plan, erases original plans
                                        if(planUpdated != null) {
                                            ArrayList<Plan> plansDeleted = mPresenter.getResultWithRepeatGroup(mPresenter.getPlanSelected());


                                            for(Plan p : plansDeleted) { // loop for finding plan which has different angle compare with selected one

                                                if(p.dateInMillis == copyCurCalendar.getTimeInMillis()) {

                                                    if( p.startAngle == startAngle && p.endAngle == endAngle ) {

                                                        if (planFirstAdded == null) // set only at first
                                                            planFirstAdded = mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), startAngle,
                                                                    endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);
                                                        else
                                                            mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), startAngle,
                                                                    endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);

                                                    }
                                                    else {

                                                        if (planFirstAdded == null) { // set only at first
                                                            planFirstAdded = mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), p.startAngle,
                                                                    p.endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);
                                                        }
                                                        else {
                                                            mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), p.startAngle,
                                                                    p.endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);
                                                        }

                                                    }

                                                }
                                            }

                                        }  // end of planUpdated is not null
                                        else {

                                            if (planFirstAdded == null) // set only at first
                                                planFirstAdded = mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), startAngle,
                                                        endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);
                                            else
                                                mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), startAngle,
                                                        endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);

                                        } // end of else

                                        copyCurCalendar.add(Calendar.DAY_OF_MONTH, 1);
                                    }

                                    if(planUpdated != null) {
                                        mPresenter.deletePlan(mPresenter.getPlanSelected()); // erase origin group
                                        planUpdated = null;
                                    }

                                    Message msg = handler.obtainMessage();
                                    msg.obj = planFirstAdded; // set first added plan
                                    handler.sendMessage(msg);
                                }
                            });
                            thread.start();

                        /*** WEEK REPEAT ***/
                        } else if (selectedRepeatTerm == RepeatTerm.WEEK.ordinal()) {

                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    boolean endFlag = false;
                                    Calendar copy2CurCalendar = Calendar.getInstance();
                                    copy2CurCalendar.setTimeInMillis(copyCurCalendar.getTimeInMillis());
                                    Plan planFirstAdded = null;

                                    while(copyCurCalendar.get(Calendar.YEAR) == nextYear) {

                                        // check loop for finding overlapping plan
                                        while(copy2CurCalendar.get(Calendar.YEAR) == nextYear) {

                                            ArrayList<Plan> plans;

                                            if(planUpdated == null)
                                                plans = mPresenter.getPlansWithDateMillis(copy2CurCalendar.getTimeInMillis());
                                            else
                                                plans = mPresenter.getResultWithRepeatGroup(planUpdated);

                                            for (Plan p : plans) {

                                                // first condition is duplication check is skipped for same group of updated plan
                                                // three conditions needed,
                                                // 1. startAngle is in plan already added
                                                // 2. endAngle is in plan already added
                                                // 3. startAngle is smaller than startAngle is in plan already added
                                                //    && endAngle is bigger than endAngle is in plan already added
                                                if ( ( planUpdated == null || !planUpdated.repeatGroupId.equals(p.repeatGroupId) ) &&

                                                        (
                                                                p.startAngle < endAngle && p.endAngle > endAngle ||
                                                                        p.startAngle < startAngle && p.endAngle > startAngle ||
                                                                        p.startAngle > startAngle && p.endAngle < endAngle
                                                        )

                                                    ) {

                                                    Message msg = handler.obtainMessage();
                                                    msg.arg1 = 1; // set and int data
                                                    handler.sendMessage(msg);

                                                    endFlag = true;
                                                    break;
                                                }
                                            }
                                            copy2CurCalendar.add(Calendar.WEEK_OF_MONTH, 1);
                                        }

                                        if (endFlag) break; // end add plans loop


                                        // if update dialog is on, before add plan, erases original plans
                                        if(planUpdated != null) {
                                            ArrayList<Plan> plansDeleted = mPresenter.getResultWithRepeatGroup(mPresenter.getPlanSelected());

                                            mPresenter.deletePlan(mPresenter.getPlanSelected()); // erase group
                                            planUpdated = null;

                                            for(Plan p : plansDeleted) { // loop for finding plan which has different angle compare with selected one

                                                if(p.dateInMillis == copyCurCalendar.getTimeInMillis()) {

                                                    if( p.startAngle == startAngle && p.endAngle == endAngle ) {

                                                        if (planFirstAdded == null) // set only at first
                                                            planFirstAdded = mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), startAngle,
                                                                    endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);
                                                        else
                                                            mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), startAngle,
                                                                    endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);

                                                    }
                                                    else {

                                                        if (planFirstAdded == null) // set only at first
                                                            planFirstAdded = mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), p.startAngle,
                                                                    p.endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);
                                                        else
                                                            mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), p.startAngle,
                                                                    p.endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);

                                                    }

                                                }
                                            }

                                        }  // end of planUpdated is not null
                                        else {

                                            if (planFirstAdded == null) // set only at first
                                                planFirstAdded = mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), startAngle,
                                                        endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);
                                            else
                                                mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), startAngle,
                                                        endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);

                                        } // end of else

                                        copyCurCalendar.add(Calendar.WEEK_OF_MONTH, 1);
                                    }

                                    if(planUpdated != null) {
                                        mPresenter.deletePlan(mPresenter.getPlanSelected()); // erase origin group
                                        planUpdated = null;
                                    }

                                    Message msg = handler.obtainMessage();
                                    msg.obj = planFirstAdded; // set first added plan
                                    handler.sendMessage(msg);

                                }
                            });
                            thread.start();

                        /*** MONTH REPEAT ***/
                        } else if (selectedRepeatTerm == RepeatTerm.MONTH.ordinal()) {

                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    boolean endFlag = false;
                                    Calendar copy2CurCalendar = Calendar.getInstance();
                                    copy2CurCalendar.setTimeInMillis(copyCurCalendar.getTimeInMillis());
                                    int dayOfMonth = copy2CurCalendar.get(Calendar.DAY_OF_MONTH);
                                    Plan planFirstAdded = null;

                                    while(copyCurCalendar.get(Calendar.YEAR) == nextYear) {

                                        // check loop for finding overlapping plan
                                        while(copy2CurCalendar.get(Calendar.YEAR) == nextYear) {

                                            ArrayList<Plan> plans;

                                            if(planUpdated == null)
                                                plans = mPresenter.getPlansWithDateMillis(copy2CurCalendar.getTimeInMillis());
                                            else
                                                plans = mPresenter.getResultWithRepeatGroup(planUpdated);

                                            for (Plan p : plans) {

                                                // first condition is duplication check is skipped for same group of updated plan
                                                // three conditions needed,
                                                // 1. startAngle is in plan already added
                                                // 2. endAngle is in plan already added
                                                // 3. startAngle is smaller than startAngle is in plan already added
                                                //    && endAngle is bigger than endAngle is in plan already added
                                                if ( ( planUpdated == null || !planUpdated.repeatGroupId.equals(p.repeatGroupId) ) &&

                                                        (
                                                                p.startAngle < endAngle && p.endAngle > endAngle ||
                                                                        p.startAngle < startAngle && p.endAngle > startAngle ||
                                                                        p.startAngle > startAngle && p.endAngle < endAngle
                                                        )

                                                    ) {

                                                    Message msg = handler.obtainMessage();
                                                    msg.arg1 = 1; // set and int data
                                                    handler.sendMessage(msg);

                                                    endFlag = true;
                                                    break;
                                                }
                                            }
                                            copy2CurCalendar.add(Calendar.MONTH, 1);
                                            if(dayOfMonth > copy2CurCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                                                copy2CurCalendar.set(Calendar.DAY_OF_MONTH, copy2CurCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                                            else
                                                copy2CurCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                        }

                                        if (endFlag) break; // end add plans loop


                                        // if update dialog is on, before add plan, erases original plans
                                        if(planUpdated != null) {
                                            ArrayList<Plan> plansDeleted = mPresenter.getResultWithRepeatGroup(mPresenter.getPlanSelected());

                                            mPresenter.deletePlan(mPresenter.getPlanSelected()); // erase group
                                            planUpdated = null;

                                            for(Plan p : plansDeleted) { // loop for finding plan which has different angle compare with selected one

                                                if(p.dateInMillis == copyCurCalendar.getTimeInMillis()) {

                                                    if( p.startAngle == startAngle && p.endAngle == endAngle ) {

                                                        if (planFirstAdded == null) // set only at first
                                                            planFirstAdded = mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), startAngle,
                                                                    endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);
                                                        else
                                                            mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), startAngle,
                                                                    endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);

                                                    }
                                                    else {

                                                        if (planFirstAdded == null) // set only at first
                                                            planFirstAdded = mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), p.startAngle,
                                                                    p.endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);
                                                        else
                                                            mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), p.startAngle,
                                                                    p.endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);

                                                    }

                                                }
                                            }

                                        }  // end of planUpdated is not null
                                        else {

                                            if (planFirstAdded == null) // set only at first
                                                planFirstAdded = mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), startAngle,
                                                        endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);
                                            else
                                                mPresenter.addPlan(mPlanName, copyCurCalendar.getTimeInMillis(), startAngle,
                                                        endAngle, mPlanColor, selectedRepeatTerm, repeatGroupId);

                                        } // end of else


                                        copyCurCalendar.add(Calendar.MONTH, 1);
                                    }

                                    if(planUpdated != null) {
                                        mPresenter.deletePlan(mPresenter.getPlanSelected()); // erase origin group
                                        planUpdated = null;
                                    }

                                    Message msg = handler.obtainMessage();
                                    msg.obj = planFirstAdded; // set first added plan
                                    handler.sendMessage(msg);
                                }
                            });
                            thread.start();

                        }

                    } // end of else

                } // end of if condition which action is MotionEvent.ACTION_UP
                return true;
            }
        });
        buttonCancel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dismiss();
                return true;
            }
        });


        setContentView(dialogLinearLayout);
        setTitle("목표 설정하기");

    } // end of onCreate method

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch(msg.arg1) {
                case 1:
                    loadingDialog.dismiss(); // 다이얼로그 삭제
                    dismiss();
                    Snackbar snackbar = Snackbar.make(((Activity) context).findViewById(R.id.planner), "목표 시간 중복입니다! 다시 확인해주세요.", Snackbar.LENGTH_LONG);
                    View mView = snackbar.getView();
                    TextView textView = (TextView) mView.findViewById(android.support.design.R.id.snackbar_text);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    else
                        textView.setGravity(Gravity.CENTER_HORIZONTAL);
                    snackbar.show();
                    break;
                default:
                    mPresenter.updateAfterAddPlanToList((Plan) msg.obj);
                    loadingDialog.dismiss(); // 다이얼로그 삭제
                    dismiss();
                    break;
            }

        }
    };

    // getters and setters

}