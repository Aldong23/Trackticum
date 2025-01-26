package com.example.trackticum.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.R;
import com.example.trackticum.models.ComCoordinator;
import com.example.trackticum.models.ComInterns;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ComCoordinatorsAdapter extends RecyclerView.Adapter<ComCoordinatorsAdapter.ComCoordinatorsViewHolder> {

    Context context;
    private List<ComCoordinator> coordinatorList;
    private ComCoordinatorsAdapter.ComCoordinatorsAction actions;

    public ComCoordinatorsAdapter(Context context, List<ComCoordinator> coordinatorList, ComCoordinatorsAction actions) {
        this.context = context;
        this.coordinatorList = coordinatorList;
        this.actions = actions;
    }

    public interface ComCoordinatorsAction {
        void onViewCoordinator(String coordinatorId, String coordinatorName);
    }

    @NonNull
    @Override
    public ComCoordinatorsAdapter.ComCoordinatorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_com_coordinators, parent, false);
        return new ComCoordinatorsAdapter.ComCoordinatorsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComCoordinatorsAdapter.ComCoordinatorsViewHolder holder, int position) {
        ComCoordinator coordinator = coordinatorList.get(position);
        String coordinatorId = coordinator.getCoordinatorId();
        String name = coordinator.getName();
        String department = coordinator.getDepartment();
        String image = coordinator.getImage();

        holder.nameTV.setText(name);
        holder.departmentTV.setText(department);

        Picasso.get()
                .load(image)
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_placeholder)
                .resize(500, 500)
                .centerCrop()
                .into(holder.imageView);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actions.onViewCoordinator(coordinatorId, name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return coordinatorList.size();
    }

    public static class ComCoordinatorsViewHolder extends RecyclerView.ViewHolder {

        CardView container;
        RoundedImageView imageView;
        TextView nameTV, departmentTV;

        public ComCoordinatorsViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            imageView = itemView.findViewById(R.id.imageView);
            nameTV = itemView.findViewById(R.id.NameTextView);
            departmentTV = itemView.findViewById(R.id.departmentTextView);
        }
    }
}
