package com.example.studyspace.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.R;
import com.example.studyspace.models.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    // LỖI 1: Tách hai khai báo biến ra hai dòng riêng biệt
    private List<Question> questions = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question currentQuestion = questions.get(position);
        holder.textViewQuestion.setText(currentQuestion.getQuestionText());
        String topicLevel = "Chủ đề: " + currentQuestion.getTopic() + " | Độ khó: " + currentQuestion.getLevel();
        holder.textViewTopicLevel.setText(topicLevel);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
        notifyDataSetChanged(); // Thông báo cho Adapter có dữ liệu mới
    }

    class QuestionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewQuestion;
        private final TextView textViewTopicLevel;
        private final Button buttonEdit;
        private final Button buttonDelete;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewQuestion = itemView.findViewById(R.id.text_view_question);
            textViewTopicLevel = itemView.findViewById(R.id.text_view_topic_level);
            buttonEdit = itemView.findViewById(R.id.button_edit);
            buttonDelete = itemView.findViewById(R.id.button_delete);

            buttonEdit.setOnClickListener(v -> {
                int position = getBindingAdapterPosition(); // Thay thế getAdapterPosition()
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(questions.get(position));
                }
            });

            buttonDelete.setOnClickListener(v -> {
                int position = getBindingAdapterPosition(); // Thay thế getAdapterPosition()
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(questions.get(position));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onEditClick(Question question);
        void onDeleteClick(Question question);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
