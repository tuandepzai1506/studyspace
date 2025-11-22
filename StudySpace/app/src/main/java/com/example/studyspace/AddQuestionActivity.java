// Thay thế com.example.studyspace bằng tên gói (package) của bạn
package com.example.studyspace;import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// Lớp này kế thừa từ AppCompatActivity để hoạt động như một màn hình
public class AddQuestionActivity extends AppCompatActivity {

    // Khai báo các thành phần có trong layout XML
    private EditText edtQuestion, edtA, edtB, edtC, edtD;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // *** DÒNG QUAN TRỌNG NHẤT: Gán layout cho Activity này ***
        // Dòng này sẽ liên kết mã Java với tệp activity_add_question.xml
        setContentView(R.layout.activity_add_question);

        // Ánh xạ các biến đã khai báo với các thành phần trong file XML
        edtQuestion = findViewById(R.id.edtQuestion);
        edtA = findViewById(R.id.edtA);
        edtB = findViewById(R.id.edtB);
        edtC = findViewById(R.id.edtC);
        edtD = findViewById(R.id.edtD);
        btnSave = findViewById(R.id.btnSave);

        // Thiết lập sự kiện cho nút "Lưu câu hỏi"
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logic để lưu câu hỏi sẽ được thêm vào đây
                // Ví dụ: hiển thị một thông báo tạm thời
                Toast.makeText(AddQuestionActivity.this, "Đã nhấn nút Lưu!", Toast.LENGTH_SHORT).show();

                // Sau khi lưu, bạn có thể đóng màn hình này để quay lại màn hình chính
                // finish();
            }
        });
    }
}
    