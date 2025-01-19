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
import com.example.trackticum.models.Announcements;
import com.example.trackticum.models.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    Context context;
    private List<Notification> notificationList;
    private NotificationAdapter.NotificationActions actions;

    public NotificationAdapter(Context context, List<Notification> notificationList, NotificationAdapter.NotificationActions actions) {
        this.context = context;
        this.notificationList = notificationList;
        this.actions = actions;
    }

    public interface NotificationActions {
        void onClick(String type);
    }

    @NonNull
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationAdapter.NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        String notificationId = notification.getNotificationId();
        String name = notification.getSenderName();
        String message = notification.getMessage();
        String type = notification.getType();
        String date = notification.getDate();
        String isRead = notification.getIsRead();

        holder.senderName.setText(!name.equals("null") ? name : "");
        holder.message.setText(message);
        holder.date.setText(date);

        holder.unReadIcon.setVisibility(isRead.equals("0") ? View.VISIBLE : View.GONE);

        holder.notifContainer.setOnClickListener(v ->
                actions.onClick(type)
        );
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {

        CardView notifContainer;
        ImageView notifIcon;
        View unReadIcon;
        TextView senderName, message, date;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notifContainer = itemView.findViewById(R.id.notif_container);
            notifIcon = itemView.findViewById(R.id.notification_icon_iv);
            unReadIcon = itemView.findViewById(R.id.unread_badge);
            senderName = itemView.findViewById(R.id.sender_name_tv);
            message = itemView.findViewById(R.id.notification_message);
            date = itemView.findViewById(R.id.notification_date);
        }
    }
}
