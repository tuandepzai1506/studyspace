package com.example.studyspace.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyspace.R;
import com.example.studyspace.models.ScoreResult;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    private List<ScoreResult> listScore;

    public ScoreAdapter(List<ScoreResult> listScore) {
        this.listScore = listScore;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        ScoreResult result = listScore.get(position);
        if (result == null) return;

        holder.tvTopic.setText("Chủ đề: " + result.getTopic());
        holder.tvScore.setText(String.valueOf(result.getScore()));

        // Format ngày tháng
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        if (result.getTimestamp() != null) {
            holder.tvDate.setText(sdf.format(result.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return listScore != null ? listScore.size() : 0;
    }

    public static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvTopic, tvScore, tvDate;

        public ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopic = itemView.findViewById(R.id.tv_topic_result);
            tvScore = itemView.findViewById(R.id.tv_score_result);
            tvDate = itemView.findViewById(R.id.tv_date_result);
        }
    }
}