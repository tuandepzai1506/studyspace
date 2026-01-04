package com.example.studyspace.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyspace.R;
import com.example.studyspace.models.User;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onChangeRole(User user);
        void onDeleteUser(User user);
    }

    public UserAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvRole.setText("Vai trò: " + user.getRole());

        holder.btnChangeRole.setOnClickListener(v -> listener.onChangeRole(user));
        holder.btnDeleteUser.setOnClickListener(v -> listener.onDeleteUser(user));
    }

    @Override
    public int getItemCount() { return userList.size(); }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        Button btnChangeRole, btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            tvRole = itemView.findViewById(R.id.tv_user_role);
            btnChangeRole = itemView.findViewById(R.id.btn_change_role);
            btnDeleteUser = itemView.findViewById(R.id.btn_delete_user);
        }
    }
}