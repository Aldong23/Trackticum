package com.example.trackticum.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.models.Message;
import com.example.trackticum.R;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CoordinatorMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_RECEIVED = 1;
    private static final int VIEW_TYPE_SENT = 2;

    private Context context;
    private List<Message> messageList;
    private String currentUserId;

    public CoordinatorMessageAdapter(Context context, List<Message> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        return (message.getSenderType().equals("company") || message.getSenderType().equals("student")) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_RECEIVED) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_left, parent, false);
            return new ReceivedMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_right, parent, false);
            return new SentMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        } else {
            ((SentMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder for received messages
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView senderPicture;
        TextView messageBubble, timestamp;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderPicture = itemView.findViewById(R.id.senderPictureReceived);
            messageBubble = itemView.findViewById(R.id.messageBubbleReceived);
            timestamp = itemView.findViewById(R.id.timestampReceived);
        }

        public void bind(Message message) {
            messageBubble.setText(message.getMessage());
            timestamp.setText(message.getCreatedAt());
            timestamp.setVisibility(View.GONE);

            messageBubble.setOnClickListener(v -> {
                if (timestamp.getVisibility() == View.VISIBLE) {
                    timestamp.setVisibility(View.GONE);
                } else {
                    timestamp.setVisibility(View.VISIBLE);
                }
            });

            // Load sender picture using Picasso or another image-loading library
            Picasso.get()
                    .load(message.getImage())
                    .placeholder(R.drawable.img_placeholder)
                    .into(senderPicture);
        }
    }

    // ViewHolder for sent messages
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageBubble, timestamp;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageBubble = itemView.findViewById(R.id.messageBubbleSent);
            timestamp = itemView.findViewById(R.id.timestampSent);
        }

        public void bind(Message message) {
            messageBubble.setText(message.getMessage());
            timestamp.setText(message.getCreatedAt());
            timestamp.setVisibility(View.GONE);

            messageBubble.setOnClickListener(v -> {
                if (timestamp.getVisibility() == View.VISIBLE) {
                    timestamp.setVisibility(View.GONE);
                } else {
                    timestamp.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
