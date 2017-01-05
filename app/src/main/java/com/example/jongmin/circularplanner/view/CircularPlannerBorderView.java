package com.example.jongmin.circularplanner.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.example.jongmin.circularplanner.R;


/**
 * Created by jongmin on 2016-12-01.
 */

public class CircularPlannerBorderView extends View {

    private final float radius;         // CPlaner radius
    private final float subLineLength;

    private Context context;
    private Canvas canvas;
    private Paint paint;
    private Path drawPath;
    private Path userPath;
    private RectF borderCircle;
    private RectF planCircle;

    public CircularPlannerBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularPlanner, 0, 0);

        try {
            radius = a.getDimension(R.styleable.CircularPlanner_radius, 0);
            subLineLength = a.getDimension(R.styleable.CircularPlanner_sub_line_length, 0);
        } finally {
            a.recycle();
        }

        this.context = context;

        paint = new Paint();
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(radius / 28.0f);

        drawPath = new Path();
        userPath = new Path();
        borderCircle = new RectF();

        float adjust = .05f * radius;
        borderCircle.set(adjust, adjust, radius*2-adjust, radius*2-adjust);
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

        paint.setColor(ContextCompat.getColor(getContext(),(R.color.tab_background_selected)));
        paint.setStrokeWidth(radius / 28.0f);

        // draw borderCircle
        drawPath.reset();
        drawPath.arcTo(borderCircle, 0, 359.99f, false);
        drawPath.close();
        canvas.drawPath(drawPath, paint);

        // draw sub lines and texts
        for(int i=0; i<360; i += 15) {
            canvas.save();
            if(i % 45 == 0) {
                float adjustVal = (0.85f*radius);
                float cx = borderCircle.centerX();
                float cy = borderCircle.centerY();

                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(subLineLength/10);

                // dp to px
                Resources r = getResources();
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, r.getDisplayMetrics());
                paint.setTextSize(px);

                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(ContextCompat.getColor(getContext(),(R.color.tab_background_selected_low_opacity)));

                canvas.rotate(i, cx, cy);
                canvas.drawText(""+i/15, cx, cy-adjustVal+px/2, paint);
            }
            else {
                float adjustVal = (0.85f*radius);
                float cx = borderCircle.centerX();
                float cy = borderCircle.centerY();

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(radius / 100.0f);
                paint.setColor(ContextCompat.getColor(getContext(),(R.color.tab_background_selected_low_opacity)));

                canvas.rotate(i, cx, cy);
                canvas.drawLine(cx, cy-adjustVal, cx, cy-adjustVal+subLineLength, paint);
            }
            canvas.restore();
        } // end of draw sub lines and texts
    }
}
