package com.example.jongmin.circularplanner.view;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import java.util.Calendar;

import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jongmin.circularplanner.CircularPlannerActivity;
import com.example.jongmin.circularplanner.helper.DBHelper;
import com.example.jongmin.circularplanner.PlannerFragment;
import com.example.jongmin.circularplanner.R;
import com.example.jongmin.circularplanner.helper.Plan;
import com.example.jongmin.circularplanner.presenter.CircularPlannerInnerPresenter;

/**
 * Created by jongmin on 2016-12-01.
 */

public class CircularPlannerInnerView extends View {

    private final float radius;         // CPlaner radius

    private Context context;
    private Canvas canvas;
    private Paint paint;
    private Path drawPath;
    private RectF innerCircle;

    private Plan planSelected;

    private long timeInMillis;
    private CircularPlannerInnerPresenter presenter;

    private LinearLayout dateTextContainer;
    private TextView planNameTextView;
    private TextView planTimeTextView;

    PlannerFragment plannerFragment;

    private boolean borderDraw = false;

    private CharSequence dayText;


    public CircularPlannerInnerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularPlanner, 0, 0);

        try {
            radius = a.getDimension(R.styleable.CircularPlanner_radius, 0);
        } finally {
            a.recycle();
        }

        this.context = context;
        this.presenter = new CircularPlannerInnerPresenter();

        // set presenter
        this.presenter.setViewAndDBHelper(this, ((CircularPlannerActivity) context).getDBHelper());

        this.dateTextContainer = null;
        this.planNameTextView = null;
        this.planTimeTextView = null;

        paint = new Paint();
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);

        drawPath = new Path();
        innerCircle = new RectF();

        float adjust = .05f * radius;
        innerCircle.set(adjust, adjust, radius*2-adjust, radius*2-adjust);


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
        this.canvas = canvas;

        // set circle background fill
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(getContext(),(R.color.circle_inner)));
        canvas.drawCircle(innerCircle.centerX(), innerCircle.centerY(), .95f * radius, paint);

        if(borderDraw) {
            // draw border of innerCircle
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(radius / 40.0f);
            paint.setColor(ContextCompat.getColor(getContext(), (R.color.user_donut_dark)));
            drawPath.reset();
            drawPath.arcTo(innerCircle, 0, 359.99f, false);
            drawPath.close();
            canvas.drawPath(drawPath, paint);
        }
    }

    // event listeners

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // get cur Activity for get cur fragment
        CircularPlannerActivity activity = (CircularPlannerActivity) context;
        plannerFragment = (PlannerFragment) activity.getRegisteredFragment();
        Calendar curCalendar = plannerFragment.getCurFragCalendar();
        CircularPlannerView curPlannerView = plannerFragment.getPlannerView();

        // set cur date
        timeInMillis = curCalendar.getTimeInMillis();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :

                if(curPlannerView.getEndAngle() != curPlannerView.getStartAngle()) {
                    // add dialog show
                    SetPlanDialog dialog = new SetPlanDialog(context, Color.WHITE, curPlannerView.getPresenter(),
                            curCalendar.getTimeInMillis(),
                            curPlannerView.getStartAngle(),
                            curPlannerView.getEndAngle());
                    dialog.show();
                } else if (planSelected != null) {
                    // update dialog show
                    // set plan selected with first plan in repeat group
                    SetPlanDialog dialog = new SetPlanDialog(context, curPlannerView.getPresenter(), planSelected);
//                            presenter.getPlanFirstInGroup());
                    dialog.show();
                }

                // Toast.makeText(context, plannerFragment.getCurFragCalendar().getTime()+"", Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    // getters and setters

    public void setPlanSelected(Plan planSelected) {
        this.planSelected = planSelected;
        this.dateTextContainer.setVisibility(INVISIBLE);
        this.planNameTextView.setVisibility(VISIBLE);
        this.planTimeTextView.setVisibility(VISIBLE);
        this.planNameTextView.setText(planSelected.planName);


        // process to add 0 at front when time string smaller than 10

        String startHourStr, startMinuteStr, endHourStr, endMinuteStr;

        /* represent of minute by unit 5 */
        int curStartMinute = Plan.getMinuteUnitFive(planSelected.startAngle);
        int curEndMinute = Plan.getMinuteUnitFive(planSelected.endAngle);

        if(((int) Math.floor(planSelected.startAngle/15)) < 10)
            startHourStr = "0" + ((int) Math.floor(planSelected.startAngle / 15));
        else
            startHourStr = ""+((int) Math.floor(planSelected.startAngle / 15));


        if(curStartMinute < 10)
            startMinuteStr = "0" + curStartMinute;
        else
            startMinuteStr = "" + curStartMinute;

        if(((int) Math.floor(planSelected.endAngle/15)) < 10)
            endHourStr = "0" + ((int) Math.floor(planSelected.endAngle / 15));
        else
            endHourStr = "" + ((int) Math.floor(planSelected.endAngle / 15));


        if(curEndMinute < 10)
            endMinuteStr = "0" + curEndMinute;
        else
            endMinuteStr = "" + curEndMinute;

        planTimeTextView.setText(startHourStr + ":" + startMinuteStr + " ~ " + endHourStr +":"+ endMinuteStr);
    }

    public void setPlanUnselected() {
        this.planSelected = null;
        this.dateTextContainer.setVisibility(VISIBLE);
        this.planNameTextView.setVisibility(INVISIBLE);
        this.planTimeTextView.setVisibility(INVISIBLE);
    }

    public boolean isBorderDraw() {
        return borderDraw;
    }

    public void setBorderDrawWithReplaceText(boolean borderDraw) {

        if (this.borderDraw != borderDraw) {
            this.borderDraw = borderDraw;

            if (borderDraw) {
                // Replace date texts to plus mark
                TextView monthAndDay = (TextView) dateTextContainer.findViewById(R.id.month_day);

                // only once init
                if (dayText == null)
                    dayText = monthAndDay.getText();

                monthAndDay.setText("목표 설정");
                TextView year = (TextView) dateTextContainer.findViewById(R.id.year);
                year.setVisibility(GONE);
                TextView dayInMonth = (TextView) dateTextContainer.findViewById(R.id.day_text);
                dayInMonth.setVisibility(GONE);
            } else {
                // Replace plus mark to date texts
                TextView monthAndDay = (TextView) dateTextContainer.findViewById(R.id.month_day);
                monthAndDay.setText(dayText);
                TextView year = (TextView) dateTextContainer.findViewById(R.id.year);
                year.setVisibility(VISIBLE);
                TextView dayInMonth = (TextView) dateTextContainer.findViewById(R.id.day_text);
                dayInMonth.setVisibility(VISIBLE);
            }

            invalidate(); // redraw with new value of borderDraw

        }
    }

    public float getRadius() {
        return radius;
    }

    public CircularPlannerInnerPresenter getPresenter() {
        return presenter;
    }

    public Plan getPlanSelected() {
        return planSelected;
    }

    public void setPlanNameTextView(TextView planNameTextView) {
        this.planNameTextView = planNameTextView;
    }

    public void setPlanTimeTextView(TextView planTimeTextView) {
        this.planTimeTextView = planTimeTextView;
    }

    public void setPlanTimeText(String planTimeText) {
        this.planTimeTextView.setText(planTimeText);
    }

    public void setDateTextContainer(LinearLayout dateTextContainer) {
        this.dateTextContainer = dateTextContainer;
    }

}
