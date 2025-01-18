package com.example.trackticum.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.R;
import com.example.trackticum.models.Activities;

import java.util.List;

public class ComActivityAdapter extends RecyclerView.Adapter<ComActivityAdapter.ComActivityViewHolder> {

    Context context;
    private List<Activities> activitiesList;

    public ComActivityAdapter(Context context, List<Activities> activitiesList) {
        this.context = context;
        this.activitiesList = activitiesList;
    }

    @NonNull
    @Override
    public ComActivityAdapter.ComActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_com_activites, parent, false);
        return new ComActivityAdapter.ComActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComActivityAdapter.ComActivityViewHolder holder, int position) {
        Activities activity = activitiesList.get(position);
        String activity_id = activity.getActivityId();
        String date = activity.getDate();
        String activities = activity.getActivity();

        String[] lines = activities.split("\\n");  // Split by newline
        StringBuilder bulletedActivities = new StringBuilder();
        String bullet = holder.itemView.getContext().getString(R.string.bullet);
        for (String line : lines) {
            bulletedActivities.append(bullet).append(" ").append(line).append("\n");
        }

        holder.dateTextView.setText(date);
        holder.activityTextView.setText(bulletedActivities.toString().trim());
    }

    @Override
    public int getItemCount() {
        return activitiesList.size();
    }

    public static class ComActivityViewHolder extends RecyclerView.ViewHolder  {
        TextView dateTextView, activityTextView;
        public ComActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_tv);
            activityTextView = itemView.findViewById(R.id.activity_tv);
        }
    }
}
