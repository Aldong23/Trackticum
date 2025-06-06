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
import com.example.trackticum.adapters.ComConversationAdapter;
import com.example.trackticum.adapters.ComInternsAdapter;
import com.example.trackticum.models.ComConversation;
import com.example.trackticum.models.ComInterns;
import com.example.trackticum.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ComStudConversation extends AppCompatActivity implements ComInternsAdapter.ComInternsAction, ComConversationAdapter.ComConversationAction {

    private Toolbar toolbar;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;

    private Handler handler = new Handler();
    private Runnable workRunnable;

    private LottieAnimationView emptyLottie;
    private RecyclerView recyclerView;
    private ComConversationAdapter conversationAdapter;
    private List<ComConversation> conversationList;
    private ComInternsAdapter internAdapter;
    private List<ComInterns> internsList;

    TextInputEditText searchET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_com_stud_conversation);
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
        toolbar = findViewById(R.id.conversation_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Conversation");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        searchET = findViewById(R.id.search_filter);
        emptyLottie = findViewById(R.id.empty_lottie);
        recyclerView = findViewById(R.id.conversation_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        internsList = new ArrayList<>();
        internAdapter = new ComInternsAdapter(this, internsList, this);

        conversationList = new ArrayList<>();
        conversationAdapter = new ComConversationAdapter(this, conversationList, this);

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
                        fetchInterns(charSequence);
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

    private void fetchInterns(CharSequence charSequence) {
        recyclerView.setAdapter(internAdapter);

        String syID = sharedPreferences.getString("sy_id", null);
        String comId = sharedPreferences.getString("com_id", null);
        String url = Constants.API_BASE_URL + "/company/get-intern-convo/" + charSequence + "/" + comId + "/" + syID;

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
                                String deployedDate = obj.getString("deployed_date");
                                String trainingDuration = obj.getString("training_duration");
                                String hoursRendered = obj.getString("hours_rendered");
                                String progress = obj.getString("progress");

                                internsList.add(new ComInterns(studID, studName, department, imageUrl, deployedDate, trainingDuration, hoursRendered, progress));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                            }
                        }
                        internAdapter.notifyDataSetChanged();
                    } else {
                        emptyLottie.setVisibility(View.VISIBLE);
                        internsList.clear();
                        internAdapter.notifyDataSetChanged();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch interns", Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonArrayRequest);
    }

    private void fetchConversation() {
        recyclerView.setAdapter(conversationAdapter);

        String syID = sharedPreferences.getString("sy_id", null);
        String comId = sharedPreferences.getString("com_id", null);
        String url = Constants.API_BASE_URL + "/company/get-all-convo/" + comId + "/" + syID;

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
                                String studentId = obj.getString("student_id");
                                String studentName = obj.getString("student_name");
                                String lastMessage = obj.getString("last_message");
                                String image = obj.getString("student_image");
                                String imageUrl = Constants.API_BASE_URL + "/" + image;
                                String seen = obj.getString("is_unseen");

                                conversationList.add(new ComConversation(messageId, studentId, studentName, lastMessage, imageUrl, seen));
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
                    Toast.makeText(this, "Failed to fetch interns", Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonArrayRequest);

    }

    private void redirectToMessaging(String studID, String studName) {
        Intent intent = new Intent(this, ComMessageStud.class);
        intent.putExtra("student_id", studID);
        intent.putExtra("student_name", studName);
        startActivity(intent);
    }

    private void readConversation(String messageId) {
        String comId = sharedPreferences.getString("com_id", null);

        String url = Constants.API_BASE_URL + "/conversation/read/" + comId + "/" + messageId;

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
    public void onViewInterns(String studID, String studName) {
        redirectToMessaging(studID, studName);
    }



    @Override
    public void onViewConversation(String messageId, String studId, String studName) {
        readConversation(messageId);
        redirectToMessaging(studId, studName);
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
        fetchConversation();
    }

}