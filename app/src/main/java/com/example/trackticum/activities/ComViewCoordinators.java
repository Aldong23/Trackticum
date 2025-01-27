package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.adapters.ComCoordinatorConversationAdapter;
import com.example.trackticum.adapters.ComCoordinatorsAdapter;
import com.example.trackticum.models.ComCoordinator;
import com.example.trackticum.models.ComCoordinatorConversation;
import com.example.trackticum.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ComViewCoordinators extends AppCompatActivity implements ComCoordinatorsAdapter.ComCoordinatorsAction, ComCoordinatorConversationAdapter.ComCoordinatorConversationAction {

    private Toolbar toolbar;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;

    private Handler handler = new Handler();
    private Runnable workRunnable;

    private LottieAnimationView emptyLottie;
    private RecyclerView recyclerView;
    private ComCoordinatorsAdapter coordinatorAdapter;
    private List<ComCoordinator> coordinatorList;
    private ComCoordinatorConversationAdapter conversationAdapter;
    private List<ComCoordinatorConversation> conversationList;

    TextInputEditText searchET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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
        toolbar = findViewById(R.id.com_view_coordinators_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Coordinators");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        searchET = findViewById(R.id.search_filter);
        emptyLottie = findViewById(R.id.empty_lottie);
        recyclerView = findViewById(R.id.com_coordinators_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        coordinatorList = new ArrayList<>();
        coordinatorAdapter = new ComCoordinatorsAdapter(this, coordinatorList, this);

        conversationList = new ArrayList<>();
        conversationAdapter = new ComCoordinatorConversationAdapter(this, conversationList, this);

        fetchConversation();
    }

    private void setupListeners() {
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (workRunnable != null) {
                    handler.removeCallbacks(workRunnable); // Cancel any previous scheduled run
                }
                workRunnable = () -> {
                    if (charSequence.length() > 0) {
                        fetchCoordinators(charSequence);
                    } else {
                        fetchConversation();
                    }
                };
                handler.postDelayed(workRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void fetchCoordinators(CharSequence charSequence) {
        recyclerView.setAdapter(coordinatorAdapter);

        String url = Constants.API_BASE_URL + "/company/get-coordinator/" + charSequence;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response != null && response.length() > 0) {
                        emptyLottie.setVisibility(View.GONE);
                        coordinatorList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);

                                String coordinatorId = obj.getString("stud_id");
                                String name = obj.getString("name");
                                String department = obj.getString("department");
                                String image = obj.getString("stud_image");

                                coordinatorList.add(new ComCoordinator(coordinatorId, name, department, image));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                            }
                        }

                    } else {
                        emptyLottie.setVisibility(View.VISIBLE);
                        coordinatorList.clear();
                    }
                    coordinatorAdapter.notifyDataSetChanged();
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch", Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonArrayRequest);
    }

    private void fetchConversation() {
        recyclerView.setAdapter(conversationAdapter);

        String comId = sharedPreferences.getString("com_id", null);
        String url = Constants.API_BASE_URL + "/company/get-coordinator-convo/" + comId;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response != null && response.length() > 0) {
                        emptyLottie.setVisibility(View.GONE);
                        conversationList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);

                                String messageId = obj.getString("message_id");
                                String userId = obj.getString("user_id");
                                String userName = obj.getString("user_name");
                                String lastMessage = obj.getString("last_message");
                                String image = obj.getString("user_image");
                                String imageUrl = Constants.API_BASE_URL + "/" + image;
                                String seen = obj.getString("is_unseen");

                                conversationList.add(new ComCoordinatorConversation(messageId, userId, userName, lastMessage, imageUrl, seen));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                            }
                        }
                        conversationAdapter.notifyDataSetChanged();
                    } else {
                        emptyLottie.setVisibility(View.VISIBLE);
                        conversationList.clear();
                        conversationAdapter.notifyDataSetChanged();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch", Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonArrayRequest);

    }

    private void redirectToMessaging(String userId, String userName) {
        Intent intent = new Intent(this, ComMessageCoordinator.class);
        intent.putExtra("coordinator_id", userId);
        intent.putExtra("coordinator_name", userName);
        startActivity(intent);
    }

    private void readConversation(String messageId) {
        String comId = sharedPreferences.getString("com_id", null);

        String url = Constants.API_BASE_URL + "/company/read-user-convo/" + comId + "/" + messageId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // Handle the API response
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if (status.equals("error")) {
                            Toast.makeText(this, "Failed to mark message as read", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle errors
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(stringRequest);
    }

    @Override
    public void onViewCoordinator(String coordinatorId, String coordinatorName) {
        redirectToMessaging(coordinatorId, coordinatorName);
    }

    @Override
    public void onViewConversation(String messageId, String userId, String userName) {
        readConversation(messageId);
        redirectToMessaging(userId, userName);
    }

    private void setupWindowInsets() {
        setContentView(R.layout.activity_com_view_coordinators);
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
        fetchConversation();
    }

}