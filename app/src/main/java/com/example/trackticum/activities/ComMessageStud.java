package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
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
import com.example.trackticum.adapters.ComCoordinatorsAdapter;
import com.example.trackticum.adapters.CompanyMessageAdapter;
import com.example.trackticum.adapters.StudentMessageAdapter;
import com.example.trackticum.models.Message;
import com.example.trackticum.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComMessageStud extends AppCompatActivity {

    private Toolbar toolbar;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;
    LinearLayoutManager layoutManager;

    private RecyclerView recyclerView;
    private CompanyMessageAdapter adapter;
    private List<Message> messageList;

    TextInputEditText messageEt;
    FloatingActionButton sendBtn;

    String companyId, studentId, studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_com_message_stud);
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
        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        studentId = getIntent().getStringExtra("student_id");
        studentName = getIntent().getStringExtra("student_name");
        companyId = sharedPreferences.getString("com_id", null);

        toolbar = findViewById(R.id.message_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(studentName);
        toolbar.setNavigationIcon(R.drawable.ic_back);

        messageEventListener();

        recyclerView = findViewById(R.id.messageRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start the RecyclerView scrolled to the bottom
        recyclerView.setLayoutManager(layoutManager);

        messageList = new ArrayList<>();
        adapter = new CompanyMessageAdapter(this, messageList, companyId);
        recyclerView.setAdapter(adapter);

        fetchMessage();

        messageEt = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendButton);
    }

    private void setupListeners() {
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEt.getText().toString().trim();
                if (message.isEmpty()) {
                    messageEt.requestFocus();
                } else {
                    sendMessage(message);
                    messageEt.setText("");
                    messageEt.clearFocus();
                }
            }
        });
    }

    private void sendMessage(String message) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/company/send-student-message";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("result")) {
                            String messge = jsonResponse.getString("message");
                            Toast.makeText(this, messge, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Operation failed!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("student_id", studentId);
                params.put("company_id", companyId);
                params.put("message", message);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    private void fetchMessage() {
        String url = Constants.API_BASE_URL + "/company/get-student-messages/" + studentId + "/" + companyId;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    messageList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);

                            // Parse the JSON response
                            String image = obj.getString("sender_image");
                            String senderId = obj.optString("sender_id", ""); // Optional field
                            String receiverId = companyId; // Assuming this is the company ID
                            String senderType = obj.getString("sender");
                            String message = obj.getString("text");
                            String createdAt = obj.getString("timestamp");

                            // Add the message to the list
                            messageList.add(new Message(image, senderId, receiverId, senderType, message, createdAt));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    }
                    adapter.notifyDataSetChanged(); // Notify the adapter of data changes
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonArrayRequest);
    }

    private void messageEventListener() {
        PusherOptions options = new PusherOptions();
        options.setCluster("ap1");

        Pusher pusher = new Pusher("3f3d1713045a0106baf7", options);

        Channel channel = pusher.subscribe("chat.company-" + companyId + "-student-" + studentId);

        channel.bind("App\\Events\\MessageStudCom", new SubscriptionEventListener() {
            @Override
            public void onEvent(PusherEvent event) {
                Log.d("Pusher", "Received event data: " + event.getData());

                // Parse the event data (assumes JSON format)
                try {
                    JSONObject eventData = new JSONObject(event.getData());
                    String image = eventData.getString("image");
                    String senderId = eventData.getString("senderId");
                    String receiverId = eventData.getString("receiverId");
                    String senderType = eventData.getString("senderType");
                    String message = eventData.getString("message");
                    String createdAt = eventData.getString("createdAt");

                    // Add the new message to the message list
                    messageList.add(new Message(image, senderId, receiverId, senderType, message, createdAt));

                    // Notify the adapter to update the UI
                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        layoutManager.scrollToPosition(messageList.size() - 1); // Scroll to the latest message
                    });
                } catch (JSONException e) {
                    Log.e("Pusher", "Error parsing event data: " + e.getMessage());
                }
            }
        });

        // Connect to Pusher
        pusher.connect();
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