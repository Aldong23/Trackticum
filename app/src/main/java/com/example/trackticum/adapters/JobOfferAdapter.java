package com.example.trackticum.adapters;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.activities.ComManageJoboffer;
import com.example.trackticum.models.JobOffer;
import com.example.trackticum.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class JobOfferAdapter extends RecyclerView.Adapter<JobOfferAdapter.JobOfferViewHolder> {

    Context context;
    private List<JobOffer> jobOfferList;

    public JobOfferAdapter(Context context, List<JobOffer> jobOfferList) {
        this.context = context;
        this.jobOfferList = jobOfferList;
    }

    @NonNull
    @Override
    public JobOfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_offer, parent, false);
        return new JobOfferViewHolder(view);
    }

    ProgressDialog progressDialog;

    @Override
    public void onBindViewHolder(@NonNull JobOfferViewHolder holder, int position) {
        JobOffer jobOffer = jobOfferList.get(position);
        holder.jobTitle.setText(jobOffer.getJobTitle());

        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View dialogView = layoutInflater.inflate(R.layout.dialog_add_job, null);

                TextInputEditText editTextJobTitle = dialogView.findViewById(R.id.job_title_et);
                editTextJobTitle.setText(jobOffer.getJobTitle());

                // Create the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Edit Job Offer");
                builder.setView(dialogView); // Set the custom layout

                // Set the positive button
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String jobTitle = editTextJobTitle.getText().toString().trim();

                        if (!jobTitle.isEmpty()) {
                            saveJobOfferToDB(jobTitle, jobOffer.getId());
                        } else {
                            Toast.makeText(context, "Please input job title!", Toast.LENGTH_SHORT).show();
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
                            deleteJobOffer(jobOffer.getId());
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

    }

    private void deleteJobOffer(int id) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Deleting");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String url = Constants.API_BASE_URL + "/company/delete-job-offers/" + id;
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Job offer deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed to delete job offer", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(stringRequest);
    }

    private void saveJobOfferToDB(String jobTitle, int id) {
        Toast.makeText(context, jobTitle + " " + String.valueOf(id), Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return jobOfferList.size();
    }

    public static class JobOfferViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle;
        ImageButton editBtn, deleteBtn;

        public JobOfferViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.job_title_tv);
            editBtn = itemView.findViewById(R.id.edit_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }
    }
}
