package com.blank.template.audio.processing;

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

    /**
     * Processes the given audio data asynchronously.
     * @param audioData The audio data to process.
     */
    public void process(@NotNull byte[] audioData) {
        if(executorService == null || executorService.isShutdown()) return;
        executorService.submit(() -> processAudioData(audioData));
    }

    private void processAudioData(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return;
        }

        int numSamples = audioData.length / 2;
        double[] floatChunk = new double[numSamples];

        for (int i = 0; i < numSamples; i++) {
            int low = audioData[2 * i] & 0xff;
            int high = audioData[2 * i + 1];
            floatChunk[i] = (short) ((high << 8) | low) / AudioConfig.PCM_16_BIT_MAX;
        }

        int fftSize = Integer.highestOneBit(numSamples);
        if (fftSize < numSamples) {
            fftSize <<= 1;
        }

        double[] fftInput = new double[fftSize];
        System.arraycopy(floatChunk, 0, fftInput, 0, numSamples);

        double[] fftOutput = FFT.compute(fftInput);

        //TODO: Further processing of fftOutput, such as filtering, feature extraction, etc.

        sendToUiThread(fftOutput);
    }

    private void sendToUiThread(double[] fftOutput) {
        if(dispatcher == null) return;
        dispatcher.dispatch(
                FFT.fundamentalFrequency(fftOutput, AudioConfig.SAMPLE_RATE),
                FFT.magnitudeSpectrum(fftOutput)
        );
    }

    /**
     * Shuts down the executor service when the application is destroyed.
     */
    public void onDestroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

}
