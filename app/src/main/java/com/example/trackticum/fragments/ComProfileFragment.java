package com.example.trackticum.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.activities.ComEditProfile;
import com.example.trackticum.activities.ComManageJoboffer;
import com.example.trackticum.activities.ComViewInterns;
import com.example.trackticum.activities.StudLogin;
import com.example.trackticum.utils.Constants;
import com.google.android.flexbox.FlexboxLayout;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class ComProfileFragment extends Fragment {

    public ComProfileFragment() {
        // Required empty public constructor
    }

    // For action bar
    private Toolbar toolbar;

    //Fetch Company Information
    private TextView comNameTV, comNatureTV, comLocationTV, comEmailTV, comSlotTV, comContactTV, comBgTV;
    private RoundedImageView comLogoIV;
    SharedPreferences sharedPreferences;
    private ImageButton manageJobOfferBtn;
    private Button viewInternsBTN;

    //for Skill Requirements
    private FlexboxLayout jobsContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_com_profile, container, false);

        // Add code below
        initializeData(view);
        setupListeners(view);

        return view;
    }

    private void initializeData(View view) {
        //For action bar
        toolbar = view.findViewById(R.id.com_profile_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Profile");

        //Fetch Company Information
        comLogoIV = view.findViewById(R.id.com_logo_IV);
        comNameTV = view.findViewById(R.id.com_name_tv);
        comNatureTV = view.findViewById(R.id.com_nature_tv);
        comLocationTV = view.findViewById(R.id.com_location_tv);
        comEmailTV = view.findViewById(R.id.com_email_tv);
        comSlotTV = view.findViewById(R.id.com_slot_tv);
        comContactTV = view.findViewById(R.id.com_contact_tv);
        comBgTV = view.findViewById(R.id.com_descrip_tv);
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        fetchCompanyDetails();

        manageJobOfferBtn = view.findViewById(R.id.manage_joboffer_btn);
        viewInternsBTN = view.findViewById(R.id.view_interns_btn);

        //Setting up the Skill Requirements
        jobsContainer = view.findViewById(R.id.jobsContainer);
        fetchJobOffer();
    }

    private void setupListeners(View view) {
        manageJobOfferBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ComManageJoboffer.class);
                startActivity(intent);

            }
        });
        viewInternsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ComViewInterns.class);
                startActivity(intent);
            }
        });
    }

    private void fetchCompanyDetails() {
        String comId = sharedPreferences.getString("com_id", null);
        RequestQueue queue = Volley.newRequestQueue(requireContext());
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
                Toast.makeText(requireActivity(), "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    private void fetchJobOffer() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String comId = sharedPreferences.getString("com_id", null);

        String url = Constants.API_BASE_URL + "/company/get-job-offers/" + comId;

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

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
                                TextView jobTextView = new TextView(requireActivity());
                                jobTextView.setText(jobTitle);
                                jobTextView.setPadding(16, 8, 16, 8);
                                jobTextView.setBackgroundResource(R.drawable.job_offer_style);
                                jobTextView.setTextColor(Color.BLACK);

                                Typeface customFont = ResourcesCompat.getFont(requireContext(), R.font.sf_rounded_regular);
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
                            Toast.makeText(requireContext(), "Error processing job offers", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(requireContext(), "Failed to fetch job offers", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonArrayRequest);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.com_profile_actionbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.edit_info) {
            Intent intent = new Intent(getActivity(), ComEditProfile.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        boolean refreshProfile = prefs.getBoolean("refreshComProfile", false);
        boolean refreshJobOffer = prefs.getBoolean("refreshJobOffers", false);

        if(refreshProfile){
            fetchCompanyDetails();
            prefs.edit().putBoolean("refreshComProfile", false).apply();
        }
        if(refreshJobOffer){
            fetchJobOffer();
            prefs.edit().putBoolean("refreshJobOffers", false).apply();
        }

    }
}