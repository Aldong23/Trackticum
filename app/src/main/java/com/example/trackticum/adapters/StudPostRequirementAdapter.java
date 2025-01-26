package com.example.trackticum.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.R;
import com.example.trackticum.models.StudPostRequirement;

import java.util.List;

public class StudPostRequirementAdapter extends RecyclerView.Adapter<StudPostRequirementAdapter.StudPostRequirementViewHolder> {

    Context context;
    private List<StudPostRequirement> requirementList;
    private StudPostRequirementAdapter.StudPostRequirementActions actions;

    public StudPostRequirementAdapter(Context context, List<StudPostRequirement> requirementList, StudPostRequirementActions actions) {
        this.context = context;
        this.requirementList = requirementList;
        this.actions = actions;
    }

    public interface StudPostRequirementActions {
        void onViewRequirement(String postReqId);
    }

    @NonNull
    @Override
    public StudPostRequirementAdapter.StudPostRequirementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_requirements, parent, false);
        return new StudPostRequirementAdapter.StudPostRequirementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudPostRequirementAdapter.StudPostRequirementViewHolder holder, int position) {
        StudPostRequirement studRequirement = requirementList.get(position);
        String postReqID = studRequirement.getPostReqId();
        String postReqTitle = studRequirement.getPostReqTitle();
        String postReqFileId = studRequirement.getPostReqFileId();

        holder.reqTitleTV.setText(postReqTitle);
        if (!postReqFileId.equalsIgnoreCase("null")) {
            holder.statusIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_circle_mark));
        }else{
            holder.statusIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_circle_mark));
        }

        holder.reqTitleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onViewRequirement(postReqID);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requirementList.size();
    }

    public static class StudPostRequirementViewHolder extends RecyclerView.ViewHolder {

        ImageView statusIV;
        TextView reqTitleTV;

        public StudPostRequirementViewHolder(@NonNull View itemView) {
            super(itemView);

            statusIV = itemView.findViewById(R.id.status_IV);
            reqTitleTV = itemView.findViewById(R.id.requirement_title_TV);
        }
    }
}
