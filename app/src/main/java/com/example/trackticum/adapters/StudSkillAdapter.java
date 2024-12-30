package com.example.trackticum.adapters;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackticum.R;
import com.example.trackticum.models.StudSkill;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class StudSkillAdapter extends RecyclerView.Adapter<StudSkillAdapter.StudSkillsViewHolder> {

    Context context;
    private List<StudSkill> studSkillList;
    private StudSkillAdapter.StudSkillActions actions;

    public StudSkillAdapter(Context context, List<StudSkill> studSkillList, StudSkillAdapter.StudSkillActions actions) {
        this.context = context;
        this.studSkillList = studSkillList;
        this.actions = actions;
    }

    public interface StudSkillActions {
        void onDeleteSkill(int skillID);
        void onEditSkill(String skillTitle, int skillID);
    }

    @NonNull
    @Override
    public StudSkillAdapter.StudSkillsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stud_skill, parent, false);
        return new StudSkillAdapter.StudSkillsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudSkillAdapter.StudSkillsViewHolder holder, int position) {
        StudSkill studSkill = studSkillList.get(position);
        holder.skillTitle.setText(studSkill.getSkillTitle());

        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View dialogView = layoutInflater.inflate(R.layout.dialog_add_studskill, null);

                TextInputEditText editTextSkill = dialogView.findViewById(R.id.skill_title_et);
                editTextSkill.setText(studSkill.getSkillTitle());

                // Create the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Edit Skill");
                builder.setView(dialogView); // Set the custom layout

                // Set the positive button
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String skillTitle = editTextSkill.getText().toString().trim();

                        if (!skillTitle.isEmpty()) {
                            actions.onEditSkill(skillTitle, studSkill.getId());
                        } else {
                            Toast.makeText(context, "Please input skill title!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Set the negative button
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Close the dialog
                    }
                });

                // Show the dialog
                builder.create().show();
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Job Offer")
                        .setMessage("Are you sure you want to delete this job offer?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Perform delete action here (e.g., send API request to delete)
                            actions.onDeleteSkill(studSkill.getId());
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return studSkillList.size();
    }

    public class StudSkillsViewHolder extends RecyclerView.ViewHolder {
        TextView skillTitle;
        ImageButton editBtn, deleteBtn;

        public StudSkillsViewHolder(@NonNull View itemView) {
            super(itemView);
            skillTitle = itemView.findViewById(R.id.skill_title_tv);
            editBtn = itemView.findViewById(R.id.edit_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }
    }
}
