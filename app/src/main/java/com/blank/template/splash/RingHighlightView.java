package com.blank.template.splash;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;

/**
 * Utility class for animating the loading ring
 */
public class RingHighlightView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private ObjectAnimator animator;
    private float rotationAngle = 0f;

    private float centerX;
    private float centerY;
    private float radius;

    /**
     * Creates a ring highlight view with the default style.
     *
     * @param context the view context
     */
    public RingHighlightView(Context context) {
        super(context);
        init();
    }

    /**
     * Creates a ring highlight view using XML attributes.
     *
     * @param context the view context
     * @param attrs the XML attributes used to configure the view
     */
    public RingHighlightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Creates a ring highlight view using XML attributes and a default style.
     *
     * @param context the view context
     * @param attrs the XML attributes used to configure the view
     * @param defStyleAttr the default style attribute
     */
    public RingHighlightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Configures the paint used to draw the animated ring.
     */
    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float density = getResources().getDisplayMetrics().density;
        paint.setStrokeWidth(26f * density);
    }

    /**
     * Recalculates the ring geometry and gradient when the view size changes.
     *
     * @param w the new width in pixels
     * @param h the new height in pixels
     * @param oldw the previous width in pixels
     * @param oldh the previous height in pixels
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerX = w / 2f;
        centerY = h / 2f;
        radius = Math.min(centerX, centerY) * 0.87f;

        oval.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        int baseColor = Color.argb(255, 190, 255, 242);

        int[] colors = {
                Color.TRANSPARENT,
                Color.argb(35, 190, 255, 242),
                Color.argb(110, 190, 255, 242),
                baseColor,
                Color.argb(110, 190, 255, 242),
                Color.argb(35, 190, 255, 242),
                Color.TRANSPARENT
        };

        float[] positions = {
                0.00f,
                0.02f,
                0.04f,
                0.07f,
                0.10f,
                0.13f,
                0.15f
        };

        SweepGradient sweepGradient = new SweepGradient(centerX, centerY, colors, positions);
        paint.setShader(sweepGradient);
    }

    /**
     * Starts the continuous seven-second rotation animation.
     */
    public void startAnimation() {
        if (animator != null && animator.isRunning()) {
            return;
        }

        animator = ObjectAnimator.ofFloat(this, "rotationAngle", 0f, 360f);
        animator.setDuration(7000);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    /**
     * Stops and clears the ring rotation animation, if it is running.
     */
    public void stopAnimation() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    /**
     * Updates the angle used to rotate the ring and redraws the view.
     *
     * @param angle the rotation angle in degrees
     */
    public void setRotationAngle(float angle) {
        this.rotationAngle = angle;
        invalidate();
    }

    /**
     * Returns the current ring rotation angle.
     *
     * @return the rotation angle in degrees
     */
    public float getRotationAngle() {
        return rotationAngle;
    }

    /**
     * Draws the gradient ring at its current rotation angle.
     *
     * @param canvas the canvas on which the ring is drawn
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.rotate(rotationAngle, centerX, centerY);
        canvas.drawArc(oval, 0f, 360f, false, paint);

        canvas.restore();
    }

    /**
     * Releases the animation when the view is detached from the window.
     */
    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }
}