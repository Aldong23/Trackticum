package com.example.trackticum.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.R;
import com.example.trackticum.models.ComConversation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ComConversationAdapter extends RecyclerView.Adapter<ComConversationAdapter.ComConversationViewHolder>  {

    Context context;
    private List<ComConversation> conversationList;
    private ComConversationAdapter.ComConversationAction actions;

    public ComConversationAdapter(Context context, List<ComConversation> conversationList, ComConversationAction actions) {
        this.context = context;
        this.conversationList = conversationList;
        this.actions = actions;
    }

    public interface ComConversationAction {
        void onViewConversation(String messageId, String studId, String studName);
    }

    @NonNull
    @Override
    public ComConversationAdapter.ComConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_student, parent, false);
        return new ComConversationAdapter.ComConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComConversationAdapter.ComConversationViewHolder holder, int position) {
        ComConversation conversation = conversationList.get(position);
        String messageId = conversation.getMessageID();
        String studentId = conversation.getStudID();
        String name = conversation.getName();
        String lastMessage = conversation.getLastMessage();
        String image = conversation.getImage();
        String seen = conversation.getSeen();

        if (!image.isEmpty()) {
            Picasso.get()
                    .load(image)
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .resize(500, 500)
                    .centerCrop()
                    .into(holder.image);
        }

        holder.seen.setVisibility(seen.equals("0") ? View.VISIBLE : View.GONE);

        holder.name.setText(name);
        holder.lastMessage.setText(lastMessage);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onViewConversation(messageId, studentId, name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public static class ComConversationViewHolder extends RecyclerView.ViewHolder {

        CardView container;
        View seen;
        ImageView image;
        TextView name, lastMessage;

        public ComConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.conversationContainer);
            seen = itemView.findViewById(R.id.unread_badge);
            image = itemView.findViewById(R.id.studentImageView);
            name = itemView.findViewById(R.id.StudentNameTextView);
            lastMessage = itemView.findViewById(R.id.lastMessageTextView);
        }
    }
}
