package com.example.studyspace.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.studyspace.models.Question;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class QuestionViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference questionsRef = db.collection("questions");

    // LiveData để chứa danh sách câu hỏi (dùng cho Ngân hàng câu hỏi)
    private final MutableLiveData<List<Question>> questionsLiveData = new MutableLiveData<>();

    // Biến quản lý việc lắng nghe Firestore
    private ListenerRegistration listenerRegistration;

    // =================================================================================
    // PHẦN 1: QUẢN LÝ NGÂN HÀNG CÂU HỎI (REALTIME)
    // =================================================================================

    // Getter cho LiveData
    public LiveData<List<Question>> getQuestionsLiveData() {
        return questionsLiveData;
    }

    // Bắt đầu lắng nghe thay đổi từ Firestore
    public void startListening() {
        if (listenerRegistration != null) return; // Đang lắng nghe rồi thì thôi

        // Lấy dữ liệu, sắp xếp theo topic
        listenerRegistration = questionsRef.orderBy("topic", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    List<Question> questionList = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Question question = doc.toObject(Question.class);
                            question.setId(doc.getId()); // Lưu ID để xóa/sửa
                            questionList.add(question);
                        }
                    }
                    questionsLiveData.setValue(questionList);
                });
    }

    // Dừng lắng nghe
    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    // =================================================================================
    // PHẦN 2: TẠO ĐỀ THI / XEM TRƯỚC (LỌC DỮ LIỆU) - MỚI THÊM
    // =================================================================================

    /**
     * Hàm lấy danh sách câu hỏi theo tiêu chí để tạo đề thi
     * Lưu ý: Cần tạo Index trên Firestore nếu log báo lỗi
     */
    public LiveData<List<Question>> getQuizQuestions(String topic, int level, int limit) {
        MutableLiveData<List<Question>> quizQuestions = new MutableLiveData<>();

        // Truy vấn: Lọc Topic + Level + Giới hạn số lượng
        questionsRef.whereEqualTo("topic", topic)
                .whereEqualTo("level", level)
                .limit(limit)
                .get() // Dùng get() lấy 1 lần, không cần realtime
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Question> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Question q = doc.toObject(Question.class);
                        q.setId(doc.getId());
                        list.add(q);
                    }
                    quizQuestions.setValue(list);
                })
                .addOnFailureListener(e -> {
                    // Nếu lỗi thì trả về list rỗng để app không bị crash
                    quizQuestions.setValue(new ArrayList<>());
                });

        return quizQuestions;
    }

    // =================================================================================
    // PHẦN 3: THÊM / XÓA / SỬA
    // =================================================================================

    // Thêm câu hỏi
    public void addQuestion(Question question, OnSaveCompleteListener listener) {
        questionsRef.add(question)
                .addOnSuccessListener(documentReference -> {
                    if (listener != null) listener.onSaveSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSaveFailure(e);
                });
    }

    // Xóa câu hỏi
    public Task<Void> deleteQuestion(String questionId) {
        return questionsRef.document(questionId).delete();
    }

    // Interface callback
    public interface OnSaveCompleteListener {
        void onSaveSuccess();
        void onSaveFailure(Exception e);
    }
}