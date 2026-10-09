package com.blank.template;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private boolean isRecording = false;
    private ImageButton btnRecord;
    private View recordHalo;
    private ValueAnimator haloAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View wave = findViewById(R.id.onda1);
        View wave2 = findViewById(R.id.onda2);
        View wave3 = findViewById(R.id.onda3);

        animWave(wave, 0);
        animWave(wave2, 2000);
        animWave(wave3, 4000);

        btnRecord = findViewById(R.id.recordButton);
        recordHalo = findViewById(R.id.recordHalo);
        setupRecordAnimation();
    }

    private void animWave(View view, long delay) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 5f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 5f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0.8f, 0.1f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha);

        animator.setDuration(4000);

        animator.setRepeatCount(ValueAnimator.INFINITE);

        animator.setRepeatMode(ValueAnimator.REVERSE);

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setStartDelay(delay);

        animator.start();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupRecordAnimation() {
        btnRecord.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    view.animate().cancel();

                    view.animate()
                            .scaleX(0.93f)
                            .scaleY(0.93f)
                            .setDuration(80)
                            .start();

                    startHaloAnimation();
                    startRecording();
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopRecording();
                    stopHaloAnimation();

                    view.animate().cancel();
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(160)
                            .setInterpolator(
                                    new DecelerateInterpolator()
                            )
                            .start();

                    return true;
            }

            return true;
        });
    }

    private void startHaloAnimation() {
        recordHalo.setVisibility(View.VISIBLE);
        recordHalo.setScaleX(0.85f);
        recordHalo.setScaleY(0.85f);
        recordHalo.setAlpha(0.65f);

        haloAnimator = ValueAnimator.ofFloat(0f, 1f);
        haloAnimator.setDuration(850);
        haloAnimator.setRepeatCount(ValueAnimator.INFINITE);

        haloAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();

            float scale = 0.85f + (0.45f * progress);

            recordHalo.setScaleX(scale);
            recordHalo.setScaleY(scale);
            recordHalo.setAlpha(0.65f * (1f - progress));
        });

        haloAnimator.start();
    }

    private void stopHaloAnimation() {
        if (haloAnimator != null) {
            haloAnimator.cancel();
            haloAnimator = null;
        }

        recordHalo.animate().cancel();
        recordHalo.setAlpha(0f);
        recordHalo.setVisibility(View.INVISIBLE);
        recordHalo.setScaleX(0.85f);
        recordHalo.setScaleY(0.85f);
    }

    private void startRecording() {
        if (isRecording) return;

        isRecording = true;

        // TODO: ADD RECORDING LOGIC HERE
    }

    private void stopRecording() {
        if (!isRecording) return;
        isRecording = false;
    }
}