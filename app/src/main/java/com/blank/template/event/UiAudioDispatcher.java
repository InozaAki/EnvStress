package com.blank.template.event;

import android.os.Handler;
import android.os.Looper;

/**
 * Dispatcher class for sending audio data and processing errors to the UI.
 */
public class UiAudioDispatcher {
    private UiAudioListener listener;

    private final Handler UiHandler = new Handler(Looper.getMainLooper());

    public void setListener(UiAudioListener listener) {
        this.listener = listener;
    }

    public void dispatch(final double f0, final double magnitude) {
        new Thread(() -> {
            try{
                if (listener != null) {
                    UiHandler.post(() -> listener.onAudioDataReceived(f0, magnitude));
                }
            } catch (Exception e) {
                if (listener != null) {
                    UiHandler.post(() -> listener.onAudioProcessingError(e));
                }
            }
        }).start();
    }
}
