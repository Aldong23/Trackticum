package com.example.trackticum.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class StudViewPostRequirement extends AppCompatActivity {

    private Toolbar toolbar;

    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;

    String postReqID;
    String postReqTitle;
    String postReqFileID;

    TextView reqTitleTV, reqFileTV;
    ImageView reqStatusIV;
    Button uploadBTN, deleteBTN, templateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stud_view_post_requirement);
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
        toolbar = findViewById(R.id.stud_view_requirements_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("View Post-Requirements");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        //get the document requirement id
        postReqID = getIntent().getStringExtra("post_file_id");

        //initialize widget
        reqTitleTV = findViewById(R.id.req_title_tv);
        reqFileTV = findViewById(R.id.req_file_tv);
        reqStatusIV = findViewById(R.id.req_status_iv);
        uploadBTN = findViewById(R.id.upload_file_btn);
        deleteBTN = findViewById(R.id.delete_file_btn);
        templateBtn = findViewById(R.id.template_btn);

        fetchPreRequirementDetails();
    }

    private void setupListeners() {
        uploadBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFile();
            }
        });
        reqStatusIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show confirmation dialog
                if(!postReqFileID.equals("null")){
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Download")
                            .setMessage("Are you sure you want to download your file?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                downloadFile();
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .show();
                }else{
                    Toast.makeText(StudViewPostRequirement.this, "No Files", Toast.LENGTH_SHORT).show();
                }

            }
        });
        deleteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show confirmation dialog
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Delete Confirmation")
                        .setMessage("Are you sure you want to delete your file?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            deleteFile();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });

        templateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show confirmation dialog
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Download Template")
                        .setMessage("Are you sure you want to download this template?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            downloadTemplate();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData(); // Get file URI
            String base64File = convertFileToBase64(fileUri);

            if (base64File != null) {
                uploadFileToServer(base64File, fileUri);
            }
        }
    }

    private String convertFileToBase64(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return Base64.encodeToString(bytes, Base64.DEFAULT); // Encode file bytes to Base64
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error encoding file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void uploadFileToServer(String base64File, Uri fileUri) {
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Uploading");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String studID = sharedPreferences.getString("stud_id", null);
        String url = Constants.API_BASE_URL + "/student/upload-post-requirement";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload Successful", Toast.LENGTH_LONG).show();
                    fetchPreRequirementDetails();
                    sharedPreferences.edit().putBoolean("refreshPostRequirements", true).apply();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("stud_id", studID);
                params.put("post_req_title", postReqTitle);
                params.put("post_req_id", postReqID);
                params.put("post_req_file_id", postReqFileID);
                params.put("post_file", base64File); // Send Base64 PDF string
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void downloadFile() {
        String url = Constants.API_BASE_URL + "/student/download-post-requirement/" + postReqFileID;

        progressDialog.setMessage("Downloading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);

        // Custom request to handle InputStream for file download
        StudViewRequirement.InputStreamRequest request = new StudViewRequirement.InputStreamRequest(Request.Method.GET, url,
                response -> {
                    try {
                        // Save the file to device storage
                        long timestamp = System.currentTimeMillis();
                        saveFileToStorage(response, postReqTitle + "_" + timestamp + ".pdf");
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

    private void downloadTemplate() {
        String url = Constants.API_BASE_URL + "/attachment/download-post-template/" + postReqID;

        progressDialog.setMessage("Downloading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);

        StudViewPostRequirement.InputStreamRequest request = new StudViewPostRequirement.InputStreamRequest(
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

    public class InputStreamRequest extends Request<StudViewPostRequirement.InputStreamRequest.InputStreamWrapper> {
        private final Response.Listener<StudViewPostRequirement.InputStreamRequest.InputStreamWrapper> listener;

        public InputStreamRequest(int method, String url, Response.Listener<StudViewPostRequirement.InputStreamRequest.InputStreamWrapper> listener, Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.listener = listener;
        }

        @Override
        protected Response<StudViewPostRequirement.InputStreamRequest.InputStreamWrapper> parseNetworkResponse(NetworkResponse response) {
            InputStream inputStream = new ByteArrayInputStream(response.data);
            String contentDisposition = response.headers.get("Content-Disposition");
            String filename = null;

            if (contentDisposition != null && contentDisposition.contains("filename=")) {
                filename = contentDisposition.split("filename=")[1].replace("\"", "").trim();
            }

            Bundle resultData = new Bundle();
            resultData.putString("filename", filename); // Pass filename to listener

            return Response.success(new StudViewPostRequirement.InputStreamRequest.InputStreamWrapper(inputStream, resultData), getCacheEntry());
        }

        @Override
        protected void deliverResponse(StudViewPostRequirement.InputStreamRequest.InputStreamWrapper response) {
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

    private void fetchPreRequirementDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String stud_id = sharedPreferences.getString("stud_id", null);
        String url = Constants.API_BASE_URL + "/view-post-requirement/" + stud_id + "/" + postReqID;

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);

                if (obj.getBoolean("status")) {
                    JSONObject data = obj.getJSONObject("data");

                    // Parse the response
                    postReqTitle = data.getString("post_req_title");
                    String postReqFile = data.optString("post_req_file", null);
                    postReqFileID = data.optString("post_req_file_id", null);

                    // Update UI
                    reqTitleTV.setText(postReqTitle);
                    if (postReqFile != null && !postReqFile.equals("null")) {
                        reqStatusIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_havefile));
                        reqFileTV.setText(postReqTitle + ".pdf");
                        deleteBTN.setEnabled(true);
                    } else {
                        reqFileTV.setText("No file");
                        deleteBTN.setEnabled(false);
                    }
                } else {
                    Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(this, "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    private void deleteFile() {
        progressDialog.setMessage("Deleting...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String url = Constants.API_BASE_URL + "/student/delete-post-requirement/" + postReqFileID;

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Deleted successfully!", Toast.LENGTH_SHORT).show();
                    fetchPreRequirementDetails();
                    // Refresh data after deletion
                    sharedPreferences.edit().putBoolean("refreshPostRequirements", true).apply();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the queue
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