package com.blank.template.utils;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class HomeAnimation {

    private final View[] waves;
    private final View halo;

    private ValueAnimator haloAnimator;

    public HomeAnimation(View[] waves, View halo) {
        this.waves = waves;
        this.halo = halo;
    }

    public void startWaveAnimation() {
        animWave(waves[0], 0);
        animWave(waves[1], 2000);
        animWave(waves[2], 4000);
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

    public void startHaloAnimation() {
        halo.setVisibility(View.VISIBLE);
        halo.setScaleX(0.85f);
        halo.setScaleY(0.85f);
        halo.setAlpha(0.65f);

        haloAnimator = ValueAnimator.ofFloat(0f, 1f);
        haloAnimator.setDuration(850);
        haloAnimator.setRepeatCount(ValueAnimator.INFINITE);

        haloAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();

            float scale = 0.85f + (0.45f * progress);

            halo.setScaleX(scale);
            halo.setScaleY(scale);
            halo.setAlpha(0.65f * (1f - progress));
        });

        haloAnimator.start();
    }

    public void stopHaloAnimation() {
        if (haloAnimator != null) {
            haloAnimator.cancel();
            haloAnimator = null;
        }

        halo.animate().cancel();
        halo.setAlpha(0f);
        halo.setVisibility(View.INVISIBLE);
        halo.setScaleX(0.85f);
        halo.setScaleY(0.85f);
    }
}
