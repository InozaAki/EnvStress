package com.blank.template.audio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import androidx.core.app.ActivityCompat;
import com.blank.template.utils.AudioConfig;

public class AudioCapture {
    private AudioRecord audioRecord;

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

    public void startRecording() {
        if (isInitialized()) {
            try {
                audioRecord.startRecording();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRecording() {
        if (isInitialized()) {
            try {
                audioRecord.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

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

    public boolean isInitialized() {
        return audioRecord != null && audioRecord.getState() == AudioRecord.STATE_INITIALIZED;
    }
}