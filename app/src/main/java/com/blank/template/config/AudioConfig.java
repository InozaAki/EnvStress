package com.blank.template.config;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public final class AudioConfig {
    AudioConfig() {}

    // Audio Capture Configuration
    public static int SAMPLE_RATE = 44100;
    public static int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    public static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);


    // Audio Processing Configuration
    public static final int FFT_SIZE = 2048;
    public static final float PCM_16_BIT_MAX = 32768.0f;
    public static final double MIN_F0 = 80.0;
    public static final double MAX_F0 = 300.0;
    public static final double MIN_SPECTRUM_FREQ = 80.0;
    public static final double MAX_SPECTRUM_FREQ = 4000.0;
    public static final double SILENCE_THRESHOLD = 1.0;

}
