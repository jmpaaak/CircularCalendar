package com.example.jongmin.circularplanner.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.jongmin.circularplanner.R;
import com.example.jongmin.circularplanner.presenter.CircularPlannerInnerPresenter;
import com.example.jongmin.circularplanner.presenter.CircularPlannerViewPresenter;

import org.w3c.dom.Text;

/**
 * Created by jongmin on 2016-12-03.
 */

public class SetAchieveDialog extends Dialog {

    CircularPlannerInnerPresenter mPresenter;
    CircularPlannerView mCircularPlannerView;

    private int percentageOfAchieve;
    TextView percentageTextView;
    SeekBar seekBar;


    public SetAchieveDialog(Context context, CircularPlannerView circularPlannerView, CircularPlannerInnerPresenter presenter) {
        super(context);

        mCircularPlannerView = circularPlannerView;
        mPresenter = presenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.achieve_plan_dialog, null);

        // button event listeners
        Button buttonSet = (Button) linearLayout.findViewById(R.id.button_set);
        Button buttonCancel = (Button) linearLayout.findViewById(R.id.button_cancel);
        buttonSet.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    // add plan
                    mPresenter.addAchievementAtSelectedPlan(percentageOfAchieve);
                    mCircularPlannerView.updateAfterUpdatePlanAtList();

                    dismiss();

                }
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

        percentageTextView = (TextView) linearLayout.findViewById(R.id.percentage_text);
        percentageTextView.setText(mPresenter.getView().getPlanSelected().percentageOfAchieve+"%");

        // set plan color
        SeekBar.OnSeekBarChangeListener l = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                percentageOfAchieve = i;
                TextView textView = (TextView) linearLayout.findViewById(R.id.percentage_text);
                textView.setText(i+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
        // seekBar event listeners
        seekBar = (SeekBar) linearLayout.findViewById(R.id.achieve_seekBar);
        seekBar.setOnSeekBarChangeListener(l);
        seekBar.setProgress(mPresenter.getView().getPlanSelected().percentageOfAchieve);

        setContentView(linearLayout);
    }


    // getters and setters

}