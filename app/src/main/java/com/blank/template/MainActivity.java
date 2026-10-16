package com.blank.template;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blank.template.audio.AudioCapture;
import com.blank.template.utils.HomeAnimation;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private AudioCapture audioCapture;
    private boolean isRecording = false;
    private ImageButton btnRecord;
    private HomeAnimation anim;

    private Thread recordingThread;
    private ByteArrayOutputStream audioOutputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View wave = findViewById(R.id.onda1);
        View wave2 = findViewById(R.id.onda2);
        View wave3 = findViewById(R.id.onda3);
        View recordHalo = findViewById(R.id.recordHalo);

        audioCapture = new AudioCapture(this);

        anim = new HomeAnimation(new View[]{wave, wave2, wave3}, recordHalo);
        anim.startWaveAnimation();

        btnRecord = findViewById(R.id.recordButton);
        setupRecordAnimation();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupRecordAnimation() {
        btnRecord.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (!hasAudioPermission()) {
                        requestAudioPermission();
                        return true;
                    }

                    view.animate().cancel();
                    view.animate()
                            .scaleX(0.93f)
                            .scaleY(0.93f)
                            .setDuration(80)
                            .start();

                    anim.startHaloAnimation();
                    startRecording();
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (!hasAudioPermission()) return true;

                    stopRecording();
                    anim.stopHaloAnimation();

                    view.animate().cancel();
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(160)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();

                    return true;
            }
            return true;
        });
    }

    private void startRecording() {
        if (isRecording) return;
        if (!audioCapture.isInitialized()) {
            Toast.makeText(this, "AudioRecorder not initialized. Check permissions.", Toast.LENGTH_SHORT).show();
            return;
        }

        isRecording = true;
        audioOutputStream = new ByteArrayOutputStream();
        audioCapture.startRecording();

        recordingThread = new Thread(() -> {
            while (isRecording) {
                byte[] data = audioCapture.readAudioDataChunk();
                if (data != null && data.length > 0) {
                    audioOutputStream.write(data, 0, data.length);
                }
            }
        });
        recordingThread.start();
    }

    private void stopRecording() {
        if (!isRecording) return;

        isRecording = false;
        if (recordingThread != null) {
            recordingThread.interrupt();
        }
        audioCapture.stopRecording();

        if (audioOutputStream != null) {
            byte[] audioData = audioOutputStream.toByteArray();
            audioOutputStream.reset();
            System.out.println("Audio data length: " + audioData.length);
            System.out.println("Audio data (first 100 bytes): " + java.util.Arrays.toString(java.util.Arrays.copyOf(audioData, Math.min(audioData.length, 100))));
        }
    }

    private boolean hasAudioPermission() {
        return checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO)) {
            new AlertDialog.Builder(this)
                    .setTitle("Se requiere permiso de micrófono")
                    .setMessage("La app necesita acceso a tu micrófono para grabar audio.")
                    .setPositiveButton("Dar permiso", (dialog, which) -> {
                        requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de micrófono concedido. Ahora puedes grabar audio.", Toast.LENGTH_SHORT).show();
            } else {
                if (!shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permiso de micrófono denegado")
                            .setMessage("Por favor, ve a la configuración de la aplicación para habilitarlo manualmente.")
                            .setPositiveButton("Abrir configuración", (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                } else {
                    Toast.makeText(this, "Permiso de micrófono denegado.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        isRecording = false;
        if (recordingThread != null) {
            recordingThread.interrupt();
        }
        super.onDestroy();
        if (audioCapture != null) {
            audioCapture.release();
        }
    }
}