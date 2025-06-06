package com.example.trackticum.fragments;

import android.app.ProgressDialog;
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
import com.example.trackticum.activities.StudEditProfile;
import com.example.trackticum.activities.StudManageSkills;
import com.example.trackticum.activities.StudPostRequirements;
import com.example.trackticum.activities.StudPreRequirements;
import com.example.trackticum.activities.StudRequirements;
import com.example.trackticum.activities.StudShowDtr;
import com.example.trackticum.activities.StudViewWeekly;
import com.example.trackticum.utils.Constants;
import com.google.android.flexbox.FlexboxLayout;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StudProfileFragment extends Fragment {

    public StudProfileFragment() {
        // Required empty public constructor
    }
    private Toolbar toolbar;
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;

    //widget for student details
    private RoundedImageView studImageIV;
    private TextView studNameTV, studNoTV, studApproveTV, studDepTV, studEmailTV, studContactTV, studGenderTV, studBirthdayTV, studAgeTV, studAddressTV;
    ImageView statusIV;

    //button for requirements and weekly report
    private Button viewInitialReqBTN, viewPreReqBTN, viewWeeklyReportBTN, viewDtrButton, viewPostRequirement;

    private ImageButton viewStudSkillsBTN;

    //for Skill Requirements
    private FlexboxLayout skillsContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stud_profile, container, false);

        // Add code below
        initializeData(view);
        setupListeners();

        return view;
    }

    private void initializeData(View view) {
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        progressDialog = new ProgressDialog(requireContext());

        //For action bar
        toolbar = view.findViewById(R.id.stud_profile_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Profile");

        //initialize widget for student details
        studImageIV = view.findViewById(R.id.stud_pic_IV);
        studNameTV = view.findViewById(R.id.stud_name_tv);
        studNoTV = view.findViewById(R.id.stud_no_tv);
        studApproveTV = view.findViewById(R.id.stud_isapprove_tv);
        studDepTV = view.findViewById(R.id.stud_school_dep_tv);
        studEmailTV = view.findViewById(R.id.stud_email_tv);
        statusIV = view.findViewById(R.id.status_IV);
        studContactTV = view.findViewById(R.id.stud_contact_tv);
        studGenderTV = view.findViewById(R.id.stud_gender_tv);
        studBirthdayTV = view.findViewById(R.id.stud_birthday_tv);
        studAgeTV = view.findViewById(R.id.stud_age_tv);
        studAddressTV = view.findViewById(R.id.stud_address_tv);

        //button for requirements and weekly report
        viewInitialReqBTN = view.findViewById(R.id.view_req_btn);
        viewPreReqBTN = view.findViewById(R.id.view_pre_rec);
        viewWeeklyReportBTN = view.findViewById(R.id.view_weekly_btn);
        viewStudSkillsBTN = view.findViewById(R.id.manage_mykills_btn);
        viewDtrButton = view.findViewById(R.id.view_dtr_btn);
        viewPostRequirement = view.findViewById(R.id.view_post_requirement);

        viewPreReqBTN.setVisibility(View.GONE);
        viewDtrButton.setVisibility(View.GONE);
        viewWeeklyReportBTN.setVisibility(View.GONE);
        viewPostRequirement.setVisibility(View.GONE);

        //fetching all details
        fetchStudAndComDetails();

        //Setting up the Skill Requirements
        skillsContainer = view.findViewById(R.id.myskillscontatiner);
        fetchSkills();
    }

    private void setupListeners() {
        viewInitialReqBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), StudRequirements.class);
                startActivity(intent);
            }
        });
        viewStudSkillsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), StudManageSkills.class);
                startActivity(intent);
            }
        });
        viewDtrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), StudShowDtr.class);
                startActivity(intent);
            }
        });
        viewWeeklyReportBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), StudViewWeekly.class);
                startActivity(intent);
            }
        });
        viewPreReqBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), StudPreRequirements.class);
                startActivity(intent);
            }
        });
        viewPostRequirement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), StudPostRequirements.class);
                startActivity(intent);
            }
        });
    }

    private void fetchStudAndComDetails() {

        String studID = sharedPreferences.getString("stud_id", null);
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = Constants.API_BASE_URL + "/student/get-studcom-details/" + studID;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            if (isAdded()) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String comId = jsonObject.getString("company_id");
                    String studImageUrl = jsonObject.getString("image_url");
                    String studFname = jsonObject.getString("firstname");
                    String studLname = jsonObject.getString("lastname");
                    String studMinitial = jsonObject.getString("middle_initial");
                    String studName = studFname + " " + studMinitial + " " + studLname;
                    String stud_no = jsonObject.getString("student_number");
                    String isApproved = jsonObject.getString("is_approve");
                    String schoolDepartment = jsonObject.getString("college_name");
                    String studEmail = jsonObject.getString("email");
                    String isVerified = jsonObject.getString("is_verified");
                    String studContact = jsonObject.getString("contact");
                    String studGender = jsonObject.getString("gender");
                    String studBirthday = jsonObject.getString("formatted_birthday");
                    String studAge = calculateAge(jsonObject.getString("birthday"));
                    String studAddress = jsonObject.getString("address");
                    String studStatus = jsonObject.getString("status");

                    //students details
                    studNameTV.setText(studName);
                    studNoTV.setText(stud_no);
                    studApproveTV.setText(isApproved.equals("1") ? "Approved" : "Not Approved");
                    studDepTV.setText(!schoolDepartment.equals("null") ? schoolDepartment : "N/A");
                    studEmailTV.setText(!studEmail.equals("null") ? studEmail : "N/A");
                    statusIV.setVisibility(isVerified.equals("1") ? View.VISIBLE : View.GONE);
                    studContactTV.setText(!studContact.equals("null") ? studContact : "N/A");
                    studGenderTV.setText(studGender.toLowerCase());
                    studBirthdayTV.setText(!studBirthday.equals("null") ? studBirthday : "N/A");
                    studAgeTV.setText(studAge);
                    studAddressTV.setText(!studAddress.equals("null") ? studAddress : "N/A");

                    if (!studImageUrl.isEmpty()) {
                        Picasso.get()
                                .load(studImageUrl)
                                .placeholder(R.drawable.img_placeholder)
                                .error(R.drawable.img_placeholder)
                                .resize(500, 500)
                                .centerCrop()
                                .into(studImageIV);
                    }

                    if (comId == null || comId.isEmpty() || comId.equals("null")) {
                        viewDtrButton.setVisibility(View.GONE);
                        viewWeeklyReportBTN.setVisibility(View.GONE);
                        viewPreReqBTN.setVisibility(View.GONE);
                        viewPostRequirement.setVisibility(View.GONE);
                    } else {
                        viewPreReqBTN.setVisibility(View.VISIBLE);
                        if (studStatus.equalsIgnoreCase("For Approval")) {
                            viewDtrButton.setVisibility(View.GONE);
                            viewWeeklyReportBTN.setVisibility(View.GONE);
                            viewPostRequirement.setVisibility(View.GONE);
                        } else if (studStatus.equalsIgnoreCase("Ongoing")) {
                            viewDtrButton.setVisibility(View.VISIBLE);
                            viewWeeklyReportBTN.setVisibility(View.VISIBLE);
                            viewPostRequirement.setVisibility(View.GONE);
                        } else if (studStatus.equalsIgnoreCase("Completed")) {
                            viewDtrButton.setVisibility(View.VISIBLE);
                            viewWeeklyReportBTN.setVisibility(View.VISIBLE);
                            viewPostRequirement.setVisibility(View.VISIBLE);
                        } else {
                            viewDtrButton.setVisibility(View.GONE);
                            viewWeeklyReportBTN.setVisibility(View.GONE);
                            viewPostRequirement.setVisibility(View.GONE);
                        }
                    }

                } catch (JSONException e) {
                    Toast.makeText(requireActivity(), "Error Fetching Details", Toast.LENGTH_SHORT).show();
                }
            }
        }, error -> {

        });

        queue.add(request);
    }

    private void fetchSkills() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String studID = sharedPreferences.getString("stud_id", null);

        String url = Constants.API_BASE_URL + "/student/get-skills/" + studID;

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (isAdded()) {
                            try {
                                skillsContainer.removeAllViews();

                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject obj = response.getJSONObject(i);
                                    int skillID = obj.getInt("id");
                                    String skillTitle = obj.getString("name");

                                    TextView jobTextView = new TextView(requireActivity());
                                    jobTextView.setText(skillTitle);
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

                                    skillsContainer.addView(jobTextView);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(requireContext(), "Error processing skills", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonArrayRequest);
    }

    public String calculateAge(String studBirthday) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);

            Date birthDate = sdf.parse(studBirthday);
            Calendar today = Calendar.getInstance();
            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);

            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return String.valueOf(age);
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.stud_profile_actionbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.edit_info) {
            Intent intent = new Intent(getActivity(), StudEditProfile.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        boolean refreshProfile = prefs.getBoolean("refreshStudProfile", false);
        boolean refreshSkills = prefs.getBoolean("refreshSkills", false);

        if(refreshProfile){
            fetchStudAndComDetails();
            prefs.edit().putBoolean("refreshStudProfile", false).apply();
        }
        if(refreshSkills){
            fetchSkills();
            prefs.edit().putBoolean("refreshSkills", false).apply();
        }
    }
}