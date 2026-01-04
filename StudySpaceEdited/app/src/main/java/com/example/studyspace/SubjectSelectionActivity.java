package com.example.studyspace;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyspace.adapters.SubjectAdapter;
import com.example.studyspace.models.Subject;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SubjectSelectionActivity extends AppCompatActivity {
    private RecyclerView rvSubjects;
    private SubjectAdapter adapter;
    private List<Subject> subjectList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_selection);

        rvSubjects = findViewById(R.id.rv_subjects);
        rvSubjects.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new SubjectAdapter(subjectList, subject -> {
            // Khi nhấn vào môn học, chuyển sang Question_Bank và gửi ID môn đó đi
            Intent intent = new Intent(this, Question_Bank.class);
            intent.putExtra("SELECTED_SUBJECT_ID", subject.getId());
            intent.putExtra("SELECTED_SUBJECT_NAME", subject.getName());
            startActivity(intent);
        });

        rvSubjects.setAdapter(adapter);
        loadSubjects();
    }

    private void loadSubjects() {
        db.collection("subjects").get().addOnSuccessListener(queryDocumentSnapshots -> {
            subjectList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Subject s = doc.toObject(Subject.class);
                s.setId(doc.getId());
                subjectList.add(s);
            }
            adapter.notifyDataSetChanged();
        });
    }
}