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
import com.example.trackticum.models.StudPreRequirement;
import com.example.trackticum.models.StudRequirement;

import java.util.List;

public class StudPreRequirementAdapter extends RecyclerView.Adapter<StudPreRequirementAdapter.StudPreRequirementViewHolder> {

    Context context;
    private List<StudPreRequirement> studPreRequirementList;
    private StudPreRequirementAdapter.StudPreRequirementActions actions;

    public StudPreRequirementAdapter(Context context, List<StudPreRequirement> studPreRequirementList, StudPreRequirementAdapter.StudPreRequirementActions actions) {
        this.context = context;
        this.studPreRequirementList = studPreRequirementList;
        this.actions = actions;
    }

    public interface StudPreRequirementActions {
        void onViewRequirement(String preReqId);
    }

    @NonNull
    @Override
    public StudPreRequirementAdapter.StudPreRequirementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_requirements, parent, false);
        return new StudPreRequirementAdapter.StudPreRequirementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudPreRequirementAdapter.StudPreRequirementViewHolder holder, int position) {
        StudPreRequirement studRequirement = studPreRequirementList.get(position);
        String preReqID = studRequirement.getPreReqId();
        String preReqTitle = studRequirement.getPreReTitle();
        String preReqFileId = studRequirement.getPreReqFileId();

        holder.reqTitleTV.setText(preReqTitle);
        if (!preReqFileId.equalsIgnoreCase("null")) {
            holder.statusIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_circle_mark));
        }else{
            holder.statusIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_circle_mark));
        }

        holder.reqTitleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onViewRequirement(preReqID);
            }
        });
    }

    @Override
    public int getItemCount() {
        return studPreRequirementList.size();
    }

    public class StudPreRequirementViewHolder extends RecyclerView.ViewHolder {

        ImageView statusIV;
        TextView reqTitleTV;

        public StudPreRequirementViewHolder(@NonNull View itemView) {
            super(itemView);

            statusIV = itemView.findViewById(R.id.status_IV);
            reqTitleTV = itemView.findViewById(R.id.requirement_title_TV);
        }
    }
}
