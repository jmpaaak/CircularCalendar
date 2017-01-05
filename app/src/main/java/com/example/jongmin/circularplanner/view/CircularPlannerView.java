package com.example.jongmin.circularplanner.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import java.util.Calendar;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jongmin.circularplanner.CircularPlannerActivity;
import com.example.jongmin.circularplanner.R;
import com.example.jongmin.circularplanner.helper.DBHelper;
import com.example.jongmin.circularplanner.helper.Plan;
import com.example.jongmin.circularplanner.presenter.CircularPlannerViewPresenter;

import java.util.ArrayList;

/**
 * Created by jongmin on 2016-12-01.
 */

public class CircularPlannerView extends View {


    public static float radius;         // CPlaner radius

    private Context context;

    private Paint paint;
    private Paint paintAchieve;

    private float startAngle;
    private float endAngle;

    private Calendar curCalendar;

    private CircularPlannerInnerView plannerInner;

    private CircularPlannerViewPresenter presenter;

    private ArrayList<Plan> planList;

    private boolean updateModeFlag;

    public Plan updatePlan;

    // set only view of today
    private boolean todayFlag;
    private double angleOfClock;
    private float curHour;
    private float curMinute;
    private Paint clockPaint;
    private Path clockDrawPath;

    private RectF plannerCircle;

    // timeout fans
    private RectF timeoutPlannerCircle;

    // achievement fans
    private RectF achievementPlannerCircle;

    ProgressDialog loadingDialog;


    public CircularPlannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularPlanner, 0, 0);

        try {
            radius = a.getDimension(R.styleable.CircularPlanner_radius, 0);
        } finally {
            a.recycle();
        }

        this.context = context;

        this.presenter = new CircularPlannerViewPresenter();

        // set presenter
        this.presenter.setViewAndDBHelper(this, ((CircularPlannerActivity) context).getDBHelper());

        this.startAngle = 0;
        this.endAngle = 0;

        paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(getContext(),(R.color.user_donut)));

        plannerCircle = new RectF();
        timeoutPlannerCircle = new RectF();
        achievementPlannerCircle = new RectF();


        float adjust = .18f * radius;
        plannerCircle.set(adjust, adjust, radius*2-adjust, radius*2-adjust);

        adjust = .05f * radius;
        timeoutPlannerCircle.set(adjust, adjust, radius*2-adjust, radius*2-adjust);

        adjust = .30f * radius;
        achievementPlannerCircle.set(adjust, adjust, radius*2-adjust, radius*2-adjust);

        updateModeFlag = false;
        this.updatePlan = null;

        todayFlag = false;

        // for hour hand of clock
        clockPaint = new Paint();
        clockPaint.setDither(true);
        clockPaint.setStyle(Paint.Style.STROKE);
        clockPaint.setStrokeJoin(Paint.Join.ROUND);
        clockPaint.setStrokeCap(Paint.Cap.ROUND);
        clockPaint.setAntiAlias(true);
        clockPaint.setStrokeWidth(radius / 15.0f);
        clockPaint.setColor(ContextCompat.getColor(context, R.color.user_donut_dark));
        clockDrawPath = new Path();

        paintAchieve = new Paint();
        paintAchieve.setDither(true);
        paintAchieve.setAntiAlias(true);
        paintAchieve.setStyle(Paint.Style.FILL);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = (int) radius*2;
        int desiredHeight = (int) radius*2;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);   // view size of parent
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec); // view size of parent

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) { // fill_parent or match_parent
            width = widthSize;
        }else if (widthMode == MeasureSpec.AT_MOST) { // wrap_content
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        ViewGroup viewGroup = (ViewGroup) getParent();
        plannerInner = (CircularPlannerInnerView) viewGroup.findViewById(R.id.inner_planner);

        if(startAngle != endAngle) {
            paint.setColor(ContextCompat.getColor(getContext(), (R.color.user_donut)));
            canvas.drawArc(plannerCircle, startAngle - 90, endAngle - startAngle, true, paint);
        } else {
            plannerInner.setBorderDrawWithReplaceText(false);
        }

        /* draw plans */
        if(planList == null)
            planList = plannerInner.getPresenter().getPlansWithDateMillis(curCalendar.getTimeInMillis());

        // draw each plan
        for(Plan p : planList) {
            int r = Color.red(p.color);
            int g = Color.green(p.color);
            int b = Color.blue(p.color);
            paint.setColor(Color.argb((int)(255*0.1),r, g, b)); // 30% alpha in plan
            canvas.drawArc(plannerCircle, p.startAngle - 90, p.endAngle - p.startAngle, true, paint);
        }



        // draw two pins
        if(updateModeFlag) {
            paint.setColor(ContextCompat.getColor(context, R.color.circle_pin));

            // start Pin
            canvas.save();
            canvas.rotate(updatePlan.startAngle, plannerCircle.centerX(), plannerCircle.centerY());
            canvas.drawCircle(plannerCircle.centerX(), plannerCircle.centerY()-plannerCircle.width()/2, radius/18, paint);
            canvas.restore();

            // end Pin
            canvas.save();
            canvas.rotate(updatePlan.endAngle, plannerCircle.centerX(), plannerCircle.centerY());
            canvas.drawCircle(plannerCircle.centerX(), plannerCircle.centerY()-plannerCircle.width()/2, radius/18, paint);
            canvas.restore();
        }

        // for clock in today flag
        if( curCalendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                curCalendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                curCalendar.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                && !todayFlag) {
            mHandler.sendEmptyMessage(0);
            todayFlag = true;
        }

        // draw hour hand of clock
        if(todayFlag) {
            clockDrawPath.reset();
            clockDrawPath.moveTo(plannerCircle.centerX(), plannerCircle.centerY());
            clockDrawPath.lineTo(
                    (float) (plannerCircle.centerX() + radius*0.75 * (Math.sin((angleOfClock) * Math.PI/180))),
                    (float) (plannerCircle.centerY() + radius*0.75 * (-Math.cos((angleOfClock) * Math.PI/180))) );
            clockDrawPath.close();
            canvas.drawPath(clockDrawPath, clockPaint);
        }

        /* draw timeout plans and achievement of fan shape */
        if(curCalendar.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
            for (Plan p : planList) {
                int r = Color.red(p.color);
                int g = Color.green(p.color);
                int b = Color.blue(p.color);
                paint.setColor(Color.argb((int)(255*0.3), r, g, b)); // paint fan of timeout
                paintAchieve.setColor(Color.rgb(255, 255, 255)); // paint fan of achievement
                if(!todayFlag) {
                    canvas.drawArc(timeoutPlannerCircle, p.startAngle - 90, p.endAngle - p.startAngle, true, paint);

                    if(p.percentageOfAchieve != 0.0)
                        canvas.drawArc(achievementPlannerCircle, p.startAngle - 90,  ((p.percentageOfAchieve) * (p.endAngle - p.startAngle))/100, true, paint);

                }
                else {
                    if(p.startAngle < angleOfClock && p.endAngle > angleOfClock) {
                        canvas.drawArc(timeoutPlannerCircle, p.startAngle - 90, (float) (angleOfClock - p.startAngle), true, paint);

                        if(p.percentageOfAchieve != 0.0)
                            canvas.drawArc(achievementPlannerCircle, p.startAngle - 90,  (float) (((p.percentageOfAchieve) * (angleOfClock - p.startAngle))/100), true, paint);
                    } else if(angleOfClock >= p.endAngle) {
                        canvas.drawArc(timeoutPlannerCircle, p.startAngle - 90, p.endAngle - p.startAngle, true, paint);

                        if(p.percentageOfAchieve != 0.0)
                            canvas.drawArc(achievementPlannerCircle, p.startAngle - 90,  ((p.percentageOfAchieve) * (p.endAngle - p.startAngle))/100, true, paint);
                    }

//                    Log.i("check sweeep angle: ", (p.percentageOfAchieve/100) * (p.endAngle - p.startAngle)+" ");
//                    Log.i("check s angle: ", p.startAngle+" ");
//                    Log.i("check e angle: ", p.endAngle+" ");

                }
            }
        }
    }


    // event listeners
    private boolean enterFlag = true; // enter draggable flag
    private boolean longClickActive = false; // for longClick
    private static final int MIN_CLICK_DURATION = 500; // for longClick
    private long startClickTime; // for longClick

    // this function update plan list and re-draw
    public void updateAfterAddPlanToList(Plan planUpdated) {

        this.planList = presenter.getPlansWithDateMillis(curCalendar.getTimeInMillis());

        if(planList.size() != 0 && planUpdated != null)
            this.plannerInner.setPlanSelected(planUpdated);

        startAngle = endAngle = 0;

        invalidate();
    }

    // this function update plan list and re-draw
    public void updateAfterUpdatePlanAtList() {
        this.planList = presenter.getPlansWithDateMillis(curCalendar.getTimeInMillis());

        if(this.updateModeFlag)
            updateModeFlag = false;

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                if(!updateModeFlag) {

                    startAngle = (float) (360 - (Math.PI + Math.atan2(x - plannerCircle.centerX(), y - plannerCircle.centerY())) * 180 / Math.PI);
                    endAngle = startAngle;

                    if (enterFlag == false) {
                        enterFlag = true;
                        invalidate();
                    }

                    // find if plan touches
                    for (Plan p : planList) {
                        if (p.startAngle <= startAngle && startAngle <= p.endAngle) {
                            plannerInner.setPlanSelected(p);

                            // for longClick
                            if (longClickActive == false) {
                                longClickActive = true;
                                startClickTime = Calendar.getInstance().getTimeInMillis();
                            }

                            break;
                        } else {
                            plannerInner.setPlanUnselected();
                            enterFlag = true;

                        }
                    }

                } // end of if

                // only update plan is touchable
                else {

                    float temp = (float) (360 - (Math.PI + Math.atan2(x - plannerCircle.centerX(), y - plannerCircle.centerY())) * 180 / Math.PI);

                    // update complete condition 1. re-touching
                    if (plannerInner.getPlanSelected().startAngle+5 <= temp
                            && temp <= plannerInner.getPlanSelected().endAngle-5) {


                        /* ProgressDialog */
                        loadingDialog = ProgressDialog.show(context, null,
                                "설정시간 수정 중.....", true, false);

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                // db plan updated
                                boolean updateComplete = presenter.updatePlan(updatePlan.id, updatePlan);

                                if(!updateComplete) {
                                    Message msg = handler.obtainMessage();
                                    msg.arg1 = 1; // set and int data
                                    handler.sendMessage(msg);
                                } else
                                    handler.sendEmptyMessage(0);

                            }
                        });
                        thread.start();


                        updateModeFlag = false;
                        invalidate();
                        break;
                    }

                }

                break;
            case MotionEvent.ACTION_MOVE:
                if(enterFlag && !updateModeFlag) {
                    float temp = (float) (360 - (Math.PI + Math.atan2(x - plannerCircle.centerX(), y - plannerCircle.centerY())) * 180 / Math.PI);

                    // find if next touch move will be plan
                    for (Plan p : planList) {
                        if (p.startAngle <= temp && temp <= p.endAngle) {
                            endAngle = startAngle = 0;
                            enterFlag = false;
                            plannerInner.setPlanSelected(p);

                            invalidate();

                            return false; // next to value of after touch
                        }
                        enterFlag = true;
                    }

                    // prevent counterclockwise
                    if (temp - startAngle > 1 && plannerInner.getPlanSelected() == null) {

                        endAngle = temp;
                        plannerInner.setBorderDrawWithReplaceText(true); // draw border of inner

                        // redraw
                        invalidate();

                    } else if(temp - startAngle < -1) {
                        startAngle = endAngle = 0;
                        enterFlag = false;
                        invalidate();
                    }
                }

                // only pins are movable
                else if (updateModeFlag) {

                    updatePlan = plannerInner.getPlanSelected();

                    float temp = (float) (360 - (Math.PI + Math.atan2(x - plannerCircle.centerX(), y - plannerCircle.centerY())) * 180 / Math.PI);

                    // check before temp is right-able for update
                    for (Plan p : planList) {
                        if ( p != plannerInner.getPlanSelected() &&
                                p.startAngle <= temp && temp <= p.endAngle)
                            return false; // next to value of after touch
                    }

                    // Threshold of angle is degree 10 - start pin is selected
                    if (plannerInner.getPlanSelected().startAngle -5 <= temp
                            && temp <= plannerInner.getPlanSelected().startAngle + 5) {
                        updatePlan.startAngle = temp;


                        // process to add 0 at front when time string smaller than 10

                        /* represent of minute by unit 5 */
                        int curStartMinute = Plan.getMinuteUnitFive(updatePlan.startAngle);
                        int curEndMinute = Plan.getMinuteUnitFive(updatePlan.endAngle);

                        String startHourStr, startMinuteStr, endHourStr, endMinuteStr;
                        if(((int) Math.floor(updatePlan.startAngle/15)) < 10)
                            startHourStr = "0" + ((int) Math.floor(updatePlan.startAngle / 15));
                        else
                            startHourStr = ""+((int) Math.floor(updatePlan.startAngle / 15));


                        if(curStartMinute < 10)
                            startMinuteStr = "0" + curStartMinute;
                        else
                            startMinuteStr = "" + curStartMinute;

                        if(((int) Math.floor(updatePlan.endAngle/15)) < 10)
                            endHourStr = "0" + ((int) Math.floor(updatePlan.endAngle / 15));
                        else
                            endHourStr = "" + ((int) Math.floor(updatePlan.endAngle / 15));


                        if(curEndMinute < 10)
                            endMinuteStr = "0" + curEndMinute;
                        else
                            endMinuteStr = "" + curEndMinute;


                        plannerInner.setPlanTimeText(startHourStr + ":" + startMinuteStr + " ~ " + endHourStr +":"+ endMinuteStr);
                        plannerInner.invalidate(); // redraw inner
                    }

                    // Threshold of angle is degree 10 - end pin is selected
                    if (plannerInner.getPlanSelected().endAngle -5 <= temp
                            && temp <= plannerInner.getPlanSelected().endAngle + 5) {
                        updatePlan.endAngle = temp;


                        // process to add 0 at front when time string smaller than 10

                        /* represent of minute by unit 5 */
                        int curStartMinute = Plan.getMinuteUnitFive(updatePlan.startAngle);
                        int curEndMinute = Plan.getMinuteUnitFive(updatePlan.endAngle);

                        String startHourStr, startMinuteStr, endHourStr, endMinuteStr;
                        if(((int) Math.floor(updatePlan.startAngle/15)) < 10)
                            startHourStr = "0" + ((int) Math.floor(updatePlan.startAngle / 15));
                        else
                            startHourStr = ""+((int) Math.floor(updatePlan.startAngle / 15));


                        if(curStartMinute < 10)
                            startMinuteStr = "0" + curStartMinute;
                        else
                            startMinuteStr = "" + curStartMinute;

                        if(((int) Math.floor(updatePlan.endAngle/15)) < 10)
                            endHourStr = "0" + ((int) Math.floor(updatePlan.endAngle / 15));
                        else
                            endHourStr = "" + ((int) Math.floor(updatePlan.endAngle / 15));


                        if(curEndMinute < 10)
                            endMinuteStr = "0" + curEndMinute;
                        else
                            endMinuteStr = "" + curEndMinute;


                        plannerInner.setPlanTimeText(startHourStr + ":" + startMinuteStr + " ~ " + endHourStr +":"+ endMinuteStr);
                        plannerInner.invalidate(); // redraw inner
                    }

                    // update complete condition 2. deleting
                    if(updatePlan.endAngle - updatePlan.startAngle == 0) {
                        updatePlan = null;

                        // plan db deleted
                        presenter.deletePlan(plannerInner.getPlanSelected());

                        // planList updating
                        planList = presenter.getPlansWithDateMillis(curCalendar.getTimeInMillis());

                        // unselecting deleted plan
                        plannerInner.setPlanUnselected();

                        updateModeFlag = false;
                    }

                    invalidate();
                }

                // for longClick if long event occurs
                if (longClickActive == true) {
                    long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                    if (clickDuration >= MIN_CLICK_DURATION) {

                        // erase other
                        startAngle = endAngle = 0;
                        enterFlag = false;

                        updatePlan = plannerInner.getPlanSelected();
                        updateModeFlag = true; // set update flag

                        invalidate();

                        Snackbar snackbar = Snackbar.make(this, "핀을 통해 줄이고 늘리고 다시 터치하며 완성하세요!", Snackbar.LENGTH_LONG);
                        View mView = snackbar.getView();
                        TextView textView = (TextView) mView.findViewById(android.support.design.R.id.snackbar_text);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        else
                            textView.setGravity(Gravity.CENTER_HORIZONTAL);
                        snackbar.show();

                        longClickActive = false; // inactive until update complete
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                // condition for inflating set achieve dialog
                if(curCalendar.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()
                        && plannerInner.getPlanSelected() != null
                        && longClickActive) { // not doing with long click callback
                    Dialog setAchieveDialog = new SetAchieveDialog(context, this, plannerInner.getPresenter());
                    if(todayFlag) {

                        if(plannerInner.getPlanSelected().startAngle < angleOfClock
                                && plannerInner.getPlanSelected().endAngle > angleOfClock) {
                            setAchieveDialog.show();
                        }
                        else if(angleOfClock >= plannerInner.getPlanSelected().endAngle) {
                            setAchieveDialog.show();
                        }

                    } else {
                        setAchieveDialog.show();
                    }
                }

                // for longClick
                longClickActive = false;

                break;
        }
        return true;
    }



    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            invalidateWithCurTime(); // re-draw with cur time
            mHandler.sendEmptyMessageDelayed(0 , 60*1000); // messaging after 1 min

        }
    };


    /* this is core function of displaying achievement */
    public void invalidateWithCurTime() {

        this.curHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        this.curMinute = Calendar.getInstance().get(Calendar.MINUTE);

        this.angleOfClock = curHour * 15 + curMinute * 0.25;

        invalidate();

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch(msg.arg1) {
                case 1:
                    loadingDialog.dismiss(); // 다이얼로그 삭제
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
                    presenter.updateAfterAddPlanToList(null);
                    loadingDialog.dismiss(); // 다이얼로그 삭제
                    break;
            }

        }
    };


    // getters and setters

    public float getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
    }

    public float getEndAngle() {
        return endAngle;
    }

    public void setEndAngle(float endAngle) {
        this.endAngle = endAngle;
    }

    public CircularPlannerInnerView getPlannerInner() {
        return plannerInner;
    }

    public void setPlannerInner(CircularPlannerInnerView plannerInner) {
        this.plannerInner = plannerInner;
    }

    public Calendar getCurCalednar() {
        return curCalendar;
    }

    public void setCurCalendar(Calendar curCalenar) {
        this.curCalendar = curCalenar;
    }

    public CircularPlannerViewPresenter getPresenter() {
        return this.presenter;
    }

    public ArrayList<Plan> getPlanList() {
        return planList;
    }

}

