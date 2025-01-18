package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowInsetsController;
import android.widget.TextView;
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

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.adapters.AnnAttachmentAdapter;
import com.example.trackticum.models.AnnAttachment;
import com.example.trackticum.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StudViewAnnouncement extends AppCompatActivity implements AnnAttachmentAdapter.AnnAttachmentActions {

    private Toolbar toolbar;

    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    RecyclerView recyclerView;

    TextView titleTv, dateTv, messageTv;

    String announcementId;
    private AnnAttachmentAdapter adapter;
    private List<AnnAttachment> attachmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stud_view_announcement);
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
        announcementId = getIntent().getStringExtra("announcement_id");

        titleTv = findViewById(R.id.title);
        dateTv = findViewById(R.id.date);
        messageTv = findViewById(R.id.message);

        //fetching job offer
        recyclerView = findViewById(R.id.attachment_id);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        attachmentList = new ArrayList<>();
        adapter = new AnnAttachmentAdapter(this, attachmentList, this);
        recyclerView.setAdapter(adapter);

        fetchAnnouncement();
        fetchAttachments();
    }

    private void setupListeners() {

    }

    private void fetchAnnouncement() {
        String url = Constants.API_BASE_URL + "/announcement/get-announcement/" + announcementId;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);

                boolean status = jsonObject.getBoolean("status");

                if (status) {
                    String annID = jsonObject.getString("id");
                    String annTitle = jsonObject.getString("title");
                    String annMessage = jsonObject.getString("message");
                    String annDate = jsonObject.getString("date");

                    titleTv.setText(annTitle);
                    dateTv.setText(annDate);
                    messageTv.setText(Html.fromHtml(annMessage, Html.FROM_HTML_MODE_LEGACY));

                } else {
                    String message = jsonObject.getString("message");
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                Toast.makeText(this, "Error Fetching Announcement", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Announcement", error.toString());
        });

        queue.add(request);
    }

    private void fetchAttachments() {
        String url = Constants.API_BASE_URL + "/announcement/get-ann-attachments/" + announcementId;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    attachmentList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String attachmentId = obj.getString("id");
                            String file = obj.getString("filename");

                            attachmentList.add(new AnnAttachment(attachmentId, file));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Failed to fetch attachments", Toast.LENGTH_SHORT).show()
        );

        queue.add(jsonArrayRequest);
    }


    @Override
    public void onDownload(String attachmentID) {
        String url = Constants.API_BASE_URL + "/attachment/download-ann-attachment/" + attachmentID;

        progressDialog.setMessage("Downloading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);

        StudViewAnnouncement.InputStreamRequest request = new StudViewAnnouncement.InputStreamRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        InputStream inputStream = response.getInputStream();
                        String fileName = response.getMetadata().getString("filename");

                        saveFileToStorage(inputStream, fileName);
                        progressDialog.dismiss();
                        Toast.makeText(this, "File downloaded successfully!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to download file", Toast.LENGTH_SHORT).show();
                }
        );


        queue.add(request);
    }

    private void saveFileToStorage(InputStream inputStream, String fileName) throws IOException {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, fileName);

        if (!path.exists()) {
            path.mkdirs();
        }

        // Write input stream to file
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();
    }

    // Custom InputStreamRequest class
    public class InputStreamRequest extends Request<InputStreamRequest.InputStreamWrapper> {
        private final Response.Listener<InputStreamWrapper> listener;

        public InputStreamRequest(int method, String url, Response.Listener<InputStreamWrapper> listener, Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.listener = listener;
        }

        @Override
        protected Response<InputStreamWrapper> parseNetworkResponse(NetworkResponse response) {
            InputStream inputStream = new ByteArrayInputStream(response.data);
            String contentDisposition = response.headers.get("Content-Disposition");
            String filename = null;

            if (contentDisposition != null && contentDisposition.contains("filename=")) {
                filename = contentDisposition.split("filename=")[1].replace("\"", "").trim();
            }

            Bundle resultData = new Bundle();
            resultData.putString("filename", filename); // Pass filename to listener

            return Response.success(new InputStreamWrapper(inputStream, resultData), getCacheEntry());
        }

        @Override
        protected void deliverResponse(InputStreamWrapper response) {
            listener.onResponse(response);
        }

        public class InputStreamWrapper {
            private final InputStream inputStream;
            private final Bundle metadata;

            public InputStreamWrapper(InputStream inputStream, Bundle metadata) {
                this.inputStream = inputStream;
                this.metadata = metadata;
            }

            public InputStream getInputStream() {
                return inputStream;
            }

            public Bundle getMetadata() {
                return metadata;
            }
        }

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
    protected void onDestroy() {
        super.onDestroy();
        Volley.newRequestQueue(this).cancelAll(request -> true);
    }

}