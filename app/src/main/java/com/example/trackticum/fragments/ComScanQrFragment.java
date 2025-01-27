package com.example.trackticum.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.trackticum.R;
import com.example.trackticum.utils.Constants;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ComScanQrFragment extends Fragment {

    private CodeScanner mCodeScanner;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;

    public ComScanQrFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final Activity activity = getActivity();
        View root = inflater.inflate(R.layout.fragment_com_scan_qr, container, false);

        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        progressDialog = new ProgressDialog(requireContext());

        CodeScannerView scannerView = root.findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(activity, scannerView);

        // Request Camera Permission
        Dexter.withContext(activity)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        setupScanner(activity, scannerView);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(activity, "Camera permission is required to use the scanner", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

        return root;
    }

    private void setupScanner(Activity activity, CodeScannerView scannerView) {
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String qrContent = result.getText(); // Get the scanned QR code content
                        processQr(activity, qrContent);
                    }
                });
            }
        });

        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    private void processQr(Activity activity, String qrContent) {
        String comId = sharedPreferences.getString("com_id", null);
        String url = Constants.API_BASE_URL + "/company/scan-qr-code";
        RequestQueue requestQueue = Volley.newRequestQueue(activity);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getBoolean("success")) {
                                // Parse student data from the response
                                JSONObject studentData = jsonResponse.getJSONObject("data");

                                String fullName = studentData.getString("student_name");
                                String department = studentData.getString("department_name");
                                String imageUrl = studentData.getString("image");
                                String timeInOrOut = studentData.getString("action");
                                String timestamp = studentData.getString("timestamp");

                                // Show the AlertDialog with the parsed details
                                showStudentDetailsDialog(activity, fullName, department, imageUrl, timeInOrOut, timestamp);
                            } else {
                                Toast.makeText(activity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(activity, "Response parsing error", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(activity, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("qr_content", qrContent);
                params.put("com_id", comId);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void showStudentDetailsDialog(Activity activity, String fullName, String department, String imageUrl, String timeInOrOut, String timestamp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_student_details, null);

        // Initialize views in the dialog
        TextView tvFullName = dialogView.findViewById(R.id.tvFullName);
        TextView tvDepartment = dialogView.findViewById(R.id.tvDepartment);
        TextView tvTimeInOut = dialogView.findViewById(R.id.tvTimeInOut);
        TextView tvTimestamp = dialogView.findViewById(R.id.tvTimestamp);
        RoundedImageView ivStudentImage = dialogView.findViewById(R.id.ivStudentImage);

        // Set data to the views
        tvFullName.setText(fullName);
        tvDepartment.setText(department);
        tvTimeInOut.setText(timeInOrOut);
        tvTimestamp.setText(timestamp);

        // Load image using Glide or Picasso
        Picasso.get().invalidate(imageUrl);
        if (!imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .resize(500, 500)
                    .centerCrop()
                    .into(ivStudentImage);
        }

        builder.setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    mCodeScanner.startPreview();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mCodeScanner != null) {
            mCodeScanner.startPreview();
        }
    }

    @Override
    public void onPause() {
        if (mCodeScanner != null) {
            mCodeScanner.releaseResources();
        }
        super.onPause();
    }

}
