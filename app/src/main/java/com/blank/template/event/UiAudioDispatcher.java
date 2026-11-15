package com.blank.template.event;

import android.os.Handler;
import android.os.Looper;

/**
 * Dispatcher class for sending audio data and processing errors to the UI.
 */
public class UiAudioDispatcher {
    private UiAudioListener listener;

    private final Handler UiHandler = new Handler(Looper.getMainLooper());

    /**
     * Registers the listener that receives audio events on the main thread.
     *
     * @param listener listener to notify, or {@code null} to disable notifications
     */
    public void setListener(UiAudioListener listener) {
        this.listener = listener;
    }

    /**
     * Dispatches audio metrics to the listener on the main thread.
     *
     * @param f0 fundamental frequency in hertz
     * @param magnitude signal magnitude, represented by the audio-processing pipeline
     */
    public void dispatch(final double f0, final double magnitude) {
        if (listener != null) {
            UiHandler.post(() -> {
                try {
                    listener.onAudioDataReceived(f0, magnitude);
                } catch (Exception e) {
                    listener.onAudioProcessingError(e);
                }
            });
        }
    }

    /**
     * Dispatches an audio-processing error to the listener on the main thread.
     *
     * @param e exception raised while processing audio
     */
    public void dispatchError(Exception e) {
        if (listener != null) {
            UiHandler.post(() -> {
                listener.onAudioProcessingError(e);
            });
        }
    }
}
