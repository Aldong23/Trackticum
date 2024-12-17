package com.example.trackticum.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.R;
import com.example.trackticum.models.StudRequirement;

import java.util.List;

public class StudRequirementAdapter extends RecyclerView.Adapter<StudRequirementAdapter.StudRequirementViewHolder> {

    Context context;
    private List<StudRequirement> studRequirementList;
    private StudRequirementAdapter.StudRequirementActions actions;

    public StudRequirementAdapter(Context context, List<StudRequirement> studRequirementList, StudRequirementAdapter.StudRequirementActions actions) {
        this.context = context;
        this.studRequirementList = studRequirementList;
        this.actions = actions;
    }

    public interface StudRequirementActions {
        void onViewRequirement(String documentRequirementID);
    }

    @NonNull
    @Override
    public StudRequirementAdapter.StudRequirementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_requirements, parent, false);
        return new StudRequirementAdapter.StudRequirementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudRequirementAdapter.StudRequirementViewHolder holder, int position) {
        StudRequirement studRequirement = studRequirementList.get(position);
        String reqTitle = studRequirement.getDocumentRequirementTitle();
        String documentRequirementID = studRequirement.getDocumentRequirementId();
        String documentBeforeOjtID = studRequirement.getDocumentBeforeOjtId();
        String documentBeforeOjtFile = studRequirement.getDocumentBeforeOjtFile();

        holder.reqTitleTV.setText(reqTitle);
        if (!documentBeforeOjtID.equals("null")) {
            holder.statusIV.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_circle_mark));
        }

        holder.reqTitleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onViewRequirement(documentRequirementID);
            }
        });

    }

    @Override
    public int getItemCount() {
        return studRequirementList.size();
    }

    public static class StudRequirementViewHolder extends RecyclerView.ViewHolder  {
        ImageView statusIV;
        TextView reqTitleTV;

        public StudRequirementViewHolder(@NonNull View itemView) {
            super(itemView);
            statusIV = itemView.findViewById(R.id.status_IV);
            reqTitleTV = itemView.findViewById(R.id.requirement_title_TV);
        }
    }

}
