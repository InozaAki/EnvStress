package Audio;

import java.util.Arrays;

public class AudioProcessing {

    public static void analyzeAudioChunk(double[] audioChunk, int sampleRate) {
        System.out.println("Analyzing audio chunk of size: " + audioChunk.length + " at sample rate: " + sampleRate);
        double[] fftResult = computeFFT(audioChunk);
        printFFTInfo(fftResult, sampleRate);
    }

    private static double[] computeFFT(double[] input) {
        int n = input.length;
        double[] real = Arrays.copyOf(input, n);
        double[] imag = new double[n];

        int levels = 31 - Integer.numberOfLeadingZeros(n);
        for (int i = 0; i < n; i++) {
            int j = Integer.reverse(i) >>> (32 - levels);
            if (j > i) {
                double tmpR = real[i];
                real[i] = real[j];
                real[j] = tmpR;
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
                    double rOdd = real[odd];
                    double iOdd = imag[odd];
                    double tR = rOdd * wR - iOdd * wI;
                    double tI = rOdd * wI + iOdd * wR;
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

    private static void printFFTInfo(double[] fftResult, int sampleRate) {
        for (float frequency : new float[]{1000, 2000, 3000}) {
            float index = frecuencyToIndex(frequency, sampleRate, fftResult.length * 2);
            if (index < fftResult.length) {
                System.out.println("Magnitude at " + frequency + " Hz: " + fftResult[(int) index]);
            } else {
                System.out.println("Frequency " + frequency + " Hz is out of FFT range.");
            }
        }
    }

    private static float frecuencyToIndex(float frequency, int sampleRate, int chunkSize) {
        return (frequency / sampleRate) * chunkSize;
    }
}
