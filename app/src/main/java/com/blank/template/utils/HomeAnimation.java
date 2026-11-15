package com.blank.template.utils;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.blank.template.config.AudioConfig;

public class HomeAnimation {
    private static final float WAVE_PERIOD_CALM = 4f;
    private static final float WAVE_PERIOD_STRESSED = 2f;
    private static final float SMOOTHING_TAU = 0.6f;

    private static final int[] PALETTE = {
            Color.parseColor("#EDF6F9"), // Alice Blue
            Color.parseColor("#F6E9E5"), // Blanco cálido
            Color.parseColor("#FFDDD2"), // Almond Silk
            Color.parseColor("#E29578")  // Tangerine Dream
    };

    private static final float[] PALETTE_POS = {0f, 0.25f, 0.5f, 1f};
    private static final int WAVE_CALM = 0xFF006D77;
    private static final int WAVE_STRESSED = 0xFF00424A;

    private final View[] waves;
    private final View background;
    private final ArgbEvaluator evaluator = new ArgbEvaluator();
    private final ValueAnimator clock = ValueAnimator.ofFloat(0f, 1f);

    private final GradientDrawable backgroundGradient;

    private long lastNanos;
    private float wavePhase;
    private float targetStress, stress, targetLevel, level;

    private double dynamicBaselineF0 = 0.0;
    private int calibrationSamplesCount = 0;
    private static final int REQUIRED_CALIBRATION_SAMPLES = 50;
    private static final int SMOOTHING_WINDOW_SIZE = 10;
    private final float[] stressHistory = new float[SMOOTHING_WINDOW_SIZE];
    private int historyIndex = 0;
    private int historyCount = 0;

    public HomeAnimation(View[] waves, View background) {
        this.waves = waves;
        this.background = background;
        this.backgroundGradient = new GradientDrawable();
        this.backgroundGradient.setOrientation(GradientDrawable.Orientation.TL_BR);
        if (this.background != null) {
            this.background.setBackground(this.backgroundGradient);
        }
        clock.setDuration(1000);
        clock.setRepeatCount(ValueAnimator.INFINITE);
        clock.setInterpolator(new LinearInterpolator());
        clock.addUpdateListener(a -> onFrame());
    }

    public void start() { lastNanos = System.nanoTime(); clock.start(); }
    public void stop()  { clock.cancel(); }

    public void updateMetrics(double f0, double magnitude) {

        float environmentalLevel = clamp01(
                (float) ((magnitude - AudioConfig.RMS_THRESHOLD) / AudioConfig.RMS_LEVEL_RANGE)
        );

        boolean voiceDetected = f0 >= AudioConfig.MIN_F0 && f0 <= AudioConfig.MAX_F0;

        float currentRawStress = 0f;

        if (voiceDetected) {
            if (calibrationSamplesCount < REQUIRED_CALIBRATION_SAMPLES) {
                dynamicBaselineF0 += f0;
                calibrationSamplesCount++;
                if (calibrationSamplesCount == REQUIRED_CALIBRATION_SAMPLES) {
                    dynamicBaselineF0 /= REQUIRED_CALIBRATION_SAMPLES;
                }
                currentRawStress = environmentalLevel;
            } else {
                float pitchStress = calculatePitchStress(f0);
                float volumeStress = clamp01(
                        (float) ((magnitude - AudioConfig.RMS_THRESHOLD) / AudioConfig.RMS_STRESS_RANGE)
                );
                currentRawStress = 0.65f * pitchStress + 0.35f * volumeStress;
            }
        } else {
            currentRawStress = environmentalLevel;
        }

        stressHistory[historyIndex] = currentRawStress;
        historyIndex = (historyIndex + 1) % SMOOTHING_WINDOW_SIZE;
        if (historyCount < SMOOTHING_WINDOW_SIZE) historyCount++;

        float sumStress = 0f;
        for (int i = 0; i < historyCount; i++) {
            sumStress += stressHistory[i];
        }

        targetStress = sumStress / historyCount;
        targetLevel = environmentalLevel;
    }

    private float calculatePitchStress(double f0) {
        if (f0 <= 0 || dynamicBaselineF0 <= 0) {
            return 0f;
        }
        double semitones = 12.0 * Math.log(f0 / dynamicBaselineF0) / Math.log(2.0);

        return clamp01((float) (semitones / AudioConfig.VOCAL_F0_STRESS_RANGE));
    }

    private void onFrame() {
        long now = System.nanoTime();
        float dt = Math.min((now - lastNanos) / 1e9f, 0.05f);
        lastNanos = now;

        float k = 1f - (float) Math.exp(-dt / SMOOTHING_TAU);
        stress += (targetStress - stress) * k;
        level  += (targetLevel  - level)  * k;

        renderWaves(dt);
        renderBackground();
    }

    private void renderWaves(float dt) {
        float period = lerp(WAVE_PERIOD_CALM, WAVE_PERIOD_STRESSED, stress);
        wavePhase = (wavePhase + dt / period) % 1f;

        float maxScale = lerp(4f, 5.5f, stress);
        float peakAlpha = lerp(0.65f, 0.90f, level);

        int currentColor = (int) evaluator.evaluate(stress, WAVE_CALM, WAVE_STRESSED);
        ColorStateList tintList = ColorStateList.valueOf(currentColor);

        for (int i = 0; i < waves.length; i++) {
            float p = (wavePhase + i / (float) waves.length) % 1f;
            float eased = 1f - (1f - p) * (1f - p);
            float fadeIn = Math.min(1f, p / 0.1f);
            float alpha = peakAlpha * fadeIn * (1f - p) * (1f - p);

            float scale = 1f + (maxScale - 1f) * eased;
            waves[i].setScaleX(scale);
            waves[i].setScaleY(scale);
            waves[i].setAlpha(alpha);

            waves[i].setBackgroundTintList(tintList);
        }
    }

    private void renderBackground() {
        if (background == null) return;

        float slowTime = (System.currentTimeMillis() % 8000) / 8000f;
        float breathingOffset = (float) (Math.sin(slowTime * Math.PI * 2) * 0.15f);
        int colorStart = colorAt(PALETTE_POS, stress);
        int colorEnd = colorAt(PALETTE_POS, clamp01(stress + 0.20f + breathingOffset));

        backgroundGradient.setColors(new int[]{colorStart, colorEnd});
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    private static float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
    private int colorAt(float[] pos, float t) {
        for (int i = 0; i < HomeAnimation.PALETTE.length - 1; i++) {
            if (t <= pos[i + 1]) {
                float local = (t - pos[i]) / (pos[i + 1] - pos[i]);
                return (int) evaluator.evaluate(local, HomeAnimation.PALETTE[i], HomeAnimation.PALETTE[i + 1]);
            }
        }
        return HomeAnimation.PALETTE[HomeAnimation.PALETTE.length - 1];
    }
}
