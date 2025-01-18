package com.example.trackticum.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.R;
import com.example.trackticum.models.Activities;
import com.example.trackticum.models.Announcements;

import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {

    Context context;
    private List<Announcements> announcementsList;
    private AnnouncementAdapter.AnnouncementActions actions;

    public AnnouncementAdapter(Context context, List<Announcements> announcementsList, AnnouncementActions actions) {
        this.context = context;
        this.announcementsList = announcementsList;
        this.actions = actions;
    }

    public interface AnnouncementActions {
        void onView(String announcementId);
    }

    @NonNull
    @Override
    public AnnouncementAdapter.AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stud_announcement, parent, false);
        return new AnnouncementAdapter.AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementAdapter.AnnouncementViewHolder holder, int position) {
        Announcements announcements = announcementsList.get(position);
        String announcementId = announcements.getAnnouncementId();
        String title = announcements.getTitle();
        String message = announcements.getMessage();
        String date = announcements.getDate();

        holder.annTiltle.setText(title);
        holder.annDate.setText(date);
        holder.annMessage.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));

        holder.annContainer.setOnClickListener(v ->
                actions.onView(announcementId)
        );
    }

    @Override
    public int getItemCount() {
        return announcementsList.size();
    }

    public static class AnnouncementViewHolder extends RecyclerView.ViewHolder {

        CardView annContainer;
        TextView annTiltle, annDate, annMessage;

        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            annContainer = itemView.findViewById(R.id.ann_container);
            annTiltle = itemView.findViewById(R.id.ann_title);
            annDate = itemView.findViewById(R.id.ann_date);
            annMessage = itemView.findViewById(R.id.ann_message);
        }
    }
}
