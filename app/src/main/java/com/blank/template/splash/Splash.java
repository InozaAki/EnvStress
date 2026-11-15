package com.blank.template.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blank.template.MainActivity;
import com.blank.template.R;

/**
 * Splash Screen / Loading screen
 * Launched first, shows a random phrase and
 * has a loading ring animation (no resources are being actually loaded)
 */
public class Splash extends AppCompatActivity {
    /**
     * Displays a random calming phrase and animated loading ring before opening the main screen.
     *
     * @param savedInstanceState previously saved activity state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView phrase = findViewById(R.id.phrase);

        phrase.setText(
                Phrases.PHRASES[
                        (int) (Math.random() * Phrases.PHRASES.length)
                        ]
        );

        RingHighlightView highlight = findViewById(R.id.ring_highlight);

        highlight.startAnimation();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            highlight.stopAnimation();

            startActivity(new Intent(Splash.this, MainActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 4000);
    }
}