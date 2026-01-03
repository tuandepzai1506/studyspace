package com.example.studyspace.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyspace.R;
import java.util.List;
import java.util.Map;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<Map<String, String>> memberList; // Chứa {uid, name}
    private OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onDeleteClick(String studentId, String studentName, int position);
    }

    public MemberAdapter(List<Map<String, String>> memberList, OnMemberClickListener listener) {
        this.memberList = memberList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Map<String, String> member = memberList.get(position);
        String name = member.get("name");
        String uid = member.get("uid");

        holder.tvName.setText(name);
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(uid, name, position));
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageButton btnDelete;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            btnDelete = itemView.findViewById(R.id.btnDeleteMember);
        }
    }
}