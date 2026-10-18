package com.blank.template.audio.processing;

import com.blank.template.config.AudioConfig;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FFT {

    /**
     * Computes the FFT of the input signal and returns the magnitude spectrum.
     *
     * @param input The input signal (time domain).
     * @return The magnitude spectrum (frequency domain).
     */
    private static double[] computeFFT(@NotNull double[] input) {
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
     * Searches for the fundamental frequency (F0) in the FFT result.
     *
     * @param fftResult The magnitude spectrum obtained from the FFT.
     * @param sampleRate The sample rate of the audio signal.
     * @return The fundamental frequency in Hz, or 0.0 if not found.
     */
    public static double searchFundamentalFrequency(@NotNull double[] fftResult, int sampleRate) {
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
        System.out.printf("F0: %.2f Hz | FFT bin: %d | Magnitude: %.4f%n", fundamentalFrequency, fundamentalIndex, maxMagnitude);
        return fundamentalFrequency;
    }

    /**
     * Converts an FFT bin index to its corresponding frequency in Hz.
     *
     * @param index The FFT bin index.
     * @param sampleRate The sample rate of the audio signal.
     * @param fftSize The size of the FFT.
     * @return The corresponding frequency in Hz.
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
     * @return The corresponding FFT bin index.
     */
    public static int frequencyToIndex(double frequency, int sampleRate, int fftSize) {
        return (int) Math.round(frequency * fftSize / sampleRate);
    }
}