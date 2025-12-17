package com.example.studyspace;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyspace.adapters.QuizPreviewAdapter;
import com.example.studyspace.viewmodels.QuestionViewModel;

public class QuizPreviewActivity extends AppCompatActivity {

    public static final String EXTRA_TOPIC = "EXTRA_TOPIC";
    public static final String EXTRA_LEVEL = "EXTRA_LEVEL";
    public static final String EXTRA_LIMIT = "EXTRA_LIMIT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_preview);

        Toolbar toolbar = findViewById(R.id.toolbar_preview);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Lấy các tham số đã gửi từ TaoBoDe
        String topic = getIntent().getStringExtra(EXTRA_TOPIC);
        int level = getIntent().getIntExtra(EXTRA_LEVEL, 1);
        int limit = getIntent().getIntExtra(EXTRA_LIMIT, 10);

        getSupportActionBar().setTitle("Chi tiết: " + topic);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_preview_questions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Lấy câu hỏi từ ViewModel và hiển thị
        QuestionViewModel questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);
        questionViewModel.getQuizQuestions(topic, level, limit).observe(this, questions -> {
            if (questions != null) {
                QuizPreviewAdapter adapter = new QuizPreviewAdapter(questions);
                recyclerView.setAdapter(adapter);
            }
        });
    }
}
    