package com.blank.template.event;

/**
 * Interface for sending audio data and processing errors to the UI.
 */
public interface UiAudioListener {
    /**
     * Receives the latest fundamental frequency and signal magnitude.
     *
     * @param f0 fundamental frequency in hertz
     * @param magnitude signal magnitude from the audio processor
     */
    void onAudioDataReceived(double f0, double magnitude);

    /**
     * Receives an error raised during audio capture or processing.
     *
     * @param e processing exception
     */
    void onAudioProcessingError(Exception e);
}
