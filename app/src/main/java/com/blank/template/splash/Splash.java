package com.blank.template.splash;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blank.template.MainActivity;
import com.blank.template.R;

public class Splash extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView phrase = findViewById(R.id.phrase);
        ImageView pulse = findViewById(R.id.pulse_indicator);

        phrase.setText(Phrases.PHRASES[(int) (Math.random() * Phrases.PHRASES.length)]);

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.2f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.2f);

        ObjectAnimator breather = ObjectAnimator.ofPropertyValuesHolder(pulse, scaleX, scaleY);

        breather.setDuration(1500);
        breather.setRepeatCount(ValueAnimator.INFINITE);
        breather.setRepeatMode(ValueAnimator.REVERSE);
        breather.setInterpolator(new AccelerateDecelerateInterpolator());

        breather.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            breather.cancel();
            startActivity(new Intent(Splash.this, MainActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 3000);


    }
}