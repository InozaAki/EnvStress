package com.blank.template;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

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
}