package com.example.trackticum.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.R;
import com.example.trackticum.models.AnnAttachment;
import com.example.trackticum.models.Announcements;

import java.util.List;

public class AnnAttachmentAdapter extends RecyclerView.Adapter<AnnAttachmentAdapter.AnnAttachmentViewHolder> {

    Context context;
    private List<AnnAttachment> attachmentList;
    private AnnAttachmentAdapter.AnnAttachmentActions actions;

    public AnnAttachmentAdapter(Context context, List<AnnAttachment> attachmentList, AnnAttachmentActions actions) {
        this.context = context;
        this.attachmentList = attachmentList;
        this.actions = actions;
    }

    public interface AnnAttachmentActions {
        void onDownload(String attachmentID);
    }

    @NonNull
    @Override
    public AnnAttachmentAdapter.AnnAttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ann_attachment, parent, false);
        return new AnnAttachmentAdapter.AnnAttachmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnAttachmentAdapter.AnnAttachmentViewHolder holder, int position) {
        AnnAttachment attachment = attachmentList.get(position);
        String attachmentId = attachment.getId();
        String file = attachment.getFile();

        holder.filename.setText(file);

        holder.download.setOnClickListener(v ->
                actions.onDownload(attachmentId)
        );
    }

    @Override
    public int getItemCount() {
        return attachmentList.size();
    }

    public static class AnnAttachmentViewHolder extends RecyclerView.ViewHolder {

        TextView filename;
        Button download;

        public AnnAttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            filename = itemView.findViewById(R.id.requirement_title_TV);
            download = itemView.findViewById(R.id.download_btn);
        }
    }
}
