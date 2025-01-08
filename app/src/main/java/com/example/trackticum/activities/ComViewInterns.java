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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.adapters.ComApplicantsAdapter;
import com.example.trackticum.adapters.ComInternsAdapter;
import com.example.trackticum.models.ComApplicants;
import com.example.trackticum.models.ComInterns;
import com.example.trackticum.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ComViewInterns extends AppCompatActivity implements ComInternsAdapter.ComInternsAction {

    private Toolbar toolbar;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;

    private LottieAnimationView emptyLottie;
    private RecyclerView recyclerView;
    private ComInternsAdapter adapter;
    private List<ComInterns> internsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_com_view_interns);
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
        toolbar = findViewById(R.id.com_view_interns_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Interns");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        emptyLottie = findViewById(R.id.empty_lottie);
        recyclerView = findViewById(R.id.com_interns_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        internsList = new ArrayList<>();
        adapter = new ComInternsAdapter(this, internsList, this);
        recyclerView.setAdapter(adapter);

        fetchInterns();
    }


    private void setupListeners() {

    }

    private void fetchInterns() {
        String comId = sharedPreferences.getString("com_id", null);
        String url = Constants.API_BASE_URL + "/company/get-interns/" + comId;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response != null && response.length() > 0) {
                        emptyLottie.setVisibility(View.GONE);
                        internsList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);

                                String studID = obj.getString("stud_id");
                                String studName = obj.getString("stud_name");
                                String department = obj.getString("department");
                                String studImage = obj.getString("stud_image");
                                String imageUrl = Constants.API_BASE_URL + "/" + studImage;

                                internsList.add(new ComInterns(studID, studName, department, imageUrl));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        emptyLottie.setVisibility(View.VISIBLE);
                        internsList.clear();
                        adapter.notifyDataSetChanged();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch interns", Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonArrayRequest);
    }

    @Override
    public void onViewInterns(String studID) {
        Intent intent = new Intent(this, ComShowIntern.class);
        intent.putExtra("stud_id", studID);
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

}