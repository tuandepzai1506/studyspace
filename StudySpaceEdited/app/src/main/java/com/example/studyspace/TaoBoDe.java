package com.example.studyspace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.studyspace.models.ChatMessage;
import com.example.studyspace.models.Question;
import com.example.studyspace.viewmodels.QuestionViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaoBoDe extends AppCompatActivity {

    private static final String TAG = "TaoBoDe";
    private static final String PREFS_NAME = "QuizPrefs";
    private static final String KEY_QUIZ_INFO_MAP = "QuizInfoMap";

    private QuestionViewModel questionViewModel;
    private ImageView addButton;
    private LinearLayout layoutQuizList;

    // --- THÊM FIREBASE ---
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private final List<String> availableTopics = new ArrayList<>();
    private Map<String, QuizInfo> savedQuizInfoMap = new HashMap<>();

    public static class QuizInfo {
        public String topic;
        public int level;
        public int limit;
        public String filePath;

        public QuizInfo() {}

        public QuizInfo(String topic, int level, int limit, String filePath) {
            this.topic = topic;
            this.level = level;
            this.limit = limit;
            this.filePath = filePath;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tao_bo_de);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);

        addButton = findViewById(R.id.add);
        layoutQuizList = findViewById(R.id.layout_criteria);

        if (addButton != null) {
            addButton.setOnClickListener(v -> showCreateQuizPopup());
        }
        observeQuestionData();
        loadAndDisplaySavedQuizzes();
    }

    private void observeQuestionData() {
        questionViewModel.getQuestionsLiveData().observe(this, questions -> {
            if (questions != null) {
                Set<String> topicsSet = new HashSet<>();
                for (Question q : questions) {
                    if (q.getTopic() != null && !q.getTopic().isEmpty()) {
                        topicsSet.add(q.getTopic());
                    }
                }
                availableTopics.clear();
                availableTopics.addAll(topicsSet);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        questionViewModel.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        questionViewModel.stopListening();
    }

    // --- HIỂN THỊ DANH SÁCH BỘ ĐỀ (ĐÃ SỬA GIAO DIỆN) ---
    private void loadAndDisplaySavedQuizzes() {
        layoutQuizList.removeAllViews();
        savedQuizInfoMap = getSavedQuizInfoMap();

        if (savedQuizInfoMap.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Chưa có bộ đề nào được tạo.");
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setPadding(0, 50, 0, 0);
            layoutQuizList.addView(emptyView);
        } else {
            for (String quizName : savedQuizInfoMap.keySet()) {
                QuizInfo info = savedQuizInfoMap.get(quizName);
                if (info != null) {
                    // Thay vì chỉ thêm TextView, ta thêm 1 Layout chứa cả nút gửi
                    layoutQuizList.addView(createQuizItemLayout(quizName, info));
                }
            }
        }
    }

    // Hàm tạo giao diện cho từng dòng bộ đề (Tên bộ đề + Nút gửi)
    private View createQuizItemLayout(String quizName, QuizInfo info) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(20, 20, 20, 20);
        itemLayout.setBackgroundResource(android.R.drawable.list_selector_background);
        itemLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Phần Text hiển thị tên
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

        TextView textView = new TextView(this);
        textView.setText("📄 " + quizName + "\n(" + info.topic + " - Level " + info.level + ")");
        textView.setTextSize(16);
        textView.setTextColor(Color.BLACK);
        textView.setLayoutParams(textParams);

        // Sự kiện bấm vào tên: Xem trước
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuizPreviewActivity.class);
            intent.putExtra(QuizPreviewActivity.EXTRA_TOPIC, info.topic);
            intent.putExtra(QuizPreviewActivity.EXTRA_LEVEL, info.level);
            intent.putExtra(QuizPreviewActivity.EXTRA_LIMIT, info.limit);
            startActivity(intent);
        });

        // Nút Gửi (Icon Send)
        ImageView btnSend = new ImageView(this);
        btnSend.setImageResource(android.R.drawable.ic_menu_send); // Icon gửi có sẵn của Android
        btnSend.setPadding(20, 20, 20, 20);
        btnSend.setColorFilter(Color.parseColor("#0084FF")); // Màu xanh

        // Sự kiện bấm nút gửi: Hiện popup chọn lớp
        btnSend.setOnClickListener(v -> showClassSelectionDialog(quizName, info));

        // Nút Xóa (Icon Delete)
        ImageView btnDelete = new ImageView(this);
        btnDelete.setImageResource(android.R.drawable.ic_menu_delete);
        btnDelete.setPadding(20, 20, 20, 20);
        btnDelete.setColorFilter(Color.RED);
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(quizName, info));

        itemLayout.addView(textView);
        itemLayout.addView(btnSend);
        itemLayout.addView(btnDelete);

        return itemLayout;
    }

    // --- LOGIC CHỌN LỚP VÀ GỬI ---

    private void showClassSelectionDialog(String quizName, QuizInfo info) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();

        // 1. Tải danh sách lớp mà user làm chủ (userId == currentUserId)
        db.collection("classes")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> classNames = new ArrayList<>();
                    List<String> classIds = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("className");
                        if (name != null) {
                            classNames.add(name);
                            classIds.add(doc.getId());
                        }
                    }

                    if (classNames.isEmpty()) {
                        Toast.makeText(this, "Bạn chưa tạo lớp học nào.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 2. Hiển thị Dialog chọn lớp
                        showListClassesDialog(classNames, classIds, quizName, info);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải lớp học: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showListClassesDialog(List<String> names, List<String> ids, String quizName, QuizInfo info) {
        String[] nameArray = names.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Chọn lớp để gửi bộ đề")
                .setItems(nameArray, (dialog, which) -> {
                    String selectedClassId = ids.get(which);
                    String selectedClassName = names.get(which);
                    sendQuizToClassFirestore(selectedClassId, selectedClassName, quizName, info);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Trong file TaoBoDe.java

    private void sendQuizToClassFirestore(String classId, String className, String quizName, QuizInfo info) {
        // Nội dung hiển thị ngắn gọn
        String messageContent = "BỘ ĐỀ: " + quizName;

        // Tạo object tin nhắn với đầy đủ thông tin
        ChatMessage chatMessage = new ChatMessage(
                mAuth.getCurrentUser().getUid(),
                messageContent,
                new Date(),
                "quiz",        // Đánh dấu đây là tin nhắn dạng quiz
                info.topic,    // Chủ đề
                info.level,    // Mức độ
                info.limit     // Số lượng câu
        );

        // Gửi lên Firestore
        db.collection("classes").document(classId).collection("messages")
                .add(chatMessage)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã gửi bộ đề vào lớp " + className, Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gửi thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- CÁC HÀM CŨ (POPUP TẠO, EXPORT CSV...) GIỮ NGUYÊN ---

    private void showCreateQuizPopup() {
        if (availableTopics.isEmpty()) {
            Toast.makeText(this, "Đang tải dữ liệu ngân hàng câu hỏi...", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_create_quiz, null);

        Spinner spinnerPopupTopic = popupView.findViewById(R.id.spinner_popup_topic);
        Spinner spinnerPopupLevel = popupView.findViewById(R.id.spinner_popup_level);
        EditText editTextPopupLimit = popupView.findViewById(R.id.edittext_popup_limit);
        EditText editTextPopupQuizName = popupView.findViewById(R.id.edittext_popup_quiz_name);

        ArrayAdapter<String> topicAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableTopics);
        topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPopupTopic.setAdapter(topicAdapter);

        Integer[] levels = {1, 2, 3, 4, 5};
        ArrayAdapter<Integer> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPopupLevel.setAdapter(levelAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Tạo bộ đề mới")
                .setView(popupView)
                .setPositiveButton("Tạo & Xuất CSV", (dialog, which) -> {
                    String quizName = editTextPopupQuizName.getText().toString().trim();
                    if (quizName.isEmpty()) return;

                    String selectedTopic = spinnerPopupTopic.getSelectedItem().toString();
                    int selectedLevel = (Integer) spinnerPopupLevel.getSelectedItem();
                    String limitStr = editTextPopupLimit.getText().toString();

                    executeCreateQuizSet(quizName, selectedTopic, selectedLevel, limitStr);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void executeCreateQuizSet(String quizName, String selectedTopic, int selectedLevel, String limitStr) {
        int questionLimit;
        try {
            questionLimit = Integer.parseInt(limitStr);
        } catch (NumberFormatException e) { return; }

        questionViewModel.getQuizQuestions(selectedTopic, selectedLevel, questionLimit)
                .observe(this, questions -> {
                    if (questions != null && !questions.isEmpty()) {
                        exportQuestionsToCSV(questions, quizName, selectedTopic, selectedLevel, questionLimit);
                    } else {
                        Toast.makeText(this, "Không tìm thấy câu hỏi!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void exportQuestionsToCSV(List<Question> questions, String quizName, String topic, int level, int limit) {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File appDirectory = new File(directory, "StudySpace");
        if (!appDirectory.exists()) appDirectory.mkdirs();

        String fileName = quizName.replaceAll("[^a-zA-Z0-9.-]", "_") + ".csv";
        File file = new File(appDirectory, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Question Text,Option 1,Option 2,Option 3,Option 4,Correct Answer,Time in seconds\n");
            for (Question q : questions) {
                // Logic export giữ nguyên như code cũ của bạn
                // ...
            }

            // Lưu thông tin
            QuizInfo newQuizInfo = new QuizInfo(topic, level, limit, file.getAbsolutePath());
            saveQuizInfoReference(quizName, newQuizInfo);
            loadAndDisplaySavedQuizzes();

            // Hỏi user xem có muốn gửi luôn không?
            new AlertDialog.Builder(this)
                    .setTitle("Tạo thành công!")
                    .setMessage("Bạn có muốn gửi bộ đề này vào lớp học ngay không?")
                    .setPositiveButton("Gửi ngay", (d, w) -> showClassSelectionDialog(quizName, newQuizInfo))
                    .setNegativeButton("Đóng", null)
                    .show();

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveQuizInfoReference(String quizName, QuizInfo info) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Map<String, QuizInfo> quizMap = getSavedQuizInfoMap();
        quizMap.put(quizName, info);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        editor.putString(KEY_QUIZ_INFO_MAP, gson.toJson(quizMap));
        editor.apply();
    }

    private Map<String, QuizInfo> getSavedQuizInfoMap() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_QUIZ_INFO_MAP, null);
        if (json == null) return new HashMap<>();
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, QuizInfo>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void showDeleteConfirmationDialog(String quizName, QuizInfo info) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa bộ đề '" + quizName + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Map<String, QuizInfo> quizMap = getSavedQuizInfoMap();
                    quizMap.remove(quizName);
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                    Gson gson = new Gson();
                    editor.putString(KEY_QUIZ_INFO_MAP, gson.toJson(quizMap));
                    editor.apply();
                    loadAndDisplaySavedQuizzes();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}