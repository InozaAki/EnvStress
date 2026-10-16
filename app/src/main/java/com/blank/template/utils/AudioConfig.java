package com.blank.template.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public final class AudioConfig {
    AudioConfig() {}

    public static int SAMPLE_RATE = 44100;
    public static int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    public static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
}
