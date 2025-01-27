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
import com.example.trackticum.models.ComCoordinatorConversation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ComCoordinatorConversationAdapter extends RecyclerView.Adapter<ComCoordinatorConversationAdapter.ViewHolder>  {

    Context context;
    private List<ComCoordinatorConversation> conversationList;
    private ComCoordinatorConversationAdapter.ComCoordinatorConversationAction actions;

    public ComCoordinatorConversationAdapter(Context context, List<ComCoordinatorConversation> conversationList, ComCoordinatorConversationAction actions) {
        this.context = context;
        this.conversationList = conversationList;
        this.actions = actions;
    }

    public interface ComCoordinatorConversationAction {
        void onViewConversation(String messageId, String userId, String userName);
    }

    @NonNull
    @Override
    public ComCoordinatorConversationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_coordinator, parent, false);
        return new ComCoordinatorConversationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComCoordinatorConversationAdapter.ViewHolder holder, int position) {
        ComCoordinatorConversation conversation = conversationList.get(position);
        String messageId = conversation.getMessageID();
        String userId = conversation.getUserID();
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
                actions.onViewConversation(messageId, userId, name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView container;
        View seen;
        ImageView image;
        TextView name, lastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.conversationContainer);
            seen = itemView.findViewById(R.id.unread_badge);
            image = itemView.findViewById(R.id.imageView);
            name = itemView.findViewById(R.id.NameTextView);
            lastMessage = itemView.findViewById(R.id.lastMessageTextView);
        }
    }
}
