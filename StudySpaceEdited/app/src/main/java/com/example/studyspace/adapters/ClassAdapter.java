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
    private IClickItemClassListener iClickItemClassListener; // 1. Khai báo biến Listener

    // 2. Định nghĩa Interface để truyền sự kiện ra ngoài
    public interface IClickItemClassListener {
        void onClickItemClass(ClassModel classModel);
    }

    // 3. Cập nhật Constructor để nhận thêm tham số listener
    public ClassAdapter(List<ClassModel> classList, IClickItemClassListener listener) {
        this.classList = classList;
        this.iClickItemClassListener = listener;
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

        // 4. Bắt sự kiện click vào item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi hàm trong interface để Activity xử lý
                iClickItemClassListener.onClickItemClass(classModel);
            }
        });
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