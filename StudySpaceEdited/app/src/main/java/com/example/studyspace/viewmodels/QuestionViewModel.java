package com.example.studyspace.viewmodels;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.studyspace.models.Question;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration; // Thêm import này
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class QuestionViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "QuestionViewModel";
    private static final String COLLECTION_NAME = "questions";

    // --- LiveData để chứa danh sách câu hỏi và cho các Activity quan sát ---
    private final MutableLiveData<List<Question>> allQuestionsLiveData = new MutableLiveData<>();

    // --- Trình lắng nghe thời gian thực ---
    // Biến này để lưu lại trình lắng nghe, giúp chúng ta có thể gỡ bỏ nó khi không cần thiết
    private ListenerRegistration questionsListenerRegistration;

    /**
     * Interface để xử lý callback khi thêm câu hỏi.
     */
    public interface OnSaveCompleteListener {
        void onSaveSuccess();
        void onSaveFailure(Exception e);
    }

    /**
     * Cung cấp LiveData cho Activity để quan sát.
     * @return LiveData chứa danh sách tất cả câu hỏi.
     */
    public LiveData<List<Question>> getAllQuestions() {
        return allQuestionsLiveData;
    }

    /**
     * Bắt đầu lắng nghe các thay đổi trên collection 'questions' theo thời gian thực.
     * Hàm này nên được gọi trong onStart() hoặc onResume() của Activity.
     */
    public void startListeningForQuestionChanges() {
        // Nếu đã có một listener đang chạy, không tạo listener mới
        if (questionsListenerRegistration != null) {
            return;
        }

        Log.d(TAG, "Bắt đầu lắng nghe thay đổi từ collection 'questions'...");
        questionsListenerRegistration = db.collection(COLLECTION_NAME)
                .addSnapshotListener((snapshots, e) -> {
                    // Xử lý lỗi nếu có
                    if (e != null) {
                        Log.e(TAG, "Lỗi lắng nghe thay đổi:", e);
                        allQuestionsLiveData.setValue(null); // Báo lỗi cho UI
                        return;
                    }

                    // Xử lý dữ liệu trả về
                    List<Question> questionList = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Question question = doc.toObject(Question.class);
                            question.setId(doc.getId()); // Gán ID của document vào đối tượng
                            questionList.add(question);
                        }
                    }
                    allQuestionsLiveData.setValue(questionList); // Cập nhật LiveData với dữ liệu mới
                    Log.d(TAG, "Dữ liệu được cập nhật theo thời gian thực. Số câu hỏi: " + questionList.size());
                });
    }

    /**
     * Dừng lắng nghe các thay đổi để tiết kiệm tài nguyên khi không cần thiết.
     */
    public void stopListeningForChanges() {
        if (questionsListenerRegistration != null) {
            questionsListenerRegistration.remove(); // Gỡ bỏ trình lắng nghe
            questionsListenerRegistration = null; // Đặt lại để có thể đăng ký lại sau
            Log.d(TAG, "Đã dừng lắng nghe thay đổi.");
        }
    }

    /**
     * ViewModel sẽ tự động gọi hàm này khi nó bị hủy,
     * đảm bảo không có rò rỉ bộ nhớ từ trình lắng nghe.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        stopListeningForChanges();
    }

    // --- CÁC HÀM CŨ KHÔNG THAY ĐỔI ---

    /**
     * Thêm một câu hỏi mới vào Firestore và thông báo kết quả qua listener.
     */
    public void addQuestion(Question question, OnSaveCompleteListener listener) {
        db.collection(COLLECTION_NAME).add(question)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Firebase: Thêm câu hỏi thành công.");
                    if (listener != null) {
                        listener.onSaveSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase: Lỗi thêm câu hỏi.", e);
                    if (listener != null) {
                        listener.onSaveFailure(e);
                    }
                });
    }

    /**
     * Xóa một câu hỏi khỏi Firestore.
     */
    public Task<Void> deleteQuestion(String questionId) {
        return db.collection(COLLECTION_NAME).document(questionId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Xóa câu hỏi thành công"))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi xóa câu hỏi", e));
    }

    /**
     * Lấy danh sách câu hỏi cho một bài quiz cụ thể (lấy một lần).
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
                            question.setId(document.getId());
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
}
