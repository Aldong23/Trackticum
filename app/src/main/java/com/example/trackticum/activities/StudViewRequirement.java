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
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class StudViewRequirement extends AppCompatActivity {

    private Toolbar toolbar;

    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;

    String documentRequirementID;
    String documentBeforeOjtID;
    String requirementTitle;

    TextView reqTitleTV, reqFileTV;
    ImageView reqStatusIV;
    Button uploadBTN, deleteBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stud_view_requirement);
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
        getSupportActionBar().setTitle("View Requirements");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        //get the document requirement id
        documentRequirementID = getIntent().getStringExtra("DOCUMENT_REQUIREMENT_ID");

        //initialize widget
        reqTitleTV = findViewById(R.id.req_title_tv);
        reqFileTV = findViewById(R.id.req_file_tv);
        reqStatusIV = findViewById(R.id.req_status_iv);
        uploadBTN = findViewById(R.id.upload_file_btn);
        deleteBTN = findViewById(R.id.delete_file_btn);

        fetchRequirementDetails();

    }

    private void setupListeners() {
        uploadBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFile();
            }
        });

        deleteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show confirmation dialog
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Delete Confirmation")
                        .setMessage("Are you sure you want to delete this document?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            deleteFile();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });

        reqStatusIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show confirmation dialog
                if(!documentBeforeOjtID.equals("null")){
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Download")
                            .setMessage("Are you sure you want to download this document?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                downloadFile();
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .show();
                }else{
                    Toast.makeText(StudViewRequirement.this, "No Files", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), 1);
    }

    private void deleteFile() {
        progressDialog.setMessage("Deleting...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String url = Constants.API_BASE_URL + "/student/delete-requirement/" + documentBeforeOjtID;

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Document deleted successfully!", Toast.LENGTH_SHORT).show();
                    fetchRequirementDetails();
                    //request to refresh profile
                    sharedPreferences.edit().putBoolean("refreshRequirements", true).apply();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to delete document", Toast.LENGTH_SHORT).show();
                }
        );

        // Add the request to the queue
        queue.add(stringRequest);
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
        String url = Constants.API_BASE_URL + "/student/upload-requirement"; // Update your API endpoint

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload Successful", Toast.LENGTH_LONG).show();
                    fetchRequirementDetails();
                    //request to refresh profile
                    sharedPreferences.edit().putBoolean("refreshRequirements", true).apply();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("stud_id", studID);
                params.put("requirement_title", requirementTitle);
                params.put("document_requirement_id", documentRequirementID);
                params.put("document_before_ojt_id", documentBeforeOjtID);
                params.put("pdf_file", base64File); // Send Base64 PDF string
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void fetchRequirementDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String stud_id = sharedPreferences.getString("stud_id", null);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/view-document-requirement/" + stud_id + "/" + documentRequirementID;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);

                String documentRequirementID = obj.getString("document_requirement_id");
                documentBeforeOjtID = obj.getString("document_before_ojt_id");
                requirementTitle = obj.getString("document_requirement_title");
                String documentBeforeOjtFile = obj.getString("document_before_ojt_file");

                reqTitleTV.setText(requirementTitle);
                if (!documentBeforeOjtID.equals("null")) {
                    reqStatusIV.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_havefile));
                    reqFileTV.setText(requirementTitle + ".pdf");
                    deleteBTN.setEnabled(true);
                } else {
                    reqFileTV.setText("No file");
                    deleteBTN.setEnabled(false);
                }


            } catch (JSONException e) {
                Toast.makeText(this, "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    private void downloadFile() {
        String url = Constants.API_BASE_URL + "/student/download-requirement/" + documentBeforeOjtID;

        progressDialog.setMessage("Downloading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);

        // Custom request to handle InputStream for file download
        InputStreamRequest request = new InputStreamRequest(Request.Method.GET, url,
                response -> {
                    try {
                        // Save the file to device storage
                        saveFileToStorage(response, requirementTitle + ".pdf");
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

    // Custom InputStreamRequest class
    public static class InputStreamRequest extends Request<InputStream> {
        private final Response.Listener<InputStream> listener;

        public InputStreamRequest(int method, String url, Response.Listener<InputStream> listener, Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.listener = listener;
        }

        @Override
        protected Response<InputStream> parseNetworkResponse(NetworkResponse response) {
            InputStream inputStream = new ByteArrayInputStream(response.data);
            return Response.success(inputStream, getCacheEntry());
        }

        @Override
        protected void deliverResponse(InputStream response) {
            listener.onResponse(response);
        }
    }

    // Method to save file to storage
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