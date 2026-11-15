package com.blank.template.audio.processing;

import com.blank.template.config.AudioConfig;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FFT {

    /**
     * Computes the fast Fourier transform of a time-domain signal and returns its
     * positive-frequency magnitude spectrum.
     *
     * @param input The input signal (time domain).
     * @return the magnitude spectrum containing {@code input.length / 2} bins
     * @throws IllegalArgumentException if the input length is not a power of two
     */
    public static double[] compute(@NotNull double[] input) {
        int n = input.length;
        if ((n & (n - 1)) != 0) throw new IllegalArgumentException("Input length must be a power of 2");

        double[] real = Arrays.copyOf(input, n);
        double[] imag = new double[n];
        int levels = 31 - Integer.numberOfLeadingZeros(n);

        for (int i = 0; i < n; i++) {
            int j = Integer.reverse(i) >>> (32 - levels);
            if (j > i) {
                double tempR = real[i];
                real[i] = real[j];
                real[j] = tempR;

                double tempI = imag[i];
                imag[i] = imag[j];
                imag[j] = tempI;
            }
        }

        for (int size = 2; size <= n; size <<= 1) {
            int half = size >> 1;
            double angle = -2 * Math.PI / size;
            double wStepR = Math.cos(angle);
            double wStepI = Math.sin(angle);

            for (int i = 0; i < n; i += size) {
                double wR = 1.0;
                double wI = 0.0;
                for (int j = 0; j < half; j++) {
                    int even = i + j;
                    int odd = i + j + half;

                    double tR = real[odd] * wR - imag[odd] * wI;
                    double tI = real[odd] * wI + imag[odd] * wR;

                    real[odd] = real[even] - tR;
                    imag[odd] = imag[even] - tI;
                    real[even] += tR;
                    imag[even] += tI;

                    double newWR = wR * wStepR - wI * wStepI;
                    wI = wR * wStepI + wI * wStepR;
                    wR = newWR;
                }
            }
        }

        double[] mags = new double[n / 2];
        for (int i = 0; i < n / 2; i++) {
            mags[i] = Math.hypot(real[i], imag[i]);
        }
        return mags;
    }

    /**
     * Finds the strongest fundamental-frequency candidate in the configured vocal range.
     *
     * @param fftResult The magnitude spectrum obtained from the FFT.
     * @param sampleRate The sample rate of the audio signal.
     * @return the fundamental frequency in hertz, or {@code 0.0} if no candidate is found
     */
    public static double fundamentalFrequency(@NotNull double[] fftResult, int sampleRate) {
        int fftSize = fftResult.length * 2;
        int minIndex = Math.max(frequencyToIndex(AudioConfig.MIN_F0, sampleRate, fftSize), 1);
        int maxIndex = Math.min(frequencyToIndex(AudioConfig.MAX_F0, sampleRate, fftSize), fftResult.length - 1);

        double maxMagnitude = 0.0;
        int fundamentalIndex = -1;

        for (int i = minIndex; i <= maxIndex; i++) {
            if (fftResult[i] > maxMagnitude) {
                maxMagnitude = fftResult[i];
                fundamentalIndex = i;
            }
        }

        if (fundamentalIndex == -1) {
            System.out.println("F0: No encontrada");
            return 0.0;
        }

        double fundamentalFrequency = indexToFrequency(fundamentalIndex, sampleRate, fftSize);
        return fundamentalFrequency;
    }

    /**
     * Converts an FFT bin index to its corresponding frequency in Hz.
     *
     * @param index The FFT bin index.
     * @param sampleRate The sample rate of the audio signal.
     * @param fftSize The size of the FFT.
     * @return the corresponding frequency in hertz
     */
    public static double indexToFrequency(int index, int sampleRate, int fftSize) {
        return (double) index * sampleRate / fftSize;
    }

    /**
     * Converts a frequency in Hz to its corresponding FFT bin index.
     *
     * @param frequency The frequency in Hz.
     * @param sampleRate The sample rate of the audio signal.
     * @param fftSize The size of the FFT.
     * @return the nearest corresponding FFT bin index
     */
    public static int frequencyToIndex(double frequency, int sampleRate, int fftSize) {
        return (int) Math.round(frequency * fftSize / sampleRate);
    }

    /**
     * Computes the normalized peak magnitude from an FFT magnitude spectrum.
     *
     * @param fftMagnitudeResult The magnitude spectrum array from FFT.compute().
     * @param originalSampleCount number of time-domain samples used to produce the spectrum
     * @return the normalized maximum magnitude, or {@code 0.0} for invalid input
     */
    public static double peakMagnitude(
            @NotNull double[] fftMagnitudeResult,
            int originalSampleCount
    ) {
        if (fftMagnitudeResult.length == 0 || originalSampleCount <= 0) {
            return 0.0;
        }

        double max = 0.0;

        for (double mag : fftMagnitudeResult) {
            if (mag > max) {
                max = mag;
            }
        }

        return (2.0 * max) / originalSampleCount;
    }

    /**
     * Computes the Root Mean Square (RMS) value of the time-domain signal.
     *
     * Used to determine if the signal is only silence and should not
     * be processed, acts as a voice activity detection (VAD)
     *
     * @param timeDomainSignal The time-domain signal.
     * @return the RMS value, or {@code 0.0} when the signal is empty
     */
    public static double calculateRMS(@NotNull double[] timeDomainSignal) {
        if (timeDomainSignal.length == 0) return 0.0;

        double sum = 0.0;
        for (double sample : timeDomainSignal) {
            sum += sample * sample;
        }

        return Math.sqrt(sum / timeDomainSignal.length);
    }
}