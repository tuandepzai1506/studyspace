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

public class QuizPreviewAdapter extends RecyclerView.Adapter<QuizPreviewAdapter.ViewHolder> {

    private final List<Question> questions;

    public QuizPreviewAdapter(List<Question> questions) {
        this.questions = questions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Question question = questions.get(position);
        // Hiển thị câu hỏi kèm số thứ tự
        String questionContent = (position + 1) + ". " + question.getQuestionText();
        holder.questionText.setText(questionContent);

        // Hiển thị đáp án đúng (giả sử đáp án đúng luôn là option 1)
        String correctAnswerText = "Đáp án đúng: " + question.getOption1();
        holder.option1Text.setText(correctAnswerText);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView questionText;
        TextView option1Text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.preview_question_text);
            option1Text = itemView.findViewById(R.id.preview_option_1);
        }
    }
}
    