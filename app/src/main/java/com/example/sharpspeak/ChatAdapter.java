package com.example.sharpspeak;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> chatList;

    public ChatAdapter(List<ChatMessage> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatList.get(position);
        holder.textMessage.setText(message.getText());

        if (message.isUser()) {
            holder.chatBubbleContainer.setGravity(Gravity.END); // رسالتك يمين
            holder.textMessage.setBackgroundColor(Color.parseColor("#a408f0")); // لون بنفسجي
            holder.textMessage.setTextColor(Color.WHITE); // نص أبيض
        } else {
            holder.chatBubbleContainer.setGravity(Gravity.START); // رسالة AI يسار
            holder.textMessage.setBackgroundColor(Color.parseColor("#000000")); // لون أسود داكن
            holder.textMessage.setTextColor(Color.WHITE); // نص أبيض
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;
        LinearLayout chatBubbleContainer;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            chatBubbleContainer = itemView.findViewById(R.id.chatBubbleContainer);
        }
    }
}