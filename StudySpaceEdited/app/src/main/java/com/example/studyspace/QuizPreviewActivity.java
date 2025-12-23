package com.example.studyspace;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyspace.adapters.QuizPreviewAdapter;
import com.example.studyspace.viewmodels.QuestionViewModel;

public class QuizPreviewActivity extends AppCompatActivity {

    private static final String TAG = "QuizPreviewActivity";
    public static final String EXTRA_TOPIC = "EXTRA_TOPIC";
    public static final String EXTRA_LEVEL = "EXTRA_LEVEL";
    public static final String EXTRA_LIMIT = "EXTRA_LIMIT";
    public static final String EXTRA_EXAM_ID = "EXAM_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_preview);

        Toolbar toolbar = findViewById(R.id.toolbar_preview);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recycler_view_preview_questions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Lấy examId từ intent
        String examId = getIntent().getStringExtra(EXTRA_EXAM_ID);

        if (examId != null && !examId.isEmpty()) {
            // Load questions from Exam collection
            QuestionViewModel questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);
            questionViewModel.getQuestionsForExam(examId).addOnSuccessListener(questions -> {
                if (questions != null && !questions.isEmpty()) {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Chi tiết: " + questions.get(0).getTopic());
                    }
                    QuizPreviewAdapter adapter = new QuizPreviewAdapter(questions);
                    recyclerView.setAdapter(adapter);
                } else {
                    Log.w(TAG, "No questions found for exam: " + examId);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error loading exam questions", e);
            });
        } else {
            // Fallback: Try loading by topic, level, limit
            String topic = getIntent().getStringExtra(EXTRA_TOPIC);
            int level = getIntent().getIntExtra(EXTRA_LEVEL, 1);
            int limit = getIntent().getIntExtra(EXTRA_LIMIT, 10);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Chi tiết: " + topic);
            }

            QuestionViewModel questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);
            questionViewModel.getQuizQuestionsByDifficulty(topic, level, level, limit).addOnSuccessListener(questions -> {
                if (questions != null && !questions.isEmpty()) {
                    QuizPreviewAdapter adapter = new QuizPreviewAdapter(questions);
                    recyclerView.setAdapter(adapter);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error loading questions by difficulty", e);
            });
        }
    }
}
    