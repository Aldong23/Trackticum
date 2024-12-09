package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.adapters.JobOfferAdapter;
import com.example.trackticum.models.JobOffer;
import com.example.trackticum.utils.Constants;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComManageJoboffer extends AppCompatActivity {

    //For Action bar
    private Toolbar toolbar;

    private ExtendedFloatingActionButton addJobOffer;
    ProgressDialog progressDialog;

    //fetching job offer
    private RecyclerView recyclerView;
    private JobOfferAdapter adapter;
    private List<JobOffer> jobOfferList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_com_manage_joboffer);
        setupWindowInsets();
        //setting up the status bar
        getWindow().setStatusBarColor(getResources().getColor(R.color.deepTeal));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().getInsetsController().setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(0); // Clear the flag for light status bar
        }

        //Add code here
        initializeData();
        setupListeners();

    }

    private void initializeData() {
        //set up action bar
        toolbar = findViewById(R.id.com_manage_joboffer_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Manage Job Offer");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        addJobOffer = findViewById(R.id.add_joboffer_btn);
        progressDialog = new ProgressDialog(this);

        //fetching job offer
        recyclerView = findViewById(R.id.joboffer_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        jobOfferList = new ArrayList<>();
        adapter = new JobOfferAdapter(this, jobOfferList);
        recyclerView.setAdapter(adapter);
        fetchJobOffers();
    }

    private void setupListeners() {
        addJobOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = LayoutInflater.from(ComManageJoboffer.this);
                View dialogView = inflater.inflate(R.layout.dialog_add_job, null);

                TextInputEditText editTextJobTitle = dialogView.findViewById(R.id.job_title_et);

                // Create the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ComManageJoboffer.this);
                builder.setTitle("Add Job Offer");
                builder.setView(dialogView); // Set the custom layout

                // Set the positive button
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String jobTitle = editTextJobTitle.getText().toString().trim();

                        if (!jobTitle.isEmpty()) {
                            // Handle adding the job offer here
                            addJobTitleToDB(jobTitle);
                        } else {
                            Toast.makeText(ComManageJoboffer.this, "Please input job title!", Toast.LENGTH_SHORT).show();
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
    }

    private void addJobTitleToDB(String jobTitle) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String comId = sharedPreferences.getString("com_id", null);

        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Adding");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/company/add-job-title";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(ComManageJoboffer.this, response, Toast.LENGTH_LONG).show();
                    fetchJobOffers();
                }, error -> {
            progressDialog.dismiss();
            Toast.makeText(ComManageJoboffer.this, "Failed to add", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("company_id", comId);
                params.put("job_title", jobTitle);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    private void fetchJobOffers() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String comId = sharedPreferences.getString("com_id", null);
        String url = Constants.API_BASE_URL + "/company/get-job-offers/" + comId;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        jobOfferList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                int jobOfferId = obj.getInt("id");
                                String jobTitle = obj.getString("job_title");

                                jobOfferList.add(new JobOffer(jobOfferId, jobTitle));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ComManageJoboffer.this, "Failed to fetch job offers", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsonArrayRequest);
    }


    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}