package com.example.trackticum.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.R;
import com.example.trackticum.models.Activities;
import com.example.trackticum.models.WeeklyReport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>  {

    Context context;
    private List<Activities> activitiesList;
    private ActivityAdapter.ActivityActions actions;

    public ActivityAdapter(Context context, List<Activities> activitiesList, ActivityAdapter.ActivityActions actions) {
        this.context = context;
        this.activitiesList = activitiesList;
        this.actions = actions;
    }

    public interface ActivityActions {
        void onEditActivity(String activityId, String date, String activities);
        void onDeleteActivity(String activityId);
    }

    @NonNull
    @Override
    public ActivityAdapter.ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activities, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityAdapter.ActivityViewHolder holder, int position) {
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

        // Click events for edit and delete
        holder.editButton.setOnClickListener(v ->
                actions.onEditActivity(activity_id, formatDate(date), activities)
        );

        holder.deleteButton.setOnClickListener(v ->
                actions.onDeleteActivity(activity_id)
        );
    }

    public static String formatDate(String inputDate) {
        try {
            // Parse the input date
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
            Date date = inputFormat.parse(inputDate);

            // Format the date to desired output as MM/dd/yyyy
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null if parsing fails
        }
    }

    @Override
    public int getItemCount() {
        return activitiesList.size();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout activityContainer;
        TextView dateTextView, activityTextView;
        ImageButton editButton, deleteButton;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            activityContainer = itemView.findViewById(R.id.activity_containe);
            dateTextView = itemView.findViewById(R.id.date_tv);
            activityTextView = itemView.findViewById(R.id.activity_tv);
            editButton = itemView.findViewById(R.id.edit_btn);
            deleteButton = itemView.findViewById(R.id.delete_btn);
        }
    }
}
