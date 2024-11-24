package com.example.trackticum.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.R;
import com.example.trackticum.models.JobOffer;

import java.util.List;

public class JobOfferAdapter extends RecyclerView.Adapter<JobOfferAdapter.JobOfferViewHolder> {

    // Listener interface defined inside the adapter
    public interface OnJobOfferClickListener {
        void onEditClick(JobOffer jobOffer);  // Handle edit action
        void onDeleteClick(JobOffer jobOffer); // Handle delete action
    }

    private List<JobOffer> jobOfferList;
    private OnJobOfferClickListener listener;

    public JobOfferAdapter(List<JobOffer> jobOfferList, OnJobOfferClickListener listener) {
        this.jobOfferList = jobOfferList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public JobOfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_offer, parent, false);
        return new JobOfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobOfferViewHolder holder, int position) {
        JobOffer jobOffer = jobOfferList.get(position);
        holder.jobTitle.setText(jobOffer.getJobTitle());
        // Set click listener for edit and delete
        holder.itemView.setOnClickListener(v -> listener.onEditClick(jobOffer));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDeleteClick(jobOffer);
            return true; // Return true to indicate the long click was handled
        });
    }

    @Override
    public int getItemCount() {
        return jobOfferList.size();
    }

    public static class JobOfferViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle;

        public JobOfferViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.job_title_tv);
        }
    }
}
