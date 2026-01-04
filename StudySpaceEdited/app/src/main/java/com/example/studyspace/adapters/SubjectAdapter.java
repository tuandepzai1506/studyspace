package com.example.studyspace.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyspace.R;
import com.example.studyspace.models.Subject;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {
    private List<Subject> subjectList;
    private OnSubjectClickListener listener;

    public interface OnSubjectClickListener {
        void onSubjectClick(Subject subject);
    }

    public SubjectAdapter(List<Subject> subjectList, OnSubjectClickListener listener) {
        this.subjectList = subjectList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.name.setText(subject.getName());
        // Ở đây bạn có thể dùng thư viện Glide để load icon nếu là URL,
        // hoặc để tạm icon mặc định.
        holder.itemView.setOnClickListener(v -> listener.onSubjectClick(subject));
    }

    @Override
    public int getItemCount() { return subjectList.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_subject_name);
            icon = itemView.findViewById(R.id.img_subject_icon);
        }
    }
}