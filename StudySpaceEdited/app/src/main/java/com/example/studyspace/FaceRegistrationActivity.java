package com.example.studyspace;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaceRegistrationActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;

    private PreviewView previewView;
    private MaterialButton btnRegister;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;

    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_registration);

        previewView = findViewById(R.id.preview_view);
        btnRegister = findViewById(R.id.btn_register_face);

        studentId = getIntent().getStringExtra("STUDENT_ID");
        if (studentId == null) studentId = "UNKNOWN";

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Khởi tạo Face Detector
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .enableTracking()
                        .build();

        faceDetector = FaceDetection.getClient(options);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE
            );
        }

        btnRegister.setOnClickListener(v -> {
            List<Float> fakeEmbedding = getMockFaceEmbedding();
            saveFaceDataToFirebase(fakeEmbedding);
        });
    }

    // ================== CAMERA ==================
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(
                                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFace);

                CameraSelector cameraSelector =
                        CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Không thể mở camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // ================== FACE ANALYSIS ==================
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeFace(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty()) {
                        Face face = faces.get(0);
                        Log.d("FaceID", "Đã phát hiện khuôn mặt");
                        // 👉 Sau này: crop mặt + đưa vào MobileFaceNet
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("FaceID", "Lỗi nhận diện khuôn mặt", e)
                )
                .addOnCompleteListener(task -> imageProxy.close());
    }

    // ================== MOCK FACE EMBEDDING ==================
    private List<Float> getMockFaceEmbedding() {
        List<Float> embedding = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 128; i++) {
            embedding.add(random.nextFloat());
        }
        return embedding;
    }

    // ================== FIREBASE ==================
    private void saveFaceDataToFirebase(List<Float> faceEmbedding) {
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang lưu...");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("faceEmbedding", faceEmbedding);
        data.put("hasFaceId", true);

        db.collection("students")
                .document(studentId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(
                            this,
                            "Đăng ký FaceID thành công",
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Lỗi: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Thử lại");
                });
    }

    // ================== PERMISSION ==================
    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(
                        this,
                        "Cần quyền camera",
                        Toast.LENGTH_SHORT
                ).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
