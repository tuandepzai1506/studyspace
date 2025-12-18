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
import androidx.core.content.FileProvider;
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

        questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);

        addButton = findViewById(R.id.add);
        layoutQuizList = findViewById(R.id.layout_criteria);

        if (addButton != null) {
            addButton.setOnClickListener(v -> showCreateQuizPopup());
        }

        // Bắt đầu lắng nghe dữ liệu từ ViewModel
        observeQuestionData();
        loadAndDisplaySavedQuizzes();
        setupBottomNavigation();
    }

    // --- SỬA LẠI LOGIC TẢI DỮ LIỆU ---
    private void observeQuestionData() {
        questionViewModel.getAllQuestions().observe(this, questions -> {
            if (questions != null) {
                // Khi có dữ liệu câu hỏi mới, cập nhật lại danh sách chủ đề
                Set<String> topicsSet = new HashSet<>();
                for (Question q : questions) {
                    if (q.getTopic() != null && !q.getTopic().isEmpty()) {
                        topicsSet.add(q.getTopic());
                    }
                }
                availableTopics.clear();
                availableTopics.addAll(topicsSet);
                Log.d(TAG, "Dữ liệu chủ đề đã được cập nhật. Số lượng: " + availableTopics.size());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bắt đầu lắng nghe khi Activity được hiển thị
        questionViewModel.startListeningForQuestionChanges();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Dừng lắng nghe khi Activity không còn hiển thị
        questionViewModel.stopListeningForChanges();
    }

    // --- CÁC HÀM CÒN LẠI GIỮ NGUYÊN NHƯ PHIÊN BẢN HOÀN CHỈNH TRƯỚC ĐÓ ---
    // (loadAndDisplaySavedQuizzes, createQuizTextView, showCreateQuizPopup, v.v...)
    // Tôi sẽ viết lại các hàm này ở dưới để đảm bảo bạn có file hoàn chỉnh nhất.

    private void loadAndDisplaySavedQuizzes() {
        layoutQuizList.removeAllViews();
        savedQuizInfoMap = getSavedQuizInfoMap();
        if (savedQuizInfoMap.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Chưa có bộ đề nào được tạo.");
            emptyView.setGravity(Gravity.CENTER);
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
        textView.setText(quizName);
        textView.setTextSize(18);
        textView.setPadding(0, 24, 0, 24);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuizPreviewActivity.class);
            intent.putExtra(QuizPreviewActivity.EXTRA_TOPIC, info.topic);
            intent.putExtra(QuizPreviewActivity.EXTRA_LEVEL, info.level);
            intent.putExtra(QuizPreviewActivity.EXTRA_LIMIT, info.limit);
            startActivity(intent);
        });
        textView.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(quizName, info);
            return true;
        });
        return textView;
    }

    private void showCreateQuizPopup() {
        if (availableTopics.isEmpty()) {
            Toast.makeText(this, "Ngân hàng câu hỏi trống hoặc đang tải, vui lòng thử lại sau giây lát.", Toast.LENGTH_LONG).show();
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
                .setView(popupView)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String quizName = editTextPopupQuizName.getText().toString().trim();
                    if (quizName.isEmpty()) {
                        Toast.makeText(this, "Vui lòng đặt tên cho bộ đề!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (savedQuizInfoMap.containsKey(quizName)) {
                        Toast.makeText(this, "Tên bộ đề đã tồn tại, vui lòng chọn tên khác.", Toast.LENGTH_SHORT).show();
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
        if (limitStr.isEmpty()) { Toast.makeText(this, "Vui lòng nhập số lượng câu hỏi!", Toast.LENGTH_SHORT).show(); return; }
        try {
            questionLimit = Integer.parseInt(limitStr);
            if (questionLimit <= 0) { Toast.makeText(this, "Số lượng câu hỏi phải lớn hơn 0.", Toast.LENGTH_SHORT).show(); return; }
        } catch (NumberFormatException e) { Toast.makeText(this, "Số lượng câu hỏi không hợp lệ.", Toast.LENGTH_SHORT).show(); return; }
        questionViewModel.getQuizQuestions(selectedTopic, selectedLevel, questionLimit)
                .observe(this, questions -> {
                    if (questions != null && !questions.isEmpty()) {
                        exportQuestionsToCSV(questions, quizName, selectedTopic, selectedLevel, questionLimit);
                    } else {
                        Toast.makeText(this, "Không tìm thấy câu hỏi nào phù hợp.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void exportQuestionsToCSV(List<Question> questions, String quizName, String topic, int level, int limit) {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File appDirectory = new File(directory, "StudySpace");
        if (!appDirectory.exists() && !appDirectory.mkdirs()) { Toast.makeText(this, "Lỗi: Không thể tạo thư mục lưu trữ.", Toast.LENGTH_SHORT).show(); return; }
        String fileName = quizName.replaceAll("[^a-zA-Z0-9.-]", "_") + ".csv";
        File file = new File(appDirectory, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Question Text,Option 1,Option 2,Option 3,Option 4,Correct Answer,Time in seconds\n");
            for (Question q : questions) {
                String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"30\"\n", safeString(q.getQuestionText()), safeString(q.getOption1()), safeString(q.getOption2()), safeString(q.getOption3()), safeString(q.getOption4()), safeString(q.getCorrectAnswer()));
                writer.write(line);
            }
            QuizInfo newQuizInfo = new QuizInfo(topic, level, limit, file.getAbsolutePath());
            saveQuizInfoReference(quizName, newQuizInfo);
            loadAndDisplaySavedQuizzes();
            showSuccessDialog(file);
        } catch (Exception e) { Log.e(TAG, "Lỗi khi xuất file CSV", e); Toast.makeText(this, "Lỗi tạo file!", Toast.LENGTH_LONG).show(); }
    }

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

    private void showDeleteConfirmationDialog(String quizName, QuizInfo info) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bộ đề '" + quizName + "' không? File .csv tương ứng cũng sẽ bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    File file = new File(info.filePath);
                    if (!file.delete() && file.exists()) { Log.w(TAG, "Không thể xóa file: " + info.filePath); }
                    Map<String, QuizInfo> quizMap = getSavedQuizInfoMap();
                    quizMap.remove(quizName);
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(quizMap);
                    editor.putString(KEY_QUIZ_INFO_MAP, json);
                    editor.apply();
                    loadAndDisplaySavedQuizzes();
                    Toast.makeText(this, "Đã xóa bộ đề '" + quizName + "'.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.action_create_quiz);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_home) { return true; }
            else if (itemId == R.id.action_question_bank) { startActivity(new Intent(getApplicationContext(), Question_Bank.class)); overridePendingTransition(0, 0); return true; }
            else if (itemId == R.id.action_create_quiz) { return true; }
            return false;
        });
    }

    private String safeString(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private void showSuccessDialog(File file) {
        new AlertDialog.Builder(this)
                .setTitle("Tạo bộ đề thành công")
                .setMessage("File đã được lưu tại:\n" + file.getAbsolutePath())
                .setPositiveButton("Mở trang Quizizz", (d, w) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://quizizz.com/create/quiz-from-spreadsheet"));
                    startActivity(intent);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }
}
