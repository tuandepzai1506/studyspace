package com.example.studyspace;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.studyspace.models.Question;
import com.example.studyspace.viewmodels.QuestionViewModel;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class TaoBoDe extends AppCompatActivity {

    private QuestionViewModel questionViewModel;

    // Khai báo các thành phần UI
    private Spinner spinnerTopic;
    private Spinner spinnerLevel;
    private EditText editTextLimit;
    private Button buttonCreateQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tao_bo_de);

        questionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);
        ImageView add = findViewById(R.id.add);
        if (add != null) {
            add.setOnClickListener(v -> showAddPopup());
        } else {
            // Kiểm tra
            Log.e("TaoBoDe", "Không tìm thấy nút Add!");
        }

        try {
            spinnerTopic = findViewById(R.id.spinner_topic);
            spinnerLevel = findViewById(R.id.spinner_level);
            editTextLimit = findViewById(R.id.edittext_limit);
            buttonCreateQuiz = findViewById(R.id.button_create_file);
            // Sự kiện nút Tạo Đề
            if (buttonCreateQuiz != null) {
                buttonCreateQuiz.setOnClickListener(v -> taoBoDe());
            }
        } catch (Exception e) {
            Log.e("TaoBoDe", "Lỗi khởi tạo UI", e);
            Toast.makeText(this, "Lỗi UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Hàm hiển thị Popup khi bấm nút Add
     */
    private void showAddPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TaoBoDe.this);

        // Nạp giao diện popup
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_makefile, null);
        builder.setView(popupView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Ánh xạ View trong Popup
        Spinner spnSource = popupView.findViewById(R.id.spinner_topic);
        Spinner spnLevel = popupView.findViewById(R.id.spinner_level);
        Button btnConfirm = popupView.findViewById(R.id.button_create_file);

        // Đổ dữ liệu vào Spinner trong Popup
        String[] sources = {"Lấy ngẫu nhiên", "Lấy chỉ định"};
        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sources);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSource.setAdapter(sourceAdapter);

        String[] levels = {"Dễ", "Trung Bình", "Khó"};
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLevel.setAdapter(levelAdapter);

        // Xử lý nút Xác nhận trong Popup
        btnConfirm.setOnClickListener(view -> {
            String selectedSource = spnSource.getSelectedItem().toString();
            dialog.dismiss();

            Toast.makeText(TaoBoDe.this, "Bạn chọn: " + selectedSource, Toast.LENGTH_SHORT).show();
            if (selectedSource.equals("Lấy theo chủ đề")) {
            }
        });
    }

    /**
     * Logic tạo bộ đề và xuất file
     */
    private void taoBoDe() {
        String selectedTopic;
        int selectedLevel = 1;
        int questionLimit;

        try {
            selectedTopic = spinnerTopic.getSelectedItem().toString();
            String levelStr = spinnerLevel.getSelectedItem().toString();
            if (levelStr.equals("TRUNG BÌNH")) selectedLevel = 2;
            if (levelStr.equals("KHÓ")) selectedLevel = 3;

            String limitStr = editTextLimit.getText().toString();
            if (limitStr.isEmpty()) {
                Toast.makeText(this, "Nhập số lượng câu!", Toast.LENGTH_SHORT).show();
                return;
            }
            questionLimit = Integer.parseInt(limitStr);

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi nhập liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi ViewModel
        int finalSelectedLevel = selectedLevel;
        questionViewModel.getQuizQuestions(selectedTopic, selectedLevel, questionLimit)
                .observe(this, new Observer<List<Question>>() {
                    @Override
                    public void onChanged(List<Question> questions) {
                        // Xóa observer
                        questionViewModel.getQuizQuestions(selectedTopic, finalSelectedLevel, questionLimit).removeObserver(this);

                        if (questions != null && !questions.isEmpty()) {
                            exportQuestionsToCSV(questions);
                        } else {
                            Toast.makeText(TaoBoDe.this, "Không tìm thấy câu hỏi phù hợp.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // --- CÁC HÀM HỖ TRỢ XUẤT FILE ---
    private void exportQuestionsToCSV(List<Question> questions) {
        File directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        String fileName = "Quizizz_" + System.currentTimeMillis() + ".csv";
        File file = new File(directory, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Question Text,Option 1,Option 2,Option 3,Option 4,Correct Answer,Time in seconds\n");
            for (Question q : questions) {
                String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"30\"\n",
                        safeString(q.getContent()), safeString(q.getOption1()), safeString(q.getOption2()),
                        safeString(q.getOption3()), safeString(q.getOption4()), safeString(q.getCorrectAnswer()));
                writer.write(line);
            }
            openQuizizzUploadPage(file.getAbsolutePath());
        } catch (Exception e) {
            Log.e("TaoBoDe", "Lỗi xuất file", e);
            Toast.makeText(this, "Lỗi tạo file!", Toast.LENGTH_LONG).show();
        }
    }

    private String safeString(String value) {
        return value != null ? value.replace("\"", "\"\"") : "";
    }

    private void openQuizizzUploadPage(String filePath) {
        new AlertDialog.Builder(this)
                .setTitle("Thành công")
                .setMessage("File lưu tại:\n" + filePath)
                .setPositiveButton("Mở Web", (d, w) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://quizizz.com/create-quiz"));
                    try { startActivity(intent); } catch (Exception e) {}
                })
                .setNegativeButton("Đóng", null)
                .show();
    }
}