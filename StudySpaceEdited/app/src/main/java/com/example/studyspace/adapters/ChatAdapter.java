package com.example.studyspace.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyspace.DoQuizActivity;
import com.example.studyspace.R;
import com.example.studyspace.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final String senderId;
    private String classId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, String classId) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.classId = classId;
    }

    // --- HÀM PHỤ: Xử lý Copy vào Clipboard ---
    private static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Chat Message", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Đã sao chép tin nhắn", Toast.LENGTH_SHORT).show();
        }
    }

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
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_send_message, parent, false);
            return new SentMessageViewHolder(view, classId);
        } else {
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

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView textMessage, textDateTime;
        private final android.widget.Button btnStartQuiz;
        private final String classId;

        SentMessageViewHolder(View itemView, String classId) {
            super(itemView);
            this.classId = classId;
            textMessage = itemView.findViewById(R.id.textMessage);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            btnStartQuiz = itemView.findViewById(R.id.btnStartQuiz);
        }

        void setData(ChatMessage chatMessage) {
            textMessage.setText(chatMessage.getMessage());
            textDateTime.setText(getReadableDateTime(chatMessage.getTimestamp()));

            // SỬA TẠI ĐÂY: Thêm sự kiện nhấn giữ để copy
            textMessage.setOnLongClickListener(v -> {
                copyToClipboard(v.getContext(), chatMessage.getMessage());
                return true;
            });

            if ("exam".equals(chatMessage.getType())) {
                btnStartQuiz.setVisibility(View.VISIBLE);
                btnStartQuiz.setOnClickListener(v -> {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DoQuizActivity.class);
                    intent.putExtra("EXAM_ID", chatMessage.getExamId());
                    intent.putExtra("EXAM_NAME", chatMessage.getMessage());
                    intent.putExtra("CLASS_ID", classId);
                    context.startActivity(intent);
                });
            } else if ("quiz".equals(chatMessage.getType())) {
                btnStartQuiz.setVisibility(View.VISIBLE);
                btnStartQuiz.setOnClickListener(v -> {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DoQuizActivity.class);
                    intent.putExtra("TOPIC", chatMessage.getTopic());
                    intent.putExtra("LEVEL", chatMessage.getLevel());
                    intent.putExtra("LIMIT", chatMessage.getLimit());
                    context.startActivity(intent);
                });
            } else {
                btnStartQuiz.setVisibility(View.GONE);
            }
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView textMessage, textDateTime;
        private final android.widget.Button btnStartQuiz;
        private final String classId;

        ReceivedMessageViewHolder(View itemView, String classId) {
            super(itemView);
            this.classId = classId;
            textMessage = itemView.findViewById(R.id.textMessage);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            btnStartQuiz = itemView.findViewById(R.id.btnStartQuiz);
        }

        void setData(ChatMessage chatMessage) {
            textMessage.setText(chatMessage.getMessage());
            textDateTime.setText(getReadableDateTime(chatMessage.getTimestamp()));

            // SỬA TẠI ĐÂY: Thêm sự kiện nhấn giữ để copy
            textMessage.setOnLongClickListener(v -> {
                copyToClipboard(v.getContext(), chatMessage.getMessage());
                return true;
            });

            if ("exam".equals(chatMessage.getType())) {
                btnStartQuiz.setVisibility(View.VISIBLE);
                btnStartQuiz.setText("📝 Làm bài ngay");
                btnStartQuiz.setOnClickListener(v -> {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DoQuizActivity.class);
                    intent.putExtra("EXAM_ID", chatMessage.getExamId());
                    intent.putExtra("EXAM_NAME", chatMessage.getMessage());
                    intent.putExtra("CLASS_ID", classId);
                    context.startActivity(intent);
                });
            } else if ("quiz".equals(chatMessage.getType())) {
                btnStartQuiz.setVisibility(View.VISIBLE);
                btnStartQuiz.setText("📝 Làm bài ngay");
                btnStartQuiz.setOnClickListener(v -> {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DoQuizActivity.class);
                    intent.putExtra("TOPIC", chatMessage.getTopic());
                    intent.putExtra("LEVEL", chatMessage.getLevel());
                    intent.putExtra("LIMIT", chatMessage.getLimit());
                    context.startActivity(intent);
                });
            } else {
                btnStartQuiz.setVisibility(View.GONE);
            }
        }
    }

    private static String getReadableDateTime(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(date);
    }
}