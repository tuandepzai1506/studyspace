package com.example.studyspace.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.studyspace.models.Question;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference questionsRef = db.collection("questions");
    private final CollectionReference examsRef = db.collection("Exam"); // Tham chiếu đến collection Exam

    private final MutableLiveData<List<Question>> questionsLiveData = new MutableLiveData<>();
    private ListenerRegistration listenerRegistration;
    public Task<Void> deleteQuestion(String questionId) {
        // Hàm này sử dụng questionsRef đã khai báo ở đầu class của bạn
        return questionsRef.document(questionId).delete();
    }
    public LiveData<List<Question>> getQuestionsLiveData() {
        return questionsLiveData;
    }
    // Lấy câu hỏi cho một bài thi cụ thể
    public Task<List<Question>> getQuestionsForExam(String examId) {
        return examsRef.document(examId).collection("questions").get().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            List<Question> list = new ArrayList<>();
            if (task.getResult() != null) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Question q = doc.toObject(Question.class);
                    q.setId(doc.getId()); // Lưu ID của câu hỏi
                    list.add(q);
                }
            }
            return list;
        });
    }


    public void startListening() {
        if (listenerRegistration != null) return;

        listenerRegistration = questionsRef.orderBy("topic", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    List<Question> questionList = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Question question = doc.toObject(Question.class);
                            question.setId(doc.getId());
                            questionList.add(question);
                        }
                    }
                    questionsLiveData.setValue(questionList);
                });
    }

    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    public Task<List<Question>> getQuizQuestionsByDifficulty(String topic, int minLevel, int maxLevel, int limit) {
        if (limit == 0) {
            return Tasks.forResult(new ArrayList<>());
        }

        // Fetch all questions by topic first, then filter by level in code to avoid index requirement
        return questionsRef.whereEqualTo("topic", topic).get().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            
            List<Question> filteredList = new ArrayList<>();
            if (task.getResult() != null) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Question q = doc.toObject(Question.class);
                    q.setId(doc.getId());
                    
                    // Filter by level range in code
                    if (q.getLevel() >= minLevel && q.getLevel() <= maxLevel) {
                        filteredList.add(q);
                    }
                }
            }
            
            // Shuffle and limit the results
            if (filteredList.size() > limit) {
                Collections.shuffle(filteredList);
                return filteredList.subList(0, limit);
            } else {
                Collections.shuffle(filteredList);
                return filteredList;
            }
        });
    }

    public void addQuestion(Question question, OnSaveCompleteListener listener) {
        // 1. Tạo một Document Reference mới để lấy ID ngẫu nhiên trước
        com.google.firebase.firestore.DocumentReference newDocRef = questionsRef.document();

        // 2. Lấy ID đó gán vào thuộc tính id của đối tượng Question
        String autoId = newDocRef.getId();
        question.setId(autoId);

        // 3. Sử dụng .set() thay vì .add() để lưu dữ liệu kèm ID
        newDocRef.set(question)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSaveSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSaveFailure(e);
                });
    }

    public interface OnSaveCompleteListener {
        void onSaveSuccess();
        void onSaveFailure(Exception e);
    }
    // Trong QuestionViewModel.java

    public void filterQuestions(String topic, Integer level) {
        // Luôn bắt đầu từ tham chiếu gốc
        Query query = questionsRef;

        // 1. LUÔN LỌC THEO CHỦ ĐỀ TRƯỚC (Rất quan trọng)
        if (topic != null && !topic.isEmpty()) {
            query = query.whereEqualTo("topic", topic);
        }

        // 2. LỌC THÊM THEO ĐỘ KHÓ (Nếu có chọn mức độ > 0)
        if (level != null && level > 0) {
            query = query.whereEqualTo("level", level);
        }

        // 3. Thực hiện truy vấn và cập nhật LiveData
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Question> filteredList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Question q = doc.toObject(Question.class);
                q.setId(doc.getId());
                filteredList.add(q);
            }
            questionsLiveData.setValue(filteredList);
        }).addOnFailureListener(e -> {
            // Gợi ý: Nếu gặp lỗi tại đây, có thể do bạn chưa tạo Index trên Firestore
            // Hãy kiểm tra Logcat để lấy link tạo Composite Index.
        });
    }

    // Thêm hàm để reset về danh sách đầy đủ
    public void clearFilter() {
        startListening(); // Gọi lại hàm lắng nghe mặc định của bạn
    }
}
