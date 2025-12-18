package com.example.studyspace.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.R;
import com.example.studyspace.ClassModel;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private List<ClassModel> classList;

    public ClassAdapter(List<ClassModel> classList) {
        this.classList = classList;
    }

    public void setClassList(List<ClassModel> classList) {
        this.classList = classList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassModel classModel = classList.get(position);
        if (classModel == null) return;

        holder.tvClassName.setText(classModel.getClassName());
        holder.tvMember.setText("So luong sinh vien: " + classModel.getMember());
    }

    @Override
    public int getItemCount() {
        if (classList != null) return classList.size();
        return 0;
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName, tvMember;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tv_class_name);
            tvMember = itemView.findViewById(R.id.tv_member);
        }
    }
}