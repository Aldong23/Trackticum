package com.example.trackticum.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.utils.Constants;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class StudQrFragment extends Fragment {

    public StudQrFragment() {
        // Required empty public constructor
    }

    private Toolbar toolbar;
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;

    private RoundedImageView qrCodeIV;
    private Button genQrBtn;

    private Bitmap bitmap;
    int smallerDimension;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stud_qr, container, false);

        // Add code below
        initializeData(view);
        setupListeners();

        return view;
    }

    private void initializeData(View view) {
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        progressDialog = new ProgressDialog(requireContext());

        //For action bar
        toolbar = view.findViewById(R.id.stud_qr_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Qr Code");
        smallerDimension = Math.min(getResources().getDisplayMetrics().widthPixels,getResources().getDisplayMetrics().heightPixels) * 3 / 4;

        qrCodeIV = view.findViewById(R.id.qr_code_iv);
        genQrBtn = view.findViewById(R.id.generate_qr_btn);

        fetchQrCode();
    }

    private void setupListeners() {
        genQrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveQrToDB();
            }
        });
    }

    private void saveQrToDB() {
        String studID = sharedPreferences.getString("stud_id", null);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Generating");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = Constants.API_BASE_URL + "/student/generate-qr-code/" + studID;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            if (isAdded()) {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean isSuccess = jsonObject.getBoolean("success");
                    String message = jsonObject.getString("message");

                    if (isSuccess) {
                        String qrContent = jsonObject.getString("qr_code");
                        generateQR(qrContent); // Assuming this is a method to display/generate the QR image
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(requireContext(), "Error parsing QR response", Toast.LENGTH_SHORT).show();
                }
            }
        }, error -> {
            if (isAdded()) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Failed to generate QR. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(request);
    }


    private void generateQR(String qrContent) {

        QRGEncoder qrgEncoder = new QRGEncoder(qrContent, null, QRGContents.Type.TEXT, smallerDimension);

        qrgEncoder.setColorBlack(getResources().getColor(R.color.pageGrey, null));
        qrgEncoder.setColorWhite(getResources().getColor(R.color.deepTeal, null));

        try {
            bitmap = qrgEncoder.getBitmap();

            // Set the bitmap to the ImageView
//            qrCodeIV.setBorderWidth((float) 0);
            qrCodeIV.setImageBitmap(bitmap);

        } catch (Exception e) {
            Toast.makeText(requireActivity(), "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchQrCode() {
        String studID = sharedPreferences.getString("stud_id", null);
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = Constants.API_BASE_URL + "/student/get-stud-details/" + studID;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            if (isAdded()) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String qrContent = jsonObject.getString("qr_code");

                    if(!qrContent.isEmpty() && !qrContent.equalsIgnoreCase("null")){
                        generateQR(qrContent);
                    }

                } catch (JSONException e) {
                    Toast.makeText(requireContext(), "Error Fetching QR", Toast.LENGTH_SHORT).show();
                }
            }
        }, error -> {

        });

        queue.add(request);
    }

}