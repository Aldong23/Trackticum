package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.adapters.StudPreRequirementAdapter;
import com.example.trackticum.adapters.StudRequirementAdapter;
import com.example.trackticum.models.StudPreRequirement;
import com.example.trackticum.models.StudRequirement;
import com.example.trackticum.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StudPreRequirements extends AppCompatActivity implements StudPreRequirementAdapter.StudPreRequirementActions {

    //For Action bar
    private Toolbar toolbar;

    ProgressDialog progressDialog;

    LottieAnimationView emptyLottie;

    private RecyclerView recyclerView;
    private StudPreRequirementAdapter adapter;
    private List<StudPreRequirement> studRequirementList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stud_pre_requirements);
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
        toolbar = findViewById(R.id.stud_pre_requirements_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pre Requirements");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);

        recyclerView = findViewById(R.id.stud_pre_requirements_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        emptyLottie = findViewById(R.id.empty_list_lottie);
        emptyLottie.setVisibility(View.GONE);

        studRequirementList = new ArrayList<>();
        adapter = new StudPreRequirementAdapter(this, studRequirementList, this);
        recyclerView.setAdapter(adapter);

        fetchStudentRequirement();
    }

    private void setupListeners() {

    }

    private void fetchStudentRequirement() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String stud_id = sharedPreferences.getString("stud_id", null);
        String dep_id = sharedPreferences.getString("dep_id", null);
        String syID = sharedPreferences.getString("sy_id", null);
        String url = Constants.API_BASE_URL + "/get-pre-requirements/" + stud_id + "/" + dep_id + "/" + syID;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        studRequirementList.clear();
                        if (response != null && response.length() > 0){
                            emptyLottie.setVisibility(View.GONE);
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject obj = response.getJSONObject(i);

                                    String preReqId = obj.getString("pre_req_id");
                                    String preReTitle = obj.getString("pre_req_title");
                                    String preReqFileId = obj.getString("pre_req_file_id");

                                    studRequirementList.add(new StudPreRequirement(preReqId, preReTitle, preReqFileId));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            emptyLottie.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(StudPreRequirements.this, "Failed to fetch requirements", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsonArrayRequest);
    }

    @Override
    public void onViewRequirement(String preReqId) {
        Intent intent = new Intent(StudPreRequirements.this, StudViewPreRequirement.class);
        intent.putExtra("pre_file_id", preReqId);
        startActivity(intent);
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

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        boolean refreshRequirements = prefs.getBoolean("refreshPreRequirements", false);

        if(refreshRequirements){
            fetchStudentRequirement();
            prefs.edit().putBoolean("refreshPreRequirements", false).apply();
        }
    }

}