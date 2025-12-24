package com.example.studyspace.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.R;
import com.example.studyspace.models.StudentScoreData;

import java.util.List;

public class ClassScoresAdapter extends RecyclerView.Adapter<ClassScoresAdapter.ScoreViewHolder> {

    private List<StudentScoreData> scoreList;

    public ClassScoresAdapter(List<StudentScoreData> scoreList) {
        this.scoreList = scoreList;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_score, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        StudentScoreData data = scoreList.get(position);
        if (data == null) return;

        // Rank (position + 1)
        holder.tvRank.setText(String.valueOf(position + 1));

        // Student name
        String name = data.getStudentName() != null ? data.getStudentName() : "N/A";
        holder.tvStudentName.setText(name);

        // Score
        holder.tvScore.setText(String.format("%.1f", data.getScore()));

        // Correct answers / Total questions
        holder.tvCorrectAnswers.setText(data.getCorrectAnswers() + "/" + data.getTotalQuestions());
    }

    @Override
    public int getItemCount() {
        return scoreList != null ? scoreList.size() : 0;
    }

    public static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvStudentName, tvScore, tvCorrectAnswers;

        public ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvScore = itemView.findViewById(R.id.tv_score);
            tvCorrectAnswers = itemView.findViewById(R.id.tv_correct_answers);
        }
    }
}
