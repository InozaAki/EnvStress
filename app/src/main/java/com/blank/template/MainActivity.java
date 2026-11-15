package com.blank.template;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blank.template.audio.AudioCapture;
import com.blank.template.audio.processing.AudioProcessing;
import com.blank.template.event.UiAudioDispatcher;
import com.blank.template.event.UiAudioListener;
import com.blank.template.utils.HomeAnimation;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private AudioCapture audioCapture;
    private UiAudioDispatcher dispatcher;
    private AudioProcessing audioProcessing;
    private HomeAnimation anim;

    /**
     * Initializes the main screen, audio processing pipeline, and visual animation.
     *
     * @param savedInstanceState previously saved activity state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dispatcher = new UiAudioDispatcher();
        dispatcher.setListener(new UiAudioListener() {
            @Override
            public void onAudioDataReceived(double f0, double magnitude) {
                runOnUiThread(() -> anim.updateMetrics(f0, magnitude));
            }

            @Override
            public void onAudioProcessingError(Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        audioProcessing = new AudioProcessing(dispatcher);

        View wave = findViewById(R.id.onda1);
        View wave2 = findViewById(R.id.onda2);
        View wave3 = findViewById(R.id.onda3);
        View background = findViewById(R.id.background);


        anim = new HomeAnimation(new View[]{wave, wave2, wave3}, background);
        anim.start();

        if (hasAudioPermission()) {
            initAudio();
        } else {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    /**
     * Creates the audio capture instance when necessary and starts recording.
     */
    private void initAudio() {
        if (audioCapture == null) {
            audioCapture = new AudioCapture(this);
        }
        startRecording();
    }

    /**
     * Starts audio capture and submits the processing loop.
     */
    private void startRecording() {
        audioCapture.startRecording();
        audioProcessing.startProcessingLoop(this.audioCapture);
    }

    /**
     * Stops audio capture when it has been initialized.
     */
    private void stopRecording() {
        if (audioCapture != null) audioCapture.stopRecording();
    }

    /**
     * Checks if the application has {@link android.Manifest.permission#RECORD_AUDIO} permission.
     * @return {@code true} when microphone recording permission is granted
     */
    private boolean hasAudioPermission() {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Handles the microphone permission result and initializes audio when access is granted.
     * @param requestCode The request code passed in {@link #requestPermissions}.
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either
     *                     {@link android.content.pm.PackageManager#PERMISSION_GRANTED} or
     *                     {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initAudio();
        } else {
            Toast.makeText(this, "Permiso denegado.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Stops audio capture and releases audio-processing resources.
     */
    @Override
    protected void onDestroy() {
        stopRecording();
        super.onDestroy();
        if (audioCapture != null) audioCapture.release();
        if (audioProcessing != null) audioProcessing.onDestroy();
    }
}