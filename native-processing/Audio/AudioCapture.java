package Audio;

import java.io.File;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class AudioCapture {

    public AudioCapture() {}

    private final String FILE_PATH = System.getenv("AUDIO_FILE_PATH") != null ? System.getenv("AUDIO_FILE_PATH") : "/home/axelespinosa/College/EnvStress/native-processing/utils/";
    private static final int CHUNK_SIZE = 30;
    
    private AudioInputStream[] audioInput;

    public void init() {

        System.out.println("Initializing audio capture from directory: " + FILE_PATH);

        File audioDirectory = new File(FILE_PATH);

        if (!audioDirectory.exists() || !audioDirectory.isDirectory()) {
            throw new RuntimeException("Audio directory not found: " + FILE_PATH);
        }

        File[] files = audioDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));

        if (files == null || files.length == 0) {
            throw new RuntimeException("No audio files found in directory: " + FILE_PATH);
        }

        audioInput = new AudioInputStream[files.length];

        for (File file : files) {
            try {
                audioInput[0] = AudioSystem.getAudioInputStream(file);
            } catch (Exception e) {
                throw new RuntimeException("Error reading audio file: " + file.getName(), e);
            }
        }

        for (AudioInputStream stream : audioInput) {
            processAudioInChunks(stream);
        }
    }

    private static void processAudioInChunks(AudioInputStream audioInputStream) {
        AudioFormat format = audioInputStream.getFormat();
        
        int frameSize = format.getFrameSize();
        float sampleRate = format.getSampleRate();

        int bytesPerMillisecond = (int) ( (sampleRate * frameSize) / 1000);
        int chunkSize = bytesPerMillisecond * CHUNK_SIZE;

        chunkSize = (chunkSize / frameSize) * frameSize;

        byte[] buffer = new byte[chunkSize];
        int bytesRead;

        try{
            while((bytesRead = audioInputStream.read(buffer)) != -1) {
                byte[] actualChunk = (bytesRead == chunkSize) ? buffer : Arrays.copyOf(buffer, bytesRead);
                handleChunk(actualChunk, (int) sampleRate);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing audio stream", e);
        }

    }

    private static void handleChunk(byte[] chunk, int sampleRate) {
        int numSamples = chunk.length / 2;
        double[] floatChunk = new double[numSamples];
        
        for (int i = 0; i < numSamples; i++) {
            int low = chunk[2 * i] & 0xff;
            int high = chunk[2 * i + 1];
            floatChunk[i] = (short) ((high << 8) | low) / 32768.0f;
        } 

        int fftSize = Integer.highestOneBit(numSamples);
        if (fftSize < numSamples) {
            fftSize <<= 1;
        }

        double[] fftInput = new double[fftSize];
        System.arraycopy(floatChunk, 0, fftInput, 0, numSamples);
        
        AudioProcessing.analyzeAudioChunk(fftInput, sampleRate);
    }

    /*

    Class AudioCapture (CASI LISTO)

    1. Llega audio, o metemos audio. Llega en bytes.
    2. Transformamos los bytes en un arreglo de flotantes que representan las ondas del audio
    3. Ahora ese audio lo dividimos en tramas de 20 a 30 milisegundos.

    Class AudioProcessing (CASI LISTO)

    4. Todas esas tramas se las metemos al algoritmo de FFT (siempre a una potencia de 2)
    5. FFT nos regresa la amplitud de la frecuencia en ese milisegundo en especifico
    
    TODO: Class AudioAnalysis
    
    6. Determinamos y categorizamos las frecuencias
    7. Checamos cuales frecuencias tienen mayor amplitud y comparamos.
    8. Cuando ya tenemos categorizadas las frecuencias por amplitud podemos dar un veredicto de, juntando estas tramas de audio podemos concluir X.
        
    
    */
    
    
}
