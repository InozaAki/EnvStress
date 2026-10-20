package com.blank.template.event;

/**
 * Interface for sending audio data and processing errors to the UI.
 */
public interface UiAudioListener {
    //TODO: Implement the correct parameters that should be sent to the UI
    void onAudioDataReceived(byte[] audioData);
    void onAudioProcessingError(Exception e);
}
