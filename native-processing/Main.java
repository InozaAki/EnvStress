import Audio.AudioCapture;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting audio processing...");
        AudioCapture audioCapture = new AudioCapture();
        audioCapture.init();
    }
}