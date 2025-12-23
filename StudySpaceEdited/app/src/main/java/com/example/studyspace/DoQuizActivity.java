package com.example.studyspace;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.studyspace.models.Question;
import com.example.studyspace.viewmodels.QuestionViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoQuizActivity extends AppCompatActivity {

    private TextView tvTitle, tvQuestionCount, tvQuestionContent;
    private RadioGroup radioGroupOptions;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    private Button btnNext;

    private QuestionViewModel questionViewModel;
    private List<Question> mListQuestions;
    private int currentQuestionIndex = 0;
    private int scoreCount = 0; // Số câu đúng

    // Biến lưu trữ thông tin để gửi lên Firebase
    private String examId;
    private String examName;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_quiz);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();

        // Nhận dữ liệu từ Intent
        examId = getIntent().getStringExtra("EXAM_ID");
        examName = getIntent().getStringExtra("EXAM_NAME");

        // Fallback: Nếu không có EXAM_NAME, tạo từ EXAM_ID
        if (examName == null || examName.isEmpty()) {
            examName = "Bộ đề #" + (examId != null ? examId.substring(0, Math.min(8, examId.length())) : "unknown");
        }

        // Loại bỏ prefix "BỘ ĐỀ: " nếu có
        if (examName.startsWith("BỘ ĐỀ: ")) {
            examName = examName.substring(7);
        }

        if (examId == null || examId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có ID bài thi.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle.setText(examName);

        // Lấy danh sách câu hỏi từ Firebase cho bài thi này
        questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);
        questionViewModel.getQuestionsForExam(examId).addOnSuccessListener(questions -> {
            if (questions != null && !questions.isEmpty()) {
                mListQuestions = questions;
                Collections.shuffle(mListQuestions); // Xáo trộn câu hỏi
                showQuestion(0);
            } else {
                Toast.makeText(this, "Không tải được câu hỏi hoặc bài thi này chưa có câu hỏi nào!", Toast.LENGTH_LONG).show();
                tvQuestionContent.setText("Lỗi: Không tìm thấy câu hỏi cho bài thi này.");
            }
        }).addOnFailureListener(e -> {
            Log.e("DoQuizActivity", "Error loading questions", e);
            Toast.makeText(this, "Lỗi khi tải câu hỏi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            tvQuestionContent.setText("Lỗi: " + e.getMessage());
        });
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_quiz_title);
        tvQuestionCount = findViewById(R.id.tv_question_count);
        tvQuestionContent = findViewById(R.id.tv_question_content);
        radioGroupOptions = findViewById(R.id.radio_group_options);
        rbOption1 = findViewById(R.id.rb_option_1);
        rbOption2 = findViewById(R.id.rb_option_2);
        rbOption3 = findViewById(R.id.rb_option_3);
        rbOption4 = findViewById(R.id.rb_option_4);
        btnNext = findViewById(R.id.btn_next_question);

        btnNext.setOnClickListener(v -> checkAnswerAndNext());
    }

    private void showQuestion(int index) {
        if (mListQuestions == null || index >= mListQuestions.size()) {
            finishQuiz();
            return;
        }

        Question q = mListQuestions.get(index);
        tvQuestionCount.setText("Câu: " + (index + 1) + "/" + mListQuestions.size());
        tvQuestionContent.setText(q.getQuestionText());

        radioGroupOptions.clearCheck();

        List<String> opts = q.getOptions();
        rbOption1.setText(opts.size() > 0 ? opts.get(0) : "");
        rbOption2.setText(opts.size() > 1 ? opts.get(1) : "");
        rbOption3.setText(opts.size() > 2 ? opts.get(2) : "");
        rbOption4.setText(opts.size() > 3 ? opts.get(3) : "");

        rbOption3.setVisibility(opts.size() > 2 ? View.VISIBLE : View.GONE);
        rbOption4.setVisibility(opts.size() > 3 ? View.VISIBLE : View.GONE);

        if (index == mListQuestions.size() - 1) {
            btnNext.setText("Nộp bài");
        } else {
            btnNext.setText("Câu tiếp theo");
        }
    }

    private void checkAnswerAndNext() {
        if (radioGroupOptions.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Vui lòng chọn đáp án!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedIndex = -1;
        int checkedId = radioGroupOptions.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_option_1) selectedIndex = 0;
        else if (checkedId == R.id.rb_option_2) selectedIndex = 1;
        else if (checkedId == R.id.rb_option_3) selectedIndex = 2;
        else if (checkedId == R.id.rb_option_4) selectedIndex = 3;

        Question currentQ = mListQuestions.get(currentQuestionIndex);
        if (selectedIndex == currentQ.getCorrectAnswerIndex()) {
            scoreCount++;
        }

        currentQuestionIndex++;
        showQuestion(currentQuestionIndex);
    }

    private void finishQuiz() {
        if (mListQuestions == null || mListQuestions.isEmpty()) {
            // Nếu không có câu hỏi nào thì không cần hiện dialog và lưu điểm
            finish();
            return;
        }

        int total = mListQuestions.size();
        float finalScore = ((float) scoreCount / total) * 10;
        String scoreFormatted = String.format("%.1f", finalScore);

        saveScoreToFirebase(finalScore, total, scoreCount);

        new AlertDialog.Builder(this)
                .setTitle("Kết quả bài thi")
                .setMessage("Số câu đúng: " + scoreCount + "/" + total + "\n\n" +
                        "ĐIỂM SỐ: " + scoreFormatted + "/10")
                .setPositiveButton("Hoàn thành", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void saveScoreToFirebase(float finalScore, int total, int correct) {
        if (mAuth.getCurrentUser() == null) return;

        Map<String, Object> result = new HashMap<>();
        result.put("userId", mAuth.getCurrentUser().getUid());
        result.put("examId", examId);
        result.put("examName", examName);
        result.put("score", Double.parseDouble(String.format("%.1f", finalScore).replace(",", ".")));
        result.put("totalQuestions", total);
        result.put("correctAnswers", correct);
        result.put("timestamp", new Date());

        db.collection("quiz_results")
                .add(result)
                .addOnSuccessListener(documentReference -> Log.d("DoQuiz", "Lưu điểm thành công!"))
                .addOnFailureListener(e -> {
                    Log.e("DoQuiz", "Lỗi lưu điểm: " + e.getMessage());
                    Toast.makeText(DoQuizActivity.this, "Lỗi lưu điểm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
