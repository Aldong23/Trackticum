package com.example.trackticum.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.adapters.ActivityAdapter;
import com.example.trackticum.adapters.AnnouncementAdapter;
import com.example.trackticum.models.Activities;
import com.example.trackticum.models.Announcements;
import com.example.trackticum.utils.Constants;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StudAnnouncementList extends AppCompatActivity implements AnnouncementAdapter.AnnouncementActions {

    private Toolbar toolbar;

    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    RecyclerView recyclerView;

    String studID;
    private AnnouncementAdapter adapter;
    private List<Announcements> announcementsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stud_announcement_list);
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
        toolbar = findViewById(R.id.stud_announcement_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Announcement");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        studID = sharedPreferences.getString("stud_id", null);

        recyclerView = findViewById(R.id.announcement_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        announcementsList = new ArrayList<>();
        adapter = new AnnouncementAdapter(this, announcementsList, this);
        recyclerView.setAdapter(adapter);

        fetchAnnouncements();
    }



    private void setupListeners() {

    }

    private void fetchAnnouncements() {
        String depID = sharedPreferences.getString("dep_id", null);
        String syID = sharedPreferences.getString("sy_id", null);

        String url = Constants.API_BASE_URL + "/announcement/get-announcement-list/" + depID + "/" + syID;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    announcementsList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String announcementID = obj.getString("id");
                            String title = obj.getString("title");
                            String date = obj.getString("date");
                            String message = obj.getString("message");

                            announcementsList.add(new Announcements(announcementID, title, message, date));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Failed to fetch activities", Toast.LENGTH_SHORT).show()
        );

        queue.add(jsonArrayRequest);
    }

    @Override
    public void onView(String announcementId) {
        Intent intent = new Intent(this, StudViewAnnouncement.class);
        intent.putExtra("announcement_id", announcementId);
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