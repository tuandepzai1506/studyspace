package com.example.studyspace;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.adapters.ScoreAdapter;
import com.example.studyspace.models.ScoreResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class scoreTable extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ScoreAdapter scoreAdapter;
    private List<ScoreResult> listScore;
    private TextView tvEmpty;
    private ImageView btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.score_table);

        // Xử lý Insets để không bị che bởi thanh trạng thái
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.score_table1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 2. Ánh xạ View
        recyclerView = findViewById(R.id.recycler_view_scores);
        tvEmpty = findViewById(R.id.tv_empty_score);
        btnBack = findViewById(R.id.btn_back);

        // 3. Cài đặt RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listScore = new ArrayList<>();
        scoreAdapter = new ScoreAdapter(listScore);
        recyclerView.setAdapter(scoreAdapter);

        // 4. Xử lý nút Back
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 5. Lấy dữ liệu
        loadScoreData();
    }

    private void loadScoreData() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();

        // Lấy từ bảng "quiz_results", lọc theo User ID, sắp xếp ngày mới nhất lên đầu
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");

                    // Bước 2: Chỉ học sinh mới được load dữ liệu điểm
                    if ("student".equals(role)) {
                        fetchQuizResults(currentUserId);
                    } else {
                        // Nếu là giáo viên, hiển thị thông báo trống hoặc ẩn danh sách
                        listScore.clear();
                        scoreAdapter.notifyDataSetChanged();
                        tvEmpty.setText("Tài khoản giáo viên không hiển thị điểm cá nhân.");
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ScoreTable", "Lỗi lấy điểm: ", e);
                    // Lưu ý: Nếu báo lỗi "requires an index", hãy check Logcat và bấm vào link để tạo Index
                    Toast.makeText(scoreTable.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }
    private void fetchQuizResults(String userId) {
        db.collection("quiz_results")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listScore.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ScoreResult result = document.toObject(ScoreResult.class);
                        listScore.add(result);
                    }
                    scoreAdapter.notifyDataSetChanged();

                    if (listScore.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }
}