package com.blank.template.config;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Utility class for Audio Capture and Processing Configuration
 */
public final class AudioConfig {
    /**
     * Prevents instantiation of this configuration holder.
     */
    private AudioConfig() {}

    // Audio Capture Configuration
    public static final int SAMPLE_RATE = 44100;
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int BUFFER_SIZE =
            AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT
            );

    // Audio Processing Configuration
    public static final float PCM_16_BIT_MAX = 32768.0f;

    public static final double MIN_F0 = 80.0;
    public static final double MAX_F0 = 300.0;

    // RMS Configuration
    public static final double RMS_THRESHOLD = 0.01;
    public static final double RMS_LEVEL_RANGE = 0.10;
    public static final double RMS_STRESS_RANGE = 0.25;

    // Vocal Stress Configuration
    public static final double VOCAL_F0_STRESS_RANGE = 5.0;

    // Audio Processing Thread Configuration
    public static final int AUDIO_PROCESSING_THREAD_PRIORITY = Thread.NORM_PRIORITY;
    public static final int AUDIO_PROCESSING_FIXED_THREAD_POOL_SIZE = 4;
}
