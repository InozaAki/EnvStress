package com.blank.template.audio.processing;

import com.blank.template.audio.AudioCapture;
import com.blank.template.config.AudioConfig;
import com.blank.template.event.UiAudioDispatcher;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AudioProcessing class is responsible for processing the audio data
 * Uses ExecutorService (Thread Pool) to process the audio data in a separate thread.
 * And sends the data to the UI Thread via the UiAudioDispatcher.
 */
public class AudioProcessing {

    private final ExecutorService executorService;
    private final UiAudioDispatcher dispatcher;

    /**
     * Creates an audio processor backed by a fixed-size worker pool.
     *
     * @param dispatcher dispatcher used to deliver metrics and errors to the UI
     */
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

    /**
     * Starts the audio processing loop via the executor service.
     *
     * Audio chunks are read while capture is active, converted to normalized samples, and
     * analyzed without blocking the main thread.
     *
     * The amount of Threads in the pool can be configured at
     * {@link com.blank.template.config.AudioConfig#AUDIO_PROCESSING_FIXED_THREAD_POOL_SIZE}
     *
     * @param audioCapture recorder used to obtain PCM audio chunks
     */
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

    /**
     * Converts a PCM 16-bit chunk to normalized samples, calculates RMS and fundamental
     * frequency, and sends the resulting metrics to the UI dispatcher.
     *
     * @param audioData raw PCM 16-bit audio bytes
     */
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

    /**
     * Sends audio metrics to the UI thread through the configured dispatcher.
     *
     * @param f0 fundamental frequency in hertz, or {@code 0.0} when unavailable
     * @param magnitude RMS magnitude of the audio signal
     */
    private void sendToUiThread(double f0, double magnitude) {
        if(dispatcher == null) return;
        dispatcher.dispatch(f0, magnitude);
    }

    /**
     * Stops accepting new processing work and shuts down the worker pool.
     */
    public void onDestroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}