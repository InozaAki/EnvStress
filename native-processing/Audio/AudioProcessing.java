package Audio;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class AudioProcessing {

    private static final double MIN_F0 = 80.0;
    private static final double MAX_F0 = 300.0;
    private static final double MIN_SPECTRUM_FREQ = 80.0;
    private static final double MAX_SPECTRUM_FREQ = 4000.0;

    private static final double SILENCE_THRESHOLD = 1.0; 

    public static void analyzeAudioChunk(double[] audioChunk, int sampleRate, String outputFile, int chunkIndex) {
        double[] fftResult = computeFFT(audioChunk);
        
        int fftSize = fftResult.length * 2;
        double maxMagnitude = 0.0;

        for (int i = 1; i < fftResult.length; i++) {
            double frequency = indexToFrequency(i, sampleRate, fftSize);
            if (frequency >= MIN_SPECTRUM_FREQ && frequency <= MAX_SPECTRUM_FREQ) {
                maxMagnitude = Math.max(maxMagnitude, fftResult[i]);
            }
        }

        double timeInSeconds = (chunkIndex * (double) audioChunk.length) / sampleRate;

        if (chunkIndex == 0) {
            writeHeaders(outputFile);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            if (maxMagnitude < SILENCE_THRESHOLD) {
                writer.printf("%.3f\t%.2f\t%.4f\t%s%n", timeInSeconds, 0.0, maxMagnitude, "Silencio");
                System.out.printf("[%.3fs] Silencio detectado (Mag: %.4f)%n", timeInSeconds, maxMagnitude);
            } else {
                double f0 = searchFundamentalFrequency(fftResult, sampleRate);
                writer.printf("%.3f\t%.2f\t%.4f\t%s%n", timeInSeconds, f0, maxMagnitude, "Voz Activa");
                System.out.printf("[%.3fs] Voz Activa -> F0: %6.2f Hz | Mag: %.4f%n", timeInSeconds, f0, maxMagnitude);
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo el archivo: " + e.getMessage());
        }
    }
 
    private static void writeHeaders(String outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            writer.println("\n==========================================");
            writer.println("Tiempo(s)\tF0(Hz)\tMagnitudMax\tEstado");
            writer.println("==========================================");
        } catch (IOException e) {
            System.err.println("Error escribiendo encabezados: " + e.getMessage());
        }
    }

    public static void writeEndOfProcessing(String outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            writer.println("==========================================");
            writer.println("Fin del procesamiento de audio");
            writer.println("==========================================");
        } catch (IOException e) {
            System.err.println("Error escribiendo el final del archivo: " + e.getMessage());
        }
    }

    private static double[] computeFFT(double[] input) {
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

    private static double searchFundamentalFrequency(double[] fftResult, int sampleRate) {
        int fftSize = fftResult.length * 2;
        int minIndex = Math.max(frequencyToIndex(MIN_F0, sampleRate, fftSize), 1);
        int maxIndex = Math.min(frequencyToIndex(MAX_F0, sampleRate, fftSize), fftResult.length - 1);

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

    private static double indexToFrequency(int index, int sampleRate, int fftSize) {
        return (double) index * sampleRate / fftSize;
    }

    private static int frequencyToIndex(double frequency, int sampleRate, int fftSize) {
        return (int) Math.round(frequency * fftSize / sampleRate);
    }
}