package com.blank.template.audio.processing;

import com.blank.template.audio.AudioCapture;
import com.blank.template.config.AudioConfig;
import com.blank.template.event.UiAudioDispatcher;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioProcessing {

    private final ExecutorService executorService;
    private final UiAudioDispatcher dispatcher;

    public AudioProcessing(UiAudioDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        executorService = Executors.newFixedThreadPool(
                AudioConfig.AUDIO_PROCESSING_FIXED_THREAD_POOL_SIZE,
                r -> {
                    Thread thread = new Thread(r);
                    thread.setPriority(AudioConfig.AUDIO_PROCESSING_THREAD_PRIORITY);
                    return thread;
                }
        );
    }

    public void startProcessingLoop(@NotNull AudioCapture audioCapture){
        if(executorService == null || executorService.isShutdown()) return;

        try {
            executorService.submit(() -> {
                while(!Thread.currentThread().isInterrupted() && audioCapture.isRecording){
                    byte[] audioData = audioCapture.readAudioDataChunk();
                    if (audioData != null && audioData.length > 0) {
                        processAudioData(audioData);
                    }
                }
            });
        } catch (Exception e) {
            dispatcher.dispatchError(e);
        }
    }

    private void processAudioData(byte[] audioData) {
        if (audioData == null || audioData.length == 0) return;

        int numSamples = audioData.length / 2;
        double[] floatChunk = new double[numSamples];

        for (int i = 0; i < numSamples; i++) {
            int low = audioData[2 * i] & 0xff;
            int high = audioData[2 * i + 1];
            floatChunk[i] = (short) ((high << 8) | low) / AudioConfig.PCM_16_BIT_MAX;
        }

        double rmsVolume = FFT.calculateRMS(floatChunk);

        if (rmsVolume < AudioConfig.RMS_THRESHOLD) {
            sendToUiThread(0.0, rmsVolume);
            return;
        }

        int fftSize = Integer.highestOneBit(numSamples);
        if (fftSize < numSamples) fftSize <<= 1;

        double[] fftInput = new double[fftSize];
        System.arraycopy(floatChunk, 0, fftInput, 0, numSamples);

        double[] fftOutput = FFT.compute(fftInput);

        double f0 = FFT.fundamentalFrequency(
                fftOutput,
                AudioConfig.SAMPLE_RATE
        );

        if (f0 < AudioConfig.MIN_F0 || f0 > AudioConfig.MAX_F0) {
            f0 = 0.0;
        }

        sendToUiThread(f0, rmsVolume);
    }

    private void sendToUiThread(double f0, double magnitude) {
        if(dispatcher == null) return;
        dispatcher.dispatch(f0, magnitude);
    }

    public void onDestroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}