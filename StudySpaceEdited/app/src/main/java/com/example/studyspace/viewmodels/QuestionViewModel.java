package com.example.studyspace.viewmodels;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.studyspace.models.Question;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class QuestionViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Hàm lấy danh sách câu hỏi theo Chủ đề và Độ khó
    public LiveData<List<Question>> getQuizQuestions(String topic, int level, int limit) {
        MutableLiveData<List<Question>> questionsLiveData = new MutableLiveData<>();

        // Truy vấn Firestore: Collection "questions" -> lọc theo topic và level
        db.collection("questions")
                .whereEqualTo("topic", topic)
                .whereEqualTo("level", level)
                .limit(limit) // Giới hạn số lượng câu hỏi
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Question> questionList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Chuyển đổi dữ liệu JSON từ Firebase thành object Question
                            Question question = document.toObject(Question.class);
                            question.setId(document.getId());
                            questionList.add(question);
                        }
                        questionsLiveData.setValue(questionList);
                    } else {
                        Log.e("QuestionViewModel", "Lỗi lấy dữ liệu: ", task.getException());
                        questionsLiveData.setValue(null); // Trả về null nếu lỗi
                    }
                });

        return questionsLiveData;
    }
}