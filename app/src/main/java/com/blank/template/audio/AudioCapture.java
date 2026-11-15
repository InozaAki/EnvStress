package com.blank.template.audio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import androidx.core.app.ActivityCompat;
import com.blank.template.config.AudioConfig;

/**
 * AudioCapture class is responsible for recording audio data
 */
public class AudioCapture {
    private AudioRecord audioRecord;

    /** Indicates whether audio capture is currently active. */
    public volatile boolean isRecording = false;

    /**
     * Creates an audio recorder using the application's audio configuration.
     *
     * @param context context used to verify microphone permission
     */
    public AudioCapture(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            audioRecord = new AudioRecord(
                    AudioConfig.AUDIO_SOURCE,
                    AudioConfig.SAMPLE_RATE,
                    AudioConfig.CHANNEL_CONFIG,
                    AudioConfig.AUDIO_FORMAT,
                    AudioConfig.BUFFER_SIZE
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts recording when the underlying {@link AudioRecord} is initialized.
     */
    public void startRecording() {
        if (isInitialized()) {
            try {
                audioRecord.startRecording();
                isRecording = true;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stops recording when the underlying {@link AudioRecord} is initialized.
     */
    public void stopRecording() {
        if (isInitialized()) {
            try {
                audioRecord.stop();
                isRecording = false;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads one chunk of raw PCM audio from the recorder.
     *
     * @return the bytes read, or {@code null} when the recorder is unavailable or no
     *         positive amount of data was read
     */
    public byte[] readAudioDataChunk() {
        if (isInitialized()) {
            byte[] buffer = new byte[AudioConfig.BUFFER_SIZE];
            int bytesRead = audioRecord.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                byte[] validData = new byte[bytesRead];
                System.arraycopy(buffer, 0, validData, 0, bytesRead);
                return validData;
            }
        }
        return null;
    }

    /**
     * Releases the underlying {@link AudioRecord} resources.
     */
    public void release() {
        if (audioRecord != null) {
            try {
                audioRecord.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            audioRecord = null;
        }
    }

    /**
     * Checks whether the underlying recorder was created and initialized successfully.
     *
     * @return {@code true} when the recorder is ready for capture
     */
    public boolean isInitialized() {
        return audioRecord != null && audioRecord.getState() == AudioRecord.STATE_INITIALIZED;
    }
}