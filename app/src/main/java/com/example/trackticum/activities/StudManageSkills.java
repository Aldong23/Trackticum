package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.example.trackticum.adapters.StudSkillAdapter;
import com.example.trackticum.models.JobOffer;
import com.example.trackticum.models.StudSkill;
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

public class StudManageSkills extends AppCompatActivity implements StudSkillAdapter.StudSkillActions {

    //For Action bar
    private Toolbar toolbar;

    private ExtendedFloatingActionButton addSkill;
    ProgressDialog progressDialog;

    //fetching job offer
    private RecyclerView recyclerView;
    private StudSkillAdapter adapter;
    private List<StudSkill> studSkillList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stud_manage_skills);
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
        toolbar = findViewById(R.id.com_manage_skills_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Manage Skills");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        addSkill = findViewById(R.id.add_skills_btn);
        progressDialog = new ProgressDialog(this);

        //fetching job offer
        recyclerView = findViewById(R.id.skills_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        studSkillList = new ArrayList<>();
        adapter = new StudSkillAdapter(this, studSkillList, this);
        recyclerView.setAdapter(adapter);
        fetchSkills();

        //request to refresh profile
        SharedPreferences prefs = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("refreshSkills", true).apply();
    }

    private void setupListeners() {
        addSkill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(StudManageSkills.this);
                View dialogView = inflater.inflate(R.layout.dialog_add_studskill, null);

                TextInputEditText editTextSkill = dialogView.findViewById(R.id.skill_title_et);

                // Create the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(StudManageSkills.this);
                builder.setTitle("Add Skill");
                builder.setView(dialogView); // Set the custom layout

                // Set the positive button
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String skillTitle = editTextSkill.getText().toString().trim();

                        if (!skillTitle.isEmpty()) {
                            // Handle adding the job offer here
                            addSkillToDB(skillTitle);
                        } else {
                            Toast.makeText(StudManageSkills.this, "Please input skill!", Toast.LENGTH_SHORT).show();
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

    private void addSkillToDB(String skillTitle) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String studID = sharedPreferences.getString("stud_id", null);

        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Adding");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/add-skill-title";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(StudManageSkills.this, response, Toast.LENGTH_LONG).show();
                    fetchSkills();
                }, error -> {
            progressDialog.dismiss();
            Toast.makeText(StudManageSkills.this, "Failed to add", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("stud_id", studID);
                params.put("skill_title", skillTitle);
                return params;
            }
        };
        queue.add(stringRequest);
    }


    private void fetchSkills() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String studID = sharedPreferences.getString("stud_id", null);
        String url = Constants.API_BASE_URL + "/student/get-skills/" + studID;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        studSkillList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                int skillID = obj.getInt("id");
                                String skillTitle = obj.getString("name");

                                studSkillList.add(new StudSkill(skillID, skillTitle));
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
                        Toast.makeText(StudManageSkills.this, "Failed to fetch skills", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsonArrayRequest);
    }

    @Override
    public void onDeleteSkill(int skillID) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Deleting");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String url = Constants.API_BASE_URL + "/student/delete-skill/" + skillID;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Toast.makeText(StudManageSkills.this, "Skill deleted successfully", Toast.LENGTH_SHORT).show();
                        fetchSkills();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(StudManageSkills.this, "Failed to delete skill", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(stringRequest);
    }

    @Override
    public void onEditSkill(String skillTitle, int skillID) {
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Saving");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/update-skill";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(StudManageSkills.this, response, Toast.LENGTH_LONG).show();
                    fetchSkills();
                }, error -> {
            progressDialog.dismiss();
            Toast.makeText(StudManageSkills.this, "Failed to update", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("skill_id", String.valueOf(skillID));
                params.put("skill_title", skillTitle);
                return params;
            }
        };
        queue.add(stringRequest);
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