package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.adapters.WeeklyReportAdapter;
import com.example.trackticum.models.WeeklyReport;
import com.example.trackticum.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ComStudWeekly extends AppCompatActivity implements WeeklyReportAdapter.WeeklyReportActions {

    private Toolbar toolbar;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;

    RecyclerView recyclerView;
    private WeeklyReportAdapter adapter;
    private List<WeeklyReport> weeklyReportList;

    String studID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_com_stud_weekly);
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
        toolbar = findViewById(R.id.stud_weekly_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Weekly Report");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        studID = getIntent().getStringExtra("stud_id");

        recyclerView = findViewById(R.id.weekly_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        weeklyReportList = new ArrayList<>();
        adapter = new WeeklyReportAdapter(this, weeklyReportList, this);
        recyclerView.setAdapter(adapter);

        fetchWeekly();

    }

    private void setupListeners() {

    }

    private void fetchWeekly() {
        String url = Constants.API_BASE_URL + "/student/get-weekly/" + studID;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    weeklyReportList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String weeklyID = obj.getString("id");
                            String stud_id = obj.getString("student_id");
                            String title = obj.getString("title");
                            String evaluation = obj.getString("evaluation");
                            String supervisorComment = obj.getString("supervisor_comment");
                            String isSigned = obj.getString("is_signed");
                            String date = obj.getString("created_at");  // Already formatted in API (mm-dd-yyyy)

                            weeklyReportList.add(new WeeklyReport(weeklyID, stud_id, title, evaluation, supervisorComment, isSigned, date));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Failed to fetch weekly", Toast.LENGTH_SHORT).show()
        );

        queue.add(jsonArrayRequest);
    }

    @Override
    public void onViewWeeklyReport(String weeklyId) {
        Intent intent = new Intent(ComStudWeekly.this, ComStudShowWeekly.class);
        intent.putExtra("stud_id", studID);
        intent.putExtra("weekly_id", weeklyId);
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
        boolean refreshWeeklyList = prefs.getBoolean("refreshWeeklyList", false);

        if(refreshWeeklyList){
            fetchWeekly();
            prefs.edit().putBoolean("refreshWeeklyList", false).apply();
        }
    }

}