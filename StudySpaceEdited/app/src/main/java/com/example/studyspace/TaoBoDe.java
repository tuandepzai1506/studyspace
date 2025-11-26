package com.example.studyspace;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.studyspace.models.Question; // KIỂM TRA LẠI PACKAGE NÀY
import com.example.studyspace.viewmodels.QuestionViewModel; // KIỂM TRA LẠI PACKAGE NÀY

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TaoBoDe extends AppCompatActivity {

    private QuestionViewModel questionViewModel;

    // Khai báo các thành phần UI
    private Spinner spinnerTopic;
    private Spinner spinnerLevel;
    private EditText editTextLimit; // Đổi lại thành EditText vì đây là nơi nhập số
    private Button buttonCreateQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tao_bo_de);


        questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);


        try {

            spinnerTopic = findViewById(R.id.spinner_topic);
            spinnerLevel = findViewById(R.id.spinner_level);

            editTextLimit = findViewById(R.id.edittext_limit);
            buttonCreateQuiz = findViewById(R.id.button_create_quiz);

            setupSpinners();

            if (buttonCreateQuiz != null) {
                buttonCreateQuiz.setOnClickListener(v -> taoBoDe());
            } else {
                Toast.makeText(this, "Lỗi UI: Nút Tạo Đề bị thiếu.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e("TaoBoDe", "Lỗi tìm kiếm View ID. Hãy kiểm tra tao_bo_de.xml", e);
            Toast.makeText(this, "Lỗi UI: Thiếu hoặc sai ID trong layout.", Toast.LENGTH_LONG).show();
        }
    }
    private void setupSpinners() {
        // 1. Adapter cho Chủ đề (spinnerTopic)
        String[] topics = new String[]{"Toán", "Vật Lý", "Hóa Học", "Sinh Học", "Lịch Sử"};
        ArrayAdapter<String> topicAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                topics
        );
        topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTopic.setAdapter(topicAdapter);

        // 2. Adapter cho Độ khó (spinnerLevel)
        String[] levels = new String[]{"1", "2", "3", "4", "5"};
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                levels
        );
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);
    }
    /**
     * Logic lấy dữ liệu từ ViewModel và Xuất file CSV
     */
    private void taoBoDe() {
        String selectedTopic;
        int selectedLevel;
        int questionLimit;

        try {
            // Lấy giá trị đầu vào an toàn
            selectedTopic = spinnerTopic.getSelectedItem().toString();
            selectedLevel = Integer.parseInt(spinnerLevel.getSelectedItem().toString());
            questionLimit = Integer.parseInt(editTextLimit.getText().toString());

            if (questionLimit <= 0) throw new NumberFormatException("Limit must be positive.");

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đầu vào: Vui lòng kiểm tra và nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Gọi ViewModel ---
        final Observer<List<Question>> quizObserver = new Observer<List<Question>>() {
            @Override
            public void onChanged(List<Question> questions) {
                questionViewModel.getQuizQuestions(selectedTopic, selectedLevel, questionLimit).removeObserver(this);

                if (questions != null && !questions.isEmpty()) {
                    exportQuestionsToCSV(questions);
                } else {
                    Toast.makeText(TaoBoDe.this, "Không tìm thấy câu hỏi nào phù hợp.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        questionViewModel.getQuizQuestions(selectedTopic, selectedLevel, questionLimit)
                .observe(this, quizObserver);
    }


    /**
     * Ghi List<Question> ra file CSV theo định dạng cho Quizizz.
     */
    private void exportQuestionsToCSV(List<Question> questions) {
        File directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (directory == null) {
            Toast.makeText(this, "Không thể truy cập bộ nhớ ngoài.", Toast.LENGTH_LONG).show();
            return;
        }

        String fileName = "Quizizz_Export_" + System.currentTimeMillis() + ".csv";
        File file = new File(directory, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Question Text,Option 1,Option 2,Option 3,Option 4,Correct Answer\n");

            for (Question q : questions) {
                // SỬ DỤNG PHƯƠNG THỨC safeString ĐỂ TRÁNH LỖI NULL
                String content = safeString(q.getContent());
                String answer = safeString(q.getCorrectAnswer());

                // >> LỖI Cannot resolve method 'getOptionX' <<
                // ĐÃ SỬA: Cần đảm bảo Question Model có các phương thức này
                String option1 = safeString(q.getOption1());
                String option2 = safeString(q.getOption2());
                String option3 = safeString(q.getOption3());
                String option4 = safeString(q.getOption4());

                // Escape các ký tự đặc biệt
                option1 = option1.replace("\"", "\"\"");
                option2 = option2.replace("\"", "\"\"");
                option3 = option3.replace("\"", "\"\"");
                option4 = option4.replace("\"", "\"\"");

                String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        content.replace("\"", "\"\""),
                        option1, option2, option3, option4,
                        answer.replace("\"", "\"\""));
                writer.write(line);
            }

            Toast.makeText(this, "✅ Xuất file thành công: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            openQuizizzUploadPage(file.getAbsolutePath());

        } catch (Exception e) {
            Log.e("TaoBoDe", "Lỗi xuất file", e);
            Toast.makeText(this, "❌ Lỗi xuất file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Phương thức tiện ích để tránh NullPointerException khi truy cập các trường của Model.
     */
    private String safeString(String value) {
        return value != null ? value : "";
    }

    /**
     * Mở trình duyệt đến trang Upload Quizizz.
     */
    private void openQuizizzUploadPage(String filePath) {
        String quizizzUploadUrl = "https://quizizz.com/create-quiz/questions";

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(quizizzUploadUrl));

        if (browserIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(browserIntent);
            Toast.makeText(this,
                    "➡️ TẢI LÊN file: " + filePath + " trên trang Quizizz vừa mở.",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Không tìm thấy ứng dụng trình duyệt.", Toast.LENGTH_LONG).show();
        }
    }
}