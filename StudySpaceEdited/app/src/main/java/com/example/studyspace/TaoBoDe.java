package com.example.studyspace;

// 1. ĐẢM BẢO CÓ ĐẦY ĐỦ CÁC IMPORT ĐÚNG
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
    // Sử dụng KEY cho Map thay vì List
    private static final String KEY_QUIZ_INFO_MAP = "QuizInfoMap";

<<<<<<< HEAD
    // Khai báo các thành phần UI
    private Spinner spinnerTopic;
    private Spinner spinnerLevel;
    private EditText editTextLimit;
    private Button buttonCreateQuiz;
    private EditText fileName;
=======
    private QuestionViewModel questionViewModel;
    private ImageView addButton;
    private LinearLayout layoutQuizList;
    private final List<String> availableTopics = new ArrayList<>();
    // Sử dụng Map để lưu thông tin chi tiết (tên tùy chỉnh, topic, level, limit, đường dẫn file)
    private Map<String, QuizInfo> savedQuizInfoMap = new HashMap<>();

    // Class nội bộ để lưu thông tin chi tiết của một bộ đề
    public static class QuizInfo {
        public String topic;
        public int level;
        public int limit;
        public String filePath;

        public QuizInfo() {} // Constructor rỗng cho Gson

        public QuizInfo(String topic, int level, int limit, String filePath) {
            this.topic = topic;
            this.level = level;
            this.limit = limit;
            this.filePath = filePath;
        }
    }
>>>>>>> 98c14fc0cefc3b4f2b783dfaa33c6fe29cc7b832

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tao_bo_de);

        questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);

        // Ánh xạ các View từ layout
        addButton = findViewById(R.id.add);
        layoutQuizList = findViewById(R.id.layout_criteria);

        if (addButton != null) {
            addButton.setOnClickListener(v -> showCreateQuizPopup());
        }

        preloadTopics();
        loadAndDisplaySavedQuizzes();
        setupBottomNavigation();
    }

    /**
     * Tải danh sách các bộ đề đã lưu và hiển thị lên giao diện.
     */
    private void loadAndDisplaySavedQuizzes() {
        layoutQuizList.removeAllViews();
        savedQuizInfoMap = getSavedQuizInfoMap(); // Lấy dữ liệu từ Map

        if (savedQuizInfoMap.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("Chưa có bộ đề nào được tạo.");
            emptyView.setGravity(Gravity.CENTER);
            layoutQuizList.addView(emptyView);
        } else {
<<<<<<< HEAD
            // Kiểm tra
            Log.e("TaoBoDe", "Không tìm thấy nút Add!");
        }

        try {
            fileName = findViewById(R.id.fileName);
            spinnerTopic = findViewById(R.id.spinner_topic);
            spinnerLevel = findViewById(R.id.spinner_level);
            editTextLimit = findViewById(R.id.edittext_limit);
            buttonCreateQuiz = findViewById(R.id.button_create_file);
            // Sự kiện nút Tạo Đề
            if (buttonCreateQuiz != null) {
                buttonCreateQuiz.setOnClickListener(v -> taoBoDe());
=======
            // Duyệt qua Map để hiển thị tên bộ đề (key)
            for (String quizName : savedQuizInfoMap.keySet()) {
                QuizInfo info = savedQuizInfoMap.get(quizName);
                if (info != null) {
                    TextView fileView = createQuizTextView(quizName, info);
                    layoutQuizList.addView(fileView);
                }
>>>>>>> 98c14fc0cefc3b4f2b783dfaa33c6fe29cc7b832
            }
        }
    }

    /**
     * Tạo một TextView đại diện cho một bộ đề.
     * Khi nhấn vào, sẽ mở màn hình xem trước.
     */
    private TextView createQuizTextView(String quizName, QuizInfo info) {
        TextView textView = new TextView(this);
        textView.setText(quizName); // Hiển thị tên bộ đề tùy chỉnh
        textView.setTextSize(18);
        textView.setPadding(0, 24, 0, 24);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTextColor(getResources().getColor(android.R.color.black));

        // Khi nhấn vào, mở màn hình xem trước (QuizPreviewActivity)
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuizPreviewActivity.class);
            // Truyền các tham số cần thiết để tải lại câu hỏi
            intent.putExtra(QuizPreviewActivity.EXTRA_TOPIC, info.topic);
            intent.putExtra(QuizPreviewActivity.EXTRA_LEVEL, info.level);
            intent.putExtra(QuizPreviewActivity.EXTRA_LIMIT, info.limit);
            startActivity(intent);
        });

        // Thêm sự kiện nhấn giữ để xóa
        textView.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(quizName, info);
            return true; // Đã xử lý sự kiện
        });

        return textView;
    }

    /**
     * Hiển thị popup để người dùng nhập thông tin tạo bộ đề.
     */
    private void showCreateQuizPopup() {
        if (availableTopics.isEmpty()) {
            Toast.makeText(this, "Ngân hàng câu hỏi trống hoặc chưa có chủ đề nào!", Toast.LENGTH_LONG).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_create_quiz, null);

        // Ánh xạ các View bên trong popup
        Spinner spinnerPopupTopic = popupView.findViewById(R.id.spinner_popup_topic);
        Spinner spinnerPopupLevel = popupView.findViewById(R.id.spinner_popup_level);
        EditText editTextPopupLimit = popupView.findViewById(R.id.edittext_popup_limit);
        EditText editTextPopupQuizName = popupView.findViewById(R.id.edittext_popup_quiz_name);

        // Điền dữ liệu vào các Spinner
        ArrayAdapter<String> topicAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableTopics);
        topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPopupTopic.setAdapter(topicAdapter);

        Integer[] levels = {1, 2, 3, 4, 5};
        ArrayAdapter<Integer> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPopupLevel.setAdapter(levelAdapter);

        // Xây dựng và hiển thị Dialog
        new AlertDialog.Builder(this)
                .setView(popupView)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    // Lấy tên bộ đề từ EditText
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

                    // Truyền tên bộ đề vào hàm thực thi
                    executeCreateQuizSet(quizName, selectedTopic, selectedLevel, limitStr);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Thực thi việc lấy câu hỏi và xuất ra file.
     */
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

    /**
     * Xuất câu hỏi ra file CSV và lưu thông tin bộ đề.
     */
    private void exportQuestionsToCSV(List<Question> questions, String quizName, String topic, int level, int limit) {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File appDirectory = new File(directory, "StudySpace");
        if (!appDirectory.exists() && !appDirectory.mkdirs()) {
            Toast.makeText(this, "Lỗi: Không thể tạo thư mục lưu trữ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sử dụng tên tùy chỉnh để tạo tên file
        String fileName = quizName.replaceAll("[^a-zA-Z0-9.-]", "_") + ".csv";
        File file = new File(appDirectory, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Question Text,Option 1,Option 2,Option 3,Option 4,Correct Answer,Time in seconds\n");
            for (Question q : questions) {
                String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"30\"\n", safeString(q.getQuestionText()), safeString(q.getOption1()), safeString(q.getOption2()), safeString(q.getOption3()), safeString(q.getOption4()), safeString(q.getCorrectAnswer()));
                writer.write(line);
            }

            // Tạo đối tượng QuizInfo và lưu lại
            QuizInfo newQuizInfo = new QuizInfo(topic, level, limit, file.getAbsolutePath());
            saveQuizInfoReference(quizName, newQuizInfo);

            loadAndDisplaySavedQuizzes(); // Cập nhật lại UI ngay lập tức
            showSuccessDialog(file); // Hiển thị thông báo thành công

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xuất file CSV", e);
            Toast.makeText(this, "Lỗi tạo file!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Lưu thông tin bộ đề vào SharedPreferences dưới dạng Map.
     */
    private void saveQuizInfoReference(String quizName, QuizInfo info) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Map<String, QuizInfo> quizMap = getSavedQuizInfoMap();
        quizMap.put(quizName, info);

        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(quizMap);
        editor.putString(KEY_QUIZ_INFO_MAP, json);
        editor.apply();
        Log.d(TAG, "Đã lưu thông tin cho bộ đề: " + quizName);
    }

    /**
     * Lấy Map thông tin các bộ đề từ SharedPreferences.
     */
    private Map<String, QuizInfo> getSavedQuizInfoMap() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_QUIZ_INFO_MAP, null);
        if (json == null) {
            return new HashMap<>();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, QuizInfo>>() {}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Hiển thị hộp thoại xác nhận xóa.
     */
    private void showDeleteConfirmationDialog(String quizName, QuizInfo info) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bộ đề '" + quizName + "' không? File .csv tương ứng cũng sẽ bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Xóa file vật lý
                    File file = new File(info.filePath);
                    boolean deleted = file.delete();
                    if (!deleted) {
                        Log.w(TAG, "Không thể xóa file: " + info.filePath);
                    }

                    // Xóa tham chiếu khỏi SharedPreferences
                    Map<String, QuizInfo> quizMap = getSavedQuizInfoMap();
                    quizMap.remove(quizName);

                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(quizMap);
                    editor.putString(KEY_QUIZ_INFO_MAP, json);
                    editor.apply();

                    loadAndDisplaySavedQuizzes(); // Cập nhật lại UI
                    Toast.makeText(this, "Đã xóa bộ đề '" + quizName + "'.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- CÁC HÀM CŨ KHÔNG THAY ĐỔI ---
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.action_create_quiz);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_home) { return true; }
            else if (itemId == R.id.action_question_bank) {
                startActivity(new Intent(getApplicationContext(), Question_Bank.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.action_create_quiz) { return true; }
            return false;
        });
    }

    private void preloadTopics() {
        questionViewModel.getAllQuestions().observe(this, questions -> {
            if (questions != null && !questions.isEmpty()) {
                Set<String> topicsSet = new HashSet<>();
                for (Question q : questions) {
                    if (q.getTopic() != null && !q.getTopic().isEmpty()) { topicsSet.add(q.getTopic()); }
                }
                availableTopics.clear();
                availableTopics.addAll(topicsSet);
                Log.d(TAG, "Đã tải " + availableTopics.size() + " chủ đề.");
            }
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
