package com.example.studyspace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class TaoBoDe extends AppCompatActivity {

    private static final String TAG = "TaoBoDe";
    private static final String PREFS_NAME = "QuizPrefs";
    private static final String KEY_QUIZ_INFO_MAP = "QuizInfoMap";

    private QuestionViewModel questionViewModel;
    private ImageView addButton;
    private LinearLayout layoutQuizList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private final List<String> availableTopics = new ArrayList<>();
    private Map<String, QuizInfo> savedQuizInfoMap = new HashMap<>();

    public static class QuizInfo {
        public String topic;
        public int easyQuestions;
        public int mediumQuestions;
        public int hardQuestions;
        public String examId;

        public QuizInfo() {}

        public QuizInfo(String topic, int easyQuestions, int mediumQuestions, int hardQuestions, String examId) {
            this.topic = topic;
            this.easyQuestions = easyQuestions;
            this.mediumQuestions = mediumQuestions;
            this.hardQuestions = hardQuestions;
            this.examId = examId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tao_bo_de);

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
                    layoutQuizList.addView(createQuizItemLayout(quizName, info));
                }
            }
        }
    }

    private View createQuizItemLayout(String quizName, QuizInfo info) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(20, 20, 20, 20);
        itemLayout.setBackgroundResource(android.R.drawable.list_selector_background);
        itemLayout.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);

        TextView textView = new TextView(this);
        textView.setText("📄 " + quizName + "\n(" + info.topic + ")");
        textView.setTextSize(16);
        textView.setTextColor(Color.BLACK);
        textView.setLayoutParams(textParams);

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(TaoBoDe.this, QuizPreviewActivity.class);
            intent.putExtra("EXAM_ID", info.examId);
            startActivity(intent);
        });

        ImageView btnSend = new ImageView(this);
        btnSend.setImageResource(android.R.drawable.ic_menu_send);
        btnSend.setPadding(20, 20, 20, 20);
        btnSend.setColorFilter(Color.parseColor("#0084FF"));

        btnSend.setOnClickListener(v -> showClassSelectionDialog(quizName, info));

        ImageView btnDelete = new ImageView(this);
        btnDelete.setImageResource(android.R.drawable.ic_menu_delete);
        btnDelete.setPadding(20, 20, 20, 20);
        btnDelete.setColorFilter(Color.RED);
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(quizName));

        itemLayout.addView(textView);
        itemLayout.addView(btnSend);
        itemLayout.addView(btnDelete);

        return itemLayout;
    }

    private void showClassSelectionDialog(String quizName, QuizInfo info) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();

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

    private void sendQuizToClassFirestore(String classId, String className, String quizName, QuizInfo info) {
        String messageContent = "BỘ ĐỀ: " + quizName;

        ChatMessage chatMessage = new ChatMessage(
                mAuth.getCurrentUser().getUid(),
                messageContent,
                new Date(),
                "exam",
                info.examId,
                0, // Not applicable
                0  // Not applicable
        );
        chatMessage.setClassId(classId); // Set classId

        db.collection("classes").document(classId).collection("messages")
                .add(chatMessage)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã gửi bộ đề vào lớp " + className, Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gửi thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showCreateQuizPopup() {
        if (availableTopics.isEmpty()) {
            Toast.makeText(this, "Đang tải dữ liệu ngân hàng câu hỏi...", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_create_quiz, null);

        Spinner spinnerPopupTopic = popupView.findViewById(R.id.spinner_popup_topic);
        EditText editTextPopupQuizName = popupView.findViewById(R.id.edittext_popup_quiz_name);
        EditText easyQuestions = popupView.findViewById(R.id.edittext_easy_questions);
        EditText mediumQuestions = popupView.findViewById(R.id.edittext_medium_questions);
        EditText hardQuestions = popupView.findViewById(R.id.edittext_hard_questions);

        ArrayAdapter<String> topicAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableTopics);
        topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPopupTopic.setAdapter(topicAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Tạo bộ đề mới")
                .setView(popupView)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String quizName = editTextPopupQuizName.getText().toString().trim();
                    if (quizName.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên bộ đề.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String selectedTopic = spinnerPopupTopic.getSelectedItem().toString();

                    int easyCount = getIntValue(easyQuestions, 0);
                    int mediumCount = getIntValue(mediumQuestions, 0);
                    int hardCount = getIntValue(hardQuestions, 0);

                    executeCreateQuizSet(quizName, selectedTopic, easyCount, mediumCount, hardCount);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private int getIntValue(EditText editText, int defaultValue) {
        if (TextUtils.isEmpty(editText.getText())) return defaultValue;
        try {
            return Integer.parseInt(editText.getText().toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void executeCreateQuizSet(String quizName, String selectedTopic, int easyCount, int mediumCount, int hardCount) {
        if (easyCount == 0 && mediumCount == 0 && hardCount == 0) {
            Toast.makeText(this, "Vui lòng nhập số lượng câu hỏi.", Toast.LENGTH_SHORT).show();
            return;
        }

        Task<List<Question>> easyTask = questionViewModel.getQuizQuestionsByDifficulty(selectedTopic, 1, 1, easyCount);
        Task<List<Question>> mediumTask = questionViewModel.getQuizQuestionsByDifficulty(selectedTopic, 2, 3, mediumCount);
        Task<List<Question>> hardTask = questionViewModel.getQuizQuestionsByDifficulty(selectedTopic, 4, 5, hardCount);

        Tasks.whenAllSuccess(easyTask, mediumTask, hardTask).addOnSuccessListener(results -> {
            List<Question> examQuestions = new ArrayList<>();

            List<Question> easyQuestions = (List<Question>) results.get(0);
            List<Question> mediumQuestions = (List<Question>) results.get(1);
            List<Question> hardQuestions = (List<Question>) results.get(2);

            // Randomly shuffle questions within each difficulty level
            Collections.shuffle(easyQuestions);
            Collections.shuffle(mediumQuestions);
            Collections.shuffle(hardQuestions);

            examQuestions.addAll(easyQuestions);
            examQuestions.addAll(mediumQuestions);
            examQuestions.addAll(hardQuestions);

            if (examQuestions.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy câu hỏi nào phù hợp.", Toast.LENGTH_SHORT).show();
            } else {
                createExamInFirestore(quizName, selectedTopic, easyCount, mediumCount, hardCount, examQuestions);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching questions", e);
            Toast.makeText(this, "Lỗi khi tải câu hỏi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void createExamInFirestore(String quizName, String topic, int easyCount, int mediumCount, int hardCount, List<Question> questions) {
        CollectionReference examsRef = db.collection("Exam");
        String examId = examsRef.document().getId();

        Map<String, Object> examData = new HashMap<>();
        examData.put("name", quizName);
        examData.put("topic", topic);
        examData.put("createdAt", new Date());
        examData.put("createdBy", mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "unknown");
        examData.put("totalQuestions", questions.size());
        examData.put("easyQuestions", easyCount);
        examData.put("mediumQuestions", mediumCount);
        examData.put("hardQuestions", hardCount);

        examsRef.document(examId).set(examData)
                .addOnSuccessListener(aVoid -> {
                    CollectionReference questionsRef = examsRef.document(examId).collection("questions");
                    List<Task<Void>> questionTasks = new ArrayList<>();
                    
                    // Add questions with order and difficulty metadata
                    for (int i = 0; i < questions.size(); i++) {
                        Question question = questions.get(i);
                        Map<String, Object> questionData = new HashMap<>();
                        questionData.put("questionText", question.getQuestionText());
                        questionData.put("options", question.getOptions());
                        questionData.put("correctAnswerIndex", question.getCorrectAnswerIndex());
                        questionData.put("topic", question.getTopic());
                        questionData.put("level", question.getLevel());
                        questionData.put("questionNumber", i + 1); // Question order in exam
                        questionData.put("originalId", question.getId()); // Reference to original question
                        
                        questionTasks.add(questionsRef.document().set(questionData));
                    }

                    Tasks.whenAll(questionTasks).addOnSuccessListener(v -> {
                        QuizInfo newQuizInfo = new QuizInfo(topic, easyCount, mediumCount, hardCount, examId);
                        saveQuizInfoReference(quizName, newQuizInfo);
                        loadAndDisplaySavedQuizzes();

                        Toast.makeText(this, "Tạo bộ đề thành công!", Toast.LENGTH_SHORT).show();

                        new AlertDialog.Builder(this)
                                .setTitle("Tạo thành công!")
                                .setMessage("Bạn có muốn gửi bộ đề này vào lớp học ngay không?")
                                .setPositiveButton("Gửi ngay", (d, w) -> showClassSelectionDialog(quizName, newQuizInfo))
                                .setNegativeButton("Đóng", null)
                                .show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi lưu câu hỏi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tạo bộ đề: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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

    private void showDeleteConfirmationDialog(String quizName) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa bộ đề '" + quizName + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    QuizInfo quizInfo = savedQuizInfoMap.get(quizName);
                    if (quizInfo != null && quizInfo.examId != null) {
                        db.collection("Exam").document(quizInfo.examId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Map<String, QuizInfo> quizMap = getSavedQuizInfoMap();
                                    quizMap.remove(quizName);
                                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                                    Gson gson = new Gson();
                                    editor.putString(KEY_QUIZ_INFO_MAP, gson.toJson(quizMap));
                                    editor.apply();
                                    loadAndDisplaySavedQuizzes();
                                    Toast.makeText(this, "Đã xóa bộ đề", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        // Fallback for old data or if examId is null
                        Map<String, QuizInfo> quizMap = getSavedQuizInfoMap();
                        quizMap.remove(quizName);
                        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                        Gson gson = new Gson();
                        editor.putString(KEY_QUIZ_INFO_MAP, gson.toJson(quizMap));
                        editor.apply();
                        loadAndDisplaySavedQuizzes();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
