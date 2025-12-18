package com.example.studyspace.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.R;
import com.example.studyspace.models.Question;

import java.util.List;

public class QuizPreviewAdapter extends RecyclerView.Adapter<QuizPreviewAdapter.QuizViewHolder> {

    private List<Question> questionList;

    public QuizPreviewAdapter(List<Question> questionList) {
        this.questionList = questionList;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_preview, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Question question = questionList.get(position);

        // Hiển thị số thứ tự câu hỏi
        holder.tvIndex.setText("Câu " + (position + 1) + ":");
        holder.tvContent.setText(question.getQuestionText());

        // Hiển thị đáp án đúng (vì đây là màn hình xem trước đề thi cho giáo viên/admin)
        // Logic lấy text đáp án dựa trên index
        String correctAnswerText = "";
        if (question.getCorrectAnswerIndex() >= 0 && question.getCorrectAnswerIndex() < question.getOptions().size()) {
            correctAnswerText = question.getOptions().get(question.getCorrectAnswerIndex());
        }
        holder.tvAnswer.setText("Đáp án đúng: " + correctAnswerText);
    }

    @Override
    public int getItemCount() {
        return questionList != null ? questionList.size() : 0;
    }

    public static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex, tvContent, tvAnswer;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.preview_question_text);
            tvAnswer = itemView.findViewById(R.id.preview_answer);
        }
    }
}