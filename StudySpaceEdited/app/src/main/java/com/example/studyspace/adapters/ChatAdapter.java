package com.example.studyspace.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.DoQuizActivity;
import com.example.studyspace.QuizPreviewActivity;
import com.example.studyspace.R;
import com.example.studyspace.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final String senderId; // ID của người dùng hiện tại (để so sánh)
    private String classId; // ID của lớp học

    // Định nghĩa 2 loại tin nhắn
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, String classId) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.classId = classId;
    }

    // Hàm quan trọng nhất: Quyết định xem tin nhắn này là GỬI hay NHẬN
    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).getSenderId().equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            // Nếu là tin nhắn gửi -> Dùng layout item_sent_message
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_send_message, parent, false);
            return new SentMessageViewHolder(view, classId);
        } else {
            // Nếu là tin nhắn nhận -> Dùng layout item_received_message
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_received_message, parent, false);
            return new ReceivedMessageViewHolder(view, classId);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);

        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessage);
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    // --- ViewHolder cho tin nhắn gửi ---
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView textMessage, textDateTime;
        private final android.widget.Button btnStartQuiz;
        private final android.widget.Button btnPreviewQuiz;
        private final String classId;

        SentMessageViewHolder(View itemView, String classId) {
            super(itemView);
            this.classId = classId;
            textMessage = itemView.findViewById(R.id.textMessage);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            btnStartQuiz = itemView.findViewById(R.id.btnStartQuiz);
            btnPreviewQuiz = itemView.findViewById(R.id.btnPreviewQuiz);
        }

        void setData(ChatMessage chatMessage) {
            textMessage.setText(chatMessage.getMessage());
            textDateTime.setText(getReadableDateTime(chatMessage.getTimestamp()));

            if ("exam".equals(chatMessage.getType())) {
                btnStartQuiz.setVisibility(View.VISIBLE);
                btnPreviewQuiz.setVisibility(View.VISIBLE);

                btnStartQuiz.setOnClickListener(v -> {
                    android.content.Context context = v.getContext();
                    Intent intent = new Intent(context, DoQuizActivity.class);
                    intent.putExtra("EXAM_ID", chatMessage.getExamId());
                    intent.putExtra("EXAM_NAME", chatMessage.getMessage());
                    intent.putExtra("CLASS_ID", classId);
                    context.startActivity(intent);
                });

                btnPreviewQuiz.setOnClickListener(v -> {
                    android.content.Context context = v.getContext();
                    Intent intent = new Intent(context, QuizPreviewActivity.class);
                    intent.putExtra("EXAM_ID", chatMessage.getExamId());
                    context.startActivity(intent);
                });
            } else if ("quiz".equals(chatMessage.getType())) {
                btnStartQuiz.setVisibility(View.VISIBLE);
                btnPreviewQuiz.setVisibility(View.GONE);

                btnStartQuiz.setOnClickListener(v -> {
                    android.content.Context context = v.getContext();
                    Intent intent = new Intent(context, DoQuizActivity.class);
                    intent.putExtra("TOPIC", chatMessage.getTopic());
                    intent.putExtra("LEVEL", chatMessage.getLevel());
                    intent.putExtra("LIMIT", chatMessage.getLimit());
                    context.startActivity(intent);
                });
            } else {
                btnStartQuiz.setVisibility(View.GONE);
                btnPreviewQuiz.setVisibility(View.GONE);
            }
        }
    }

    // --- ViewHolder cho tin nhắn nhận ---
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView textMessage, textDateTime;
        private final android.widget.Button btnStartQuiz;
        private final android.widget.Button btnPreviewQuiz;
        private final String classId;

        ReceivedMessageViewHolder(View itemView, String classId) {
            super(itemView);
            this.classId = classId;
            textMessage = itemView.findViewById(R.id.textMessage);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            btnStartQuiz = itemView.findViewById(R.id.btnStartQuiz);
            btnPreviewQuiz = itemView.findViewById(R.id.btnPreviewQuiz);
        }

        void setData(ChatMessage chatMessage) {
            textMessage.setText(chatMessage.getMessage());
            textDateTime.setText(getReadableDateTime(chatMessage.getTimestamp()));

            if ("exam".equals(chatMessage.getType())) {
                btnStartQuiz.setVisibility(View.VISIBLE);
                btnPreviewQuiz.setVisibility(View.VISIBLE);
                btnStartQuiz.setText("📝 Làm bài ngay");
                btnPreviewQuiz.setText("👁 Xem trước");

                btnStartQuiz.setOnClickListener(v -> {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DoQuizActivity.class);
                    intent.putExtra("EXAM_ID", chatMessage.getExamId());
                    intent.putExtra("EXAM_NAME", chatMessage.getMessage());
                    intent.putExtra("CLASS_ID", classId);
                    context.startActivity(intent);
                });

                btnPreviewQuiz.setOnClickListener(v -> {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, QuizPreviewActivity.class);
                    intent.putExtra("EXAM_ID", chatMessage.getExamId());
                    context.startActivity(intent);
                });
            } else if ("quiz".equals(chatMessage.getType())) {
                btnStartQuiz.setVisibility(View.VISIBLE);
                btnPreviewQuiz.setVisibility(View.GONE);
                btnStartQuiz.setText("📝 Làm bài ngay");

                btnStartQuiz.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, DoQuizActivity.class);
                        intent.putExtra("TOPIC", chatMessage.getTopic());
                        intent.putExtra("LEVEL", chatMessage.getLevel());
                        intent.putExtra("LIMIT", chatMessage.getLimit());

                        // Bắt đầu Activity
                        context.startActivity(intent);
                    }
                });
            } else {
                btnStartQuiz.setVisibility(View.GONE);
                btnPreviewQuiz.setVisibility(View.GONE);
            }
        }
    }

    // Hàm phụ để format ngày giờ cho đẹp (Ví dụ: "14:30 - 20/12/2025")
    private static String getReadableDateTime(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(date);
    }
}