package com.blank.template.utils;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class HomeAnimation {
    private static final float WAVE_PERIOD_CALM = 4f;
    private static final float WAVE_PERIOD_STRESSED = 2f;
    private static final float HALO_PERIOD_CALM = 0.85f;
    private static final float HALO_PERIOD_STRESSED = 0.45f;
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
    private final View halo;
    private final View background;
    private final ArgbEvaluator evaluator = new ArgbEvaluator();
    private final ValueAnimator clock = ValueAnimator.ofFloat(0f, 1f);

    private long lastNanos;
    private float wavePhase, haloPhase;
    private boolean haloActive;
    private float targetStress, stress, targetLevel, level;

    public HomeAnimation(View[] waves, View halo, View background) {
        this.waves = waves;
        this.halo = halo;
        this.background = background;

        clock.setDuration(1000);
        clock.setRepeatCount(ValueAnimator.INFINITE);
        clock.setInterpolator(new LinearInterpolator());
        clock.addUpdateListener(a -> onFrame());
    }

    public void start() { lastNanos = System.nanoTime(); clock.start(); }
    public void stop()  { clock.cancel(); }

    public void updateMetrics(double f0, double magnitude) {
        targetStress = clamp01((float) ((f0 - 100.0) / 180.0));
        targetLevel  = clamp01((float) magnitude);
    }

    private void onFrame() {
        long now = System.nanoTime();
        float dt = Math.min((now - lastNanos) / 1e9f, 0.05f);
        lastNanos = now;

        float k = 1f - (float) Math.exp(-dt / SMOOTHING_TAU);
        stress += (targetStress - stress) * k;
        level  += (targetLevel  - level)  * k;

        renderWaves(dt);
        renderHalo(dt);
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

    private void renderHalo(float dt) {
        if (!haloActive) return;
        haloPhase = (haloPhase + dt / lerp(HALO_PERIOD_CALM, HALO_PERIOD_STRESSED, stress)) % 1f;
        float scale = 0.85f + 0.45f * haloPhase;
        halo.setScaleX(scale);
        halo.setScaleY(scale);
        halo.setAlpha(0.65f * (1f - haloPhase));
    }

    private void renderBackground() {
        if (background == null) return;
        background.setBackgroundColor(colorAt(PALETTE_POS, stress));
    }

    public void startHaloAnimation() {
        haloActive = true;
        haloPhase = 0f;
        halo.setVisibility(View.VISIBLE);
    }

    public void stopHaloAnimation() {
        haloActive = false;
        halo.setAlpha(0f);
        halo.setVisibility(View.INVISIBLE);
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
