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
        questionsRef.add(question)
                .addOnSuccessListener(documentReference -> {
                    if (listener != null) listener.onSaveSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onSaveFailure(e);
                });
    }

    public Task<Void> deleteQuestion(String questionId) {
        return questionsRef.document(questionId).delete();
    }

    public interface OnSaveCompleteListener {
        void onSaveSuccess();
        void onSaveFailure(Exception e);
    }
}
