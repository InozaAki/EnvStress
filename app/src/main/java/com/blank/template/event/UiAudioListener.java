package com.blank.template.event;

/**
 * Interface for sending audio data and processing errors to the UI.
 */
public interface UiAudioListener {
    void onAudioDataReceived(double f0, double magnitude);
    void onAudioProcessingError(Exception e);
}
