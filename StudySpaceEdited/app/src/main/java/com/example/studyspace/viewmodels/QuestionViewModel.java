package com.example.studyspace.viewmodels;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.studyspace.models.Question;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class QuestionViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "QuestionViewModel";
    private static final String COLLECTION_NAME = "questions";

    /**
     * Lấy một danh sách câu hỏi giới hạn cho bài trắc nghiệm (quiz).
     * @param topic Chủ đề câu hỏi.
     * @param level Độ khó của câu hỏi.
     * @param limit Số lượng câu hỏi tối đa cần lấy.
     * @return LiveData chứa danh sách câu hỏi hoặc null nếu có lỗi.
     */
    public LiveData<List<Question>> getQuizQuestions(String topic, int level, int limit) {
        MutableLiveData<List<Question>> questionsLiveData = new MutableLiveData<>();

        db.collection(COLLECTION_NAME)
                .whereEqualTo("topic", topic)
                .whereEqualTo("level", level)
                .limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Question> questionList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Question question = document.toObject(Question.class);
                            question.setId(document.getId()); // Gán ID của document vào đối tượng
                            questionList.add(question);
                        }
                        questionsLiveData.setValue(questionList);
                    } else {
                        Log.e(TAG, "Lỗi lấy dữ liệu quiz: ", task.getException());
                        questionsLiveData.setValue(null);
                    }
                });

        return questionsLiveData;
    }

    /**
     * Lấy TẤT CẢ các câu hỏi từ Firestore để quản lý trong ngân hàng câu hỏi.
     * @return LiveData chứa danh sách tất cả câu hỏi.
     */
    public LiveData<List<Question>> getAllQuestions() {
        MutableLiveData<List<Question>> questionsLiveData = new MutableLiveData<>();
        db.collection(COLLECTION_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Question> questionList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Question question = document.toObject(Question.class);
                            question.setId(document.getId());
                            questionList.add(question);
                        }
                        questionsLiveData.setValue(questionList);
                    } else {
                        Log.e(TAG, "Lỗi lấy tất cả câu hỏi: ", task.getException());
                        questionsLiveData.setValue(null);
                    }
                });
        return questionsLiveData;
    }

    /**
     * Thêm một câu hỏi mới vào Firestore.
     * @param question Đối tượng câu hỏi cần thêm.
     * @return Task<Void> để theo dõi quá trình thực hiện.
     */
    // Trong file: QuestionViewModel.java

    public void addQuestion(Question question, OnSaveCompleteListener listener) {
        db.collection(COLLECTION_NAME).add(question)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Thêm câu hỏi thành công với ID: " + documentReference.getId());
                    if (listener != null) {
                        listener.onSaveSuccess(); // GỌI KHI THÀNH CÔNG
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi thêm câu hỏi", e);
                    if (listener != null) {
                        listener.onSaveFailure(e); // GỌI KHI THẤT BẠI
                    }
                });
    }


    /**
     * Cập nhật (sửa) một câu hỏi đã có.
     * @param question Đối tượng câu hỏi đã được chỉnh sửa (phải chứa ID).
     * @return Task<Void> để theo dõi quá trình thực hiện.
     */
    public Task<Void> updateQuestion(Question question) {
        if (question.getId() == null || question.getId().isEmpty()) {
            Log.e(TAG, "ID câu hỏi không được rỗng khi cập nhật");
            // Trả về một task thất bại ngay lập tức
            return Tasks.forException(new IllegalArgumentException("Question ID must not be null"));
        }
        return db.collection(COLLECTION_NAME).document(question.getId()).set(question)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Cập nhật câu hỏi thành công"))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi cập nhật câu hỏi", e));
    }

    /**
     * Xóa một câu hỏi khỏi Firestore.
     * @param questionId ID của câu hỏi cần xóa.
     * @return Task<Void> để theo dõi quá trình thực hiện.
     */
    public Task<Void> deleteQuestion(String questionId) {
        return db.collection(COLLECTION_NAME).document(questionId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Xóa câu hỏi thành công"))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi xóa câu hỏi", e));
    }
    public interface OnSaveCompleteListener {
        void onSaveSuccess();
        void onSaveFailure(Exception e);
    }
}
