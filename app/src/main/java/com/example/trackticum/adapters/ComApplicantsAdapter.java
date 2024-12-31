package com.example.trackticum.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.R;
import com.example.trackticum.models.ComApplicants;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ComApplicantsAdapter extends RecyclerView.Adapter<ComApplicantsAdapter.ComApplicantsViewHolder> {

    Context context;
    private List<ComApplicants> applicantsList;
    private ComApplicantsAdapter.ComApplicantsAction actions;

    public ComApplicantsAdapter(Context context, List<ComApplicants> applicantsList, ComApplicantsAction actions) {
        this.context = context;
        this.applicantsList = applicantsList;
        this.actions = actions;
    }

    public interface ComApplicantsAction {
        void onViewApplicants(String studID);
        void onDeclineApplicants(String applicationID);
        void onAcceptApplicants(String studID);
    }

    @NonNull
    @Override
    public ComApplicantsAdapter.ComApplicantsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_com_applicants, parent, false);
        return new ComApplicantsAdapter.ComApplicantsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComApplicantsAdapter.ComApplicantsViewHolder holder, int position) {
        ComApplicants applicants = applicantsList.get(position);
        String applicationID = applicants.getApplicationID();
        String studID = applicants.getStudID();
        String studName = applicants.getName();
        String studDepartment = applicants.getDepartment();
        String image = applicants.getImage();

        if (!image.isEmpty()) {
            Picasso.get()
                    .load(image)
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .resize(500, 500)
                    .centerCrop()
                    .into(holder.studImage);
        }

        holder.studName.setText(studName);
        holder.studDepartment.setText(studDepartment);

        holder.applicantsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onViewApplicants(studID);
            }
        });

        holder.decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onDeclineApplicants(applicationID);
            }
        });

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onAcceptApplicants(studID);
            }
        });
    }

    @Override
    public int getItemCount() {
        return applicantsList.size();
    }

    public static class ComApplicantsViewHolder extends RecyclerView.ViewHolder {

        CardView applicantsContainer;
        RoundedImageView studImage;
        TextView studName, studDepartment;
        ImageButton decline, accept;

        public ComApplicantsViewHolder(@NonNull View itemView) {
            super(itemView);
            applicantsContainer = itemView.findViewById(R.id.applicantsContainer);
            studImage = itemView.findViewById(R.id.studentImageView);
            studName = itemView.findViewById(R.id.StudentNameTextView);
            studDepartment = itemView.findViewById(R.id.departmentTextView);
            decline = itemView.findViewById(R.id.declineButton);
            accept = itemView.findViewById(R.id.acceptButton);
        }
    }
}
