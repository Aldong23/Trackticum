package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowInsetsController;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.utils.Constants;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StudShowCompany extends AppCompatActivity {

    // For action bar
    private Toolbar toolbar;

    //Fetch Company Information
    private TextView comNameTV, comNatureTV, comLocationTV, comEmailTV, comSlotTV, comContactTV, comBgTV;
    private RoundedImageView comLogoIV;
    SharedPreferences sharedPreferences;
    private ExtendedFloatingActionButton applyBTN;

    //for Skill Requirements
    private FlexboxLayout jobsContainer;

    private String comId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stud_show_company);
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
        toolbar = findViewById(R.id.show_company_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Companies");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        //Fetch Company Information
        comId = getIntent().getStringExtra("com_id");
        comLogoIV = findViewById(R.id.com_logo_IV);
        comNameTV = findViewById(R.id.com_name_tv);
        comNatureTV = findViewById(R.id.com_nature_tv);
        comLocationTV = findViewById(R.id.com_location_tv);
        comEmailTV = findViewById(R.id.com_email_tv);
        comSlotTV = findViewById(R.id.com_slot_tv);
        comContactTV = findViewById(R.id.com_contact_tv);
        comBgTV = findViewById(R.id.com_descrip_tv);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        fetchCompanyDetails();

        applyBTN = findViewById(R.id.apply_btn);

        //Setting up the Skill Requirements
        jobsContainer = findViewById(R.id.jobsContainer);
        fetchJobOffer();
    }

    private void setupListeners() {

    }

    private void fetchCompanyDetails() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/company/get-com-details/" + comId;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject comDetails = new JSONObject(response);

                int comID = comDetails.getInt("id");
                String imageUrl = comDetails.getString("image_url");
                String comName = comDetails.getString("name");
                String comNature = comDetails.getString("nature");
                String comLocation = comDetails.getString("address");
                String comEmail = comDetails.getString("email");
                String comSlot = comDetails.getString("slot");
                String comContact = comDetails.getString("contact");
                String comBg = comDetails.getString("description");

                comNameTV.setText(comName);
                comNatureTV.setText(comNature);
                comLocationTV.setText(comLocation);
                comEmailTV.setText(comEmail);
                comSlotTV.setText(comSlot);
                comContactTV.setText(comContact);
                comBgTV.setText(comBg);

                Picasso.get().invalidate(imageUrl);
                if (!imageUrl.isEmpty()) {
                    Picasso.get()
                            .load(imageUrl)
                            .placeholder(R.drawable.img_placeholder)
                            .error(R.drawable.img_placeholder)
                            .resize(500, 500)
                            .centerCrop()
                            .into(comLogoIV);
                }

            } catch (JSONException e) {
                Toast.makeText(this, "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    private void fetchJobOffer() {
        String url = Constants.API_BASE_URL + "/company/get-job-offers/" + comId;

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Clear the existing views in jobsContainer
                            jobsContainer.removeAllViews();

                            // Loop through the job offers in the response
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                int jobOfferId = obj.getInt("id");
                                String jobTitle = obj.getString("name");

                                // Create TextView for each job offer
                                TextView jobTextView = new TextView(StudShowCompany.this);
                                jobTextView.setText(jobTitle);
                                jobTextView.setPadding(16, 8, 16, 8);
                                jobTextView.setBackgroundResource(R.drawable.job_offer_style);
                                jobTextView.setTextColor(Color.BLACK);

                                Typeface customFont = ResourcesCompat.getFont(StudShowCompany.this, R.font.sf_rounded_regular);
                                jobTextView.setTypeface(customFont);

                                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                                );
                                params.setMargins(2, 2, 2, 2);
                                jobTextView.setLayoutParams(params);

                                // Add the TextView to the container
                                jobsContainer.addView(jobTextView);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(StudShowCompany.this, "Error processing job offers", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(StudShowCompany.this, "Failed to fetch job offers", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonArrayRequest);
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