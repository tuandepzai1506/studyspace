package com.example.studyspace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.studyspace.models.Question;
import com.example.studyspace.viewmodels.QuestionViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
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

    // Danh sách chủ đề lấy từ Database
    private final List<String> availableTopics = new ArrayList<>();

    // Map lưu thông tin các bộ đề đã tạo
    private Map<String, QuizInfo> savedQuizInfoMap = new HashMap<>();

    // Class nội bộ để lưu thông tin bộ đề
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

        // Khởi tạo ViewModel
        questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);

        addButton = findViewById(R.id.add);
        layoutQuizList = findViewById(R.id.layout_criteria);

        if (addButton != null) {
            addButton.setOnClickListener(v -> showCreateQuizPopup());
        }

        // 1. Lắng nghe dữ liệu để lấy danh sách Chủ đề
        observeQuestionData();

        // 2. Tải danh sách bộ đề đã lưu từ trước
        loadAndDisplaySavedQuizzes();

        // 3. Cài đặt thanh điều hướng dưới đáy
        setupBottomNavigation();
    }

    private void observeQuestionData() {
        // Lấy toàn bộ câu hỏi để trích xuất ra các Chủ đề (Topic) đang có
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
                Log.d(TAG, "Đã cập nhật danh sách chủ đề: " + availableTopics.size());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        questionViewModel.startListening(); // Bắt đầu lắng nghe Firestore
    }

    @Override
    protected void onStop() {
        super.onStop();
        questionViewModel.stopListening(); // Dừng lắng nghe
    }

    // --- HIỂN THỊ DANH SÁCH BỘ ĐỀ ĐÃ TẠO ---
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
                    layoutQuizList.addView(createQuizTextView(quizName, info));
                }
            }
        }
    }

    private TextView createQuizTextView(String quizName, QuizInfo info) {
        TextView textView = new TextView(this);
        textView.setText("📄 " + quizName + " (" + info.topic + " - Level " + info.level + ")");
        textView.setTextSize(16);
        textView.setPadding(20, 24, 20, 24);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setBackgroundResource(android.R.drawable.list_selector_background); // Hiệu ứng bấm

        // Bấm ngắn: Xem trước (Mở Activity mới)
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuizPreviewActivity.class);
            intent.putExtra(QuizPreviewActivity.EXTRA_TOPIC, info.topic);
            intent.putExtra(QuizPreviewActivity.EXTRA_LEVEL, info.level);
            intent.putExtra(QuizPreviewActivity.EXTRA_LIMIT, info.limit);
            startActivity(intent);
        });

        // Bấm giữ: Xóa
        textView.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(quizName, info);
            return true;
        });
        return textView;
    }

    // --- POPUP TẠO BỘ ĐỀ MỚI ---
    private void showCreateQuizPopup() {
        if (availableTopics.isEmpty()) {
            Toast.makeText(this, "Đang tải dữ liệu ngân hàng câu hỏi, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_create_quiz, null);

        Spinner spinnerPopupTopic = popupView.findViewById(R.id.spinner_popup_topic);
        Spinner spinnerPopupLevel = popupView.findViewById(R.id.spinner_popup_level);
        EditText editTextPopupLimit = popupView.findViewById(R.id.edittext_popup_limit);
        EditText editTextPopupQuizName = popupView.findViewById(R.id.edittext_popup_quiz_name);

        // Setup Spinner Chủ đề
        ArrayAdapter<String> topicAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableTopics);
        topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPopupTopic.setAdapter(topicAdapter);

        // Setup Spinner Độ khó
        Integer[] levels = {1, 2, 3, 4, 5};
        ArrayAdapter<Integer> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPopupLevel.setAdapter(levelAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Tạo bộ đề mới")
                .setView(popupView)
                .setPositiveButton("Tạo & Xuất CSV", (dialog, which) -> {
                    String quizName = editTextPopupQuizName.getText().toString().trim();
                    if (quizName.isEmpty()) {
                        Toast.makeText(this, "Vui lòng đặt tên cho bộ đề!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (savedQuizInfoMap.containsKey(quizName)) {
                        Toast.makeText(this, "Tên này đã tồn tại, vui lòng chọn tên khác.", Toast.LENGTH_SHORT).show();
                        return;
                    }

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
        if (limitStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số lượng câu hỏi!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            questionLimit = Integer.parseInt(limitStr);
            if (questionLimit <= 0) {
                Toast.makeText(this, "Số lượng phải lớn hơn 0.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số lượng không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi ViewModel để lấy câu hỏi theo tiêu chí
        questionViewModel.getQuizQuestions(selectedTopic, selectedLevel, questionLimit)
                .observe(this, questions -> {
                    // Cần xóa observer ngay sau khi nhận dữ liệu để tránh gọi lại nhiều lần không cần thiết
                    // (Trong thực tế nên dùng SingleLiveEvent hoặc xử lý kỹ hơn, nhưng ở đây tạm chấp nhận)
                    if (questions != null && !questions.isEmpty()) {
                        exportQuestionsToCSV(questions, quizName, selectedTopic, selectedLevel, questionLimit);
                    } else {
                        Toast.makeText(this, "Không tìm thấy câu hỏi nào với tiêu chí này!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // --- XUẤT FILE CSV (QUAN TRỌNG: ĐÃ SỬA ĐỂ KHỚP VỚI MODEL QUESTION MỚI) ---
    private void exportQuestionsToCSV(List<Question> questions, String quizName, String topic, int level, int limit) {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File appDirectory = new File(directory, "StudySpace");

        if (!appDirectory.exists() && !appDirectory.mkdirs()) {
            Toast.makeText(this, "Lỗi: Không thể tạo thư mục lưu trữ.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = quizName.replaceAll("[^a-zA-Z0-9.-]", "_") + ".csv";
        File file = new File(appDirectory, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            // Header chuẩn của Quizizz
            writer.write("Question Text,Option 1,Option 2,Option 3,Option 4,Correct Answer,Time in seconds\n");

            for (Question q : questions) {
                List<String> opts = q.getOptions();

                // Lấy các đáp án, nếu không có thì để trống
                String op1 = opts.size() > 0 ? safeString(opts.get(0)) : "";
                String op2 = opts.size() > 1 ? safeString(opts.get(1)) : "";
                String op3 = opts.size() > 2 ? safeString(opts.get(2)) : "";
                String op4 = opts.size() > 3 ? safeString(opts.get(3)) : "";

                // Xác định nội dung đáp án đúng dựa vào Index
                String correctAnsStr = "";
                if (q.getCorrectAnswerIndex() >= 0 && q.getCorrectAnswerIndex() < opts.size()) {
                    correctAnsStr = safeString(opts.get(q.getCorrectAnswerIndex()));
                }

                // Format dòng CSV
                String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"30\"\n",
                        safeString(q.getQuestionText()), op1, op2, op3, op4, correctAnsStr);

                writer.write(line);
            }

            // Lưu thông tin bộ đề vào SharedPreferences
            QuizInfo newQuizInfo = new QuizInfo(topic, level, limit, file.getAbsolutePath());
            saveQuizInfoReference(quizName, newQuizInfo);

            // Cập nhật giao diện
            loadAndDisplaySavedQuizzes();

            showSuccessDialog(file);

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xuất file CSV", e);
            Toast.makeText(this, "Lỗi tạo file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // --- CÁC HÀM TIỆN ÍCH LƯU TRỮ ---

    private void saveQuizInfoReference(String quizName, QuizInfo info) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Map<String, QuizInfo> quizMap = getSavedQuizInfoMap();
        quizMap.put(quizName, info);

        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(quizMap);
        editor.putString(KEY_QUIZ_INFO_MAP, json);
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

    private String safeString(String value) {
        if (value == null) return "";
        // Thoát ký tự ngoặc kép trong CSV (double quotes)
        return value.replace("\"", "\"\"");
    }

    // --- CÁC HỘP THOẠI ---

    private void showDeleteConfirmationDialog(String quizName, QuizInfo info) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa bộ đề '" + quizName + "'?\nFile CSV cũng sẽ bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Xóa file vật lý
                    File file = new File(info.filePath);
                    if (file.exists()) {
                        file.delete();
                    }

                    // Xóa trong SharedPreferences
                    Map<String, QuizInfo> quizMap = getSavedQuizInfoMap();
                    quizMap.remove(quizName);

                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                    Gson gson = new Gson();
                    editor.putString(KEY_QUIZ_INFO_MAP, gson.toJson(quizMap));
                    editor.apply();

                    loadAndDisplaySavedQuizzes();
                    Toast.makeText(this, "Đã xóa bộ đề thành công.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showSuccessDialog(File file) {
        new AlertDialog.Builder(this)
                .setTitle("Tạo bộ đề thành công!")
                .setMessage("File đã được lưu tại:\n" + file.getAbsolutePath() + "\n\nBạn có muốn mở trang web Quizizz để import ngay không?")
                .setPositiveButton("Mở Quizizz", (d, w) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://quizizz.com/create/quiz-from-spreadsheet"));
                    startActivity(intent);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.action_create_quiz); // Đánh dấu tab hiện tại

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_home) {
                // Chuyển về trang chủ (MainActivity)
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.action_question_bank) {
                startActivity(new Intent(getApplicationContext(), Question_Bank.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.action_create_quiz) {
                return true;
            }
            return false;
        });
    }
}