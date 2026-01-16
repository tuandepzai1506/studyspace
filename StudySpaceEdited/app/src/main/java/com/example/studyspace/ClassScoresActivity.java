package com.example.studyspace;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.adapters.ClassScoresAdapter;
import com.example.studyspace.models.StudentScoreData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassScoresActivity extends AppCompatActivity {

    private Spinner spinnerClass;
    private RecyclerView recyclerView;
    private ImageView btnBack;
    private TextView tvEmpty;
    private Spinner spinnerExam;

    private ClassScoresAdapter adapter;
    private List<StudentScoreData> scoreList;
    private List<ClassModel> classList;
    private Map<String, String> examMap; // examId -> examName

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String selectedClassId = "";
    private String selectedExamId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_scores);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupListeners();
        loadClasses();
    }

    private void initViews() {
        spinnerClass = findViewById(R.id.spinner_class);
        spinnerExam = findViewById(R.id.spinner_exam);
        recyclerView = findViewById(R.id.rv_class_scores);
        tvEmpty = findViewById(R.id.tv_empty_scores);
        btnBack = findViewById(R.id.btn_back);

        scoreList = new ArrayList<>();
        classList = new ArrayList<>();
        examMap = new HashMap<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassScoresAdapter(scoreList);
        recyclerView.setAdapter(adapter);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupListeners() {
        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (position > 0 && position <= classList.size()) {
                        selectedClassId = classList.get(position - 1).getClassId();
                        selectedExamId = ""; // Reset exam selection

                        Log.d("ClassScores", "Selected class: " + selectedClassId);

                        // Clear exam spinner and scores when changing class
                        List<String> defaultExams = new ArrayList<>();
                        defaultExams.add("-- Chọn đợt thi --");
                        ArrayAdapter<String> examAdapter = new ArrayAdapter<>(ClassScoresActivity.this,
                                android.R.layout.simple_spinner_item, defaultExams);
                        examAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerExam.setAdapter(examAdapter);

                        // Clear scores display
                        scoreList.clear();
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);

                        loadExamsForClass(selectedClassId);
                    }
                } catch (Exception e) {
                    Log.e("ClassScores", "Error in class selection", e);
                    Toast.makeText(ClassScoresActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerExam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (position > 0) {
                        String selectedValue = (String) parent.getItemAtPosition(position);
                        if (selectedValue != null && !selectedValue.isEmpty()) {
                            // Find the exam ID from exam name
                            for (Map.Entry<String, String> entry : examMap.entrySet()) {
                                if (entry.getValue().equals(selectedValue)) {
                                    selectedExamId = entry.getKey();
                                    break;
                                }
                            }
                            Log.d("ClassScores", "Selected exam: " + selectedExamId + " from class: " + selectedClassId);

                            if (!selectedClassId.isEmpty() && !selectedExamId.isEmpty()) {
                                loadScoresForClassAndExam(selectedClassId, selectedExamId);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("ClassScores", "Error in exam selection", e);
                    Toast.makeText(ClassScoresActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadClasses() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("classes")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    classList.clear();
                    List<String> classNames = new ArrayList<>();
                    classNames.add("-- Chọn lớp --");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ClassModel classModel = document.toObject(ClassModel.class);
                        classModel.setClassId(document.getId());
                        classList.add(classModel);
                        classNames.add(classModel.getClassName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ClassScoresActivity.this,
                            android.R.layout.simple_spinner_item, classNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerClass.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ClassScoresActivity.this, "Lỗi tải lớp: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadExamsForClass(String classId) {
        if (classId == null || classId.isEmpty()) {
            return;
        }

        Log.d("ClassScores", "Loading exams for class: " + classId);

        // Load list of exams sent to this class
        db.collection("classes").document(classId).collection("messages")
                .whereEqualTo("type", "exam")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    examMap.clear();
                    List<String> examNames = new ArrayList<>();
                    examNames.add("-- Chọn đợt thi --");

                    Log.d("ClassScores", "Found " + queryDocumentSnapshots.size() + " exams");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String examId = document.getString("examId");
                        String examName = document.getString("message");
                        if (examName != null && examName.startsWith("BỘ ĐỀ: ")) {
                            examName = examName.substring("BỘ ĐỀ: ".length());
                        }
                        if (examId != null && examName != null) {
                            examMap.put(examId, examName);
                            examNames.add(examName);
                            Log.d("ClassScores", "Added exam: " + examName + " (" + examId + ")");
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ClassScoresActivity.this,
                            android.R.layout.simple_spinner_item, examNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerExam.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("ClassScores", "Error loading exams", e);
                    Toast.makeText(ClassScoresActivity.this, "Lỗi tải đợt thi: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadScoresForClassAndExam(String classId, String examId) {
        if (classId == null || classId.isEmpty() || examId == null || examId.isEmpty()) {
            scoreList.clear();
            adapter.notifyDataSetChanged();
            tvEmpty.setText("Vui lòng chọn lớp và đợt thi");
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        Log.d("ClassScores", "Loading scores for class: " + classId + ", exam: " + examId);

        // Get all members of the class
        db.collection("classes").document(classId)
                .get()
                .addOnSuccessListener(classDoc -> {
                    if (classDoc.exists()) {
                        List<String> members = (List<String>) classDoc.get("member");
                        Log.d("ClassScores", "Class members: " + (members != null ? members.size() : 0));

                        if (members != null && !members.isEmpty()) {
                            // Load scores for each member
                            loadScoresForMembers(members, examId, classId);
                        } else {
                            scoreList.clear();
                            adapter.notifyDataSetChanged();
                            tvEmpty.setText("Lớp này chưa có học sinh");
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ClassScores", "Error loading class data", e);
                    Toast.makeText(ClassScoresActivity.this, "Lỗi tải thông tin lớp: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadScoresForMembers(List<String> members, String examId, String classId) {
        scoreList.clear();
        Map<String, StudentScoreData> scoreMap = new HashMap<>();

        // Initialize score map for all members
        for (String memberId : members) {
            StudentScoreData data = new StudentScoreData();
            data.setUserId(memberId);
            scoreMap.put(memberId, data);
        }

        Log.d("ClassScores", "Loading scores for " + members.size() + " members, examId: " + examId + ", classId: " + classId);

        // IMPORTANT: Create a set of member IDs for quick lookup
        // This ensures we ONLY get data from users who are members of this specific class
        db.collection("quiz_results")
                .whereEqualTo("examId", examId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        Log.d("ClassScores", "=== DEBUG INFO ===");
                        Log.d("ClassScores", "Searching for examId: '" + examId + "'");
                        Log.d("ClassScores", "Filtering by classId: '" + classId + "'");
                        Log.d("ClassScores", "Total quiz_results found: " + queryDocumentSnapshots.size());
                        Toast.makeText(ClassScoresActivity.this, "Tìm thấy " + queryDocumentSnapshots.size() + " kết quả", Toast.LENGTH_SHORT).show();

                        int acceptedCount = 0;
                        int skippedCount = 0;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                String userId = document.getString("userId");
                                String docClassId = document.getString("classId");
                                String docExamId = document.getString("examId");
                                Double score = document.getDouble("score");
                                
                                // Safe timestamp retrieval
                                Long timestamp = null;
                                try {
                                    timestamp = document.getLong("timestamp");
                                } catch (Exception e) {
                                    Log.d("ClassScores", "  timestamp is not a Long, trying alternative");
                                }
                                
                                Long correctAnswersLong = document.getLong("correctAnswers");
                                Long totalQuestionsLong = document.getLong("totalQuestions");
                                Integer correctAnswers = correctAnswersLong != null ? correctAnswersLong.intValue() : 0;
                                Integer totalQuestions = totalQuestionsLong != null ? totalQuestionsLong.intValue() : 0;

                                Log.d("ClassScores", "  Doc ID: " + document.getId() + 
                                        " | userId: " + userId + 
                                        " | examId: " + docExamId + 
                                        " | classId: " + docClassId + 
                                        " | score: " + score);

                                // CRITICAL FILTER:
                                // 1. User must be in current class members list
                                // 2. If classId is set, it MUST match current classId
                                // 3. If classId is not set, SKIP it (too risky with old data)
                                if (userId != null && scoreMap.containsKey(userId)) {
                                    // Only accept if classId matches OR is properly set
                                    // REJECT if classId is null/empty (old untracked data)
                                    if (docClassId != null && !docClassId.isEmpty() && docClassId.equals(classId)) {
                                        StudentScoreData data = scoreMap.get(userId);
                                        if (data != null) {
                                            data.setScore(score != null ? score : 0);
                                            data.setCorrectAnswers(correctAnswers != null ? correctAnswers : 0);
                                            data.setTotalQuestions(totalQuestions != null ? totalQuestions : 0);
                                            data.setTimestamp(timestamp);
                                            Log.d("ClassScores", "    ✓ ACCEPTED");
                                            acceptedCount++;
                                        }
                                    } else {
                                        Log.d("ClassScores", "    ✗ SKIPPED - classId mismatch. Expected: '" + classId + "', Got: '" + docClassId + "'");
                                        skippedCount++;
                                    }
                                } else {
                                    Log.d("ClassScores", "    ✗ SKIPPED - userId not in members or null");
                                    skippedCount++;
                                }
                            } catch (Exception e) {
                                Log.e("ClassScores", "Error processing document: " + document.getId(), e);
                            }
                        }

                        String debugMsg = "Kết quả: " + acceptedCount + " chấp nhận, " + skippedCount + " bỏ qua";
                        Log.d("ClassScores", debugMsg);
                        Toast.makeText(ClassScoresActivity.this, debugMsg, Toast.LENGTH_LONG).show();

                        // Load student names
                        loadStudentNames(scoreMap, classId);
                    } catch (Exception e) {
                        Log.e("ClassScores", "Error in onSuccessListener", e);
                        Toast.makeText(ClassScoresActivity.this, "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ClassScores", "Error loading scores from Firestore", e);
                    Toast.makeText(ClassScoresActivity.this, "Lỗi tải điểm: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // Load student names with empty map to show empty state
                    loadStudentNames(scoreMap, classId);
                });
    }

        private void loadStudentNames(Map<String, StudentScoreData> scoreMap, String classId) {
            // Load name for each student
            int[] count = {0};
            int total = scoreMap.size();

            // If no scores, update UI immediately
            if (total == 0) {
                updateScoreList(scoreMap);
                return;
            }
            List<String> idsToRemove = new ArrayList<>();
            for (String userId : scoreMap.keySet()) {
                db.collection("users").document(userId).get()
                        .addOnSuccessListener(userDoc -> {
                            if (userDoc.exists()) {
                                String role = userDoc.getString("role");
                                if ("teacher".equals(role)) {
                                    idsToRemove.add(userId);
                                } else {
                                    String fullName = userDoc.getString("fullName");
                                    String sId = userDoc.getString("studentId"); // Lấy mã số sinh viên

                                    // Hiển thị dạng: Nguyễn Văn A (SV001)
                                    String displayName = (fullName != null ? fullName : "N/A") +
                                            " (" + (sId != null ? sId : "N/A") + ")";

                                    scoreMap.get(userId).setStudentName(displayName);
                                }
                            }

                            count[0]++;

                            if (count[0] == total) {
                                // Sau khi đã kiểm tra xong tất cả các User
                                // Tiến hành xóa các giáo viên khỏi scoreMap trước khi update UI
                                for (String id : idsToRemove) {
                                    scoreMap.remove(id);
                                }
                                updateScoreList(scoreMap);
                            }
                        })
                        .addOnFailureListener(e -> {
                            count[0]++;
                            if (count[0] == total) {
                                for (String id : idsToRemove) {
                                    scoreMap.remove(id);
                                }
                                updateScoreList(scoreMap);
                            }
                        });
            }
        }

        private void updateScoreList (Map < String, StudentScoreData > scoreMap){
            scoreList.clear();
            scoreList.addAll(scoreMap.values());

            // Sort by score descending
            scoreList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

            adapter.notifyDataSetChanged();

            if (scoreList.isEmpty()) {
                tvEmpty.setText("Chưa có học sinh nào làm bài thi này");
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

