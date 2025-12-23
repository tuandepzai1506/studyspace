package com.example.studyspace.adapters; // Hoặc package com.example.studyspace.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.ClassModel;
import com.example.studyspace.R;

import java.util.List;

public class ClassListAdapter extends RecyclerView.Adapter<ClassListAdapter.ViewHolder> {

    private List<ClassModel> classList;
    private OnItemClickListener listener;

    // Interface để bắt sự kiện click
    public interface OnItemClickListener {
        void onItemClick(ClassModel classModel);
    }

    // Constructor
    public ClassListAdapter(List<ClassModel> classList, OnItemClickListener listener) {
        this.classList = classList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp layout item_class_row.xml vừa tạo ở Bước 2
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassModel model = classList.get(position);

        holder.tvName.setText(model.getClassName());

        // Hiển thị số lượng thành viên (kiểm tra null để tránh crash)
        int count = (model.getMember() != null) ? model.getMember().size() : 0;
        holder.tvCount.setText("Thành viên: " + count);

        // Bắt sự kiện click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(model));
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ đúng ID trong file item_class_row.xml
            tvName = itemView.findViewById(R.id.tv_row_class_name);
            tvCount = itemView.findViewById(R.id.tv_row_member_count);
        }
    }
}