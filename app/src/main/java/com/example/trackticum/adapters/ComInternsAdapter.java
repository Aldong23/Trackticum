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
import com.example.trackticum.models.ComInterns;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ComInternsAdapter extends RecyclerView.Adapter<ComInternsAdapter.ComInternsViewHolder> {

    Context context;
    private List<ComInterns> internsList;
    private ComInternsAdapter.ComInternsAction actions;

    public ComInternsAdapter(Context context, List<ComInterns> internsList, ComInternsAdapter.ComInternsAction actions) {
        this.context = context;
        this.internsList = internsList;
        this.actions = actions;
    }

    public interface ComInternsAction {
        void onViewInterns(String studID);
    }

    @NonNull
    @Override
    public ComInternsAdapter.ComInternsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_com_interns, parent, false);
        return new ComInternsAdapter.ComInternsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComInternsAdapter.ComInternsViewHolder holder, int position) {
        ComInterns interns = internsList.get(position);
        String studID = interns.getStudID();
        String studName = interns.getName();
        String studDepartment = interns.getDepartment();
        String image = interns.getImage();

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

        holder.internsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onViewInterns(studID);
            }
        });
    }

    @Override
    public int getItemCount() {
        return internsList.size();
    }

    public static class ComInternsViewHolder extends RecyclerView.ViewHolder {
        CardView internsContainer;
        RoundedImageView studImage;
        TextView studName, studDepartment;

        public ComInternsViewHolder(@NonNull View itemView) {
            super(itemView);
            internsContainer = itemView.findViewById(R.id.internsContainer);
            studImage = itemView.findViewById(R.id.studentImageView);
            studName = itemView.findViewById(R.id.StudentNameTextView);
            studDepartment = itemView.findViewById(R.id.departmentTextView);
        }
    }
}
