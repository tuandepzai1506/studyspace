package com.example.studyspace;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class createClass extends AppCompatActivity {
        private EditText editTextLimit1;
    private EditText editTextLimit2;
    private EditText editTextLimit3;
        private Button buttonCreateClass;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.create_class);
            Button createClass = findViewById(R.id.create_class);
            if (createClass != null) {
                createClass.setOnClickListener(v -> showClassPopup());
            }
            try {
                editTextLimit1 = findViewById(R.id.edittext_limit1);
                editTextLimit2 = findViewById(R.id.edittext_limit2);
                editTextLimit3 = findViewById(R.id.edittext_limit3);
                // Sự kiện nút Tạo Đề
            } catch (Exception e) {
                Log.e("TaoLop", "Lỗi khởi tạo UI", e);
                Toast.makeText(this, "Lỗi UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
    private void showClassPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(createClass.this);

        // Nạp giao diện popup
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_class, null);
        builder.setView(popupView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Ánh xạ View trong Popup
        EditText edittext_limit1 = popupView.findViewById(R.id.edittext_limit1);
        EditText edittext_limit2 = popupView.findViewById(R.id.edittext_limit2);
        EditText edittext_limit3 = popupView.findViewById(R.id.edittext_limit3);
        Button btnCreateClass = popupView.findViewById(R.id.button_create_class);

    }

}
