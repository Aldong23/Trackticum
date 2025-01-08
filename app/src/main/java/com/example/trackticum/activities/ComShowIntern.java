package com.example.trackticum.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ComShowIntern extends AppCompatActivity {

    private Toolbar toolbar;
    ProgressDialog progressDialog;

    //widget for student details
    private RoundedImageView studImageIV;
    private TextView studNameTV, studNoTV, studDepTV, studEmailTV, studContactTV, studGenderTV, studBirthdayTV, studAgeTV, studAddressTV;
    SharedPreferences sharedPreferences;
    private Button viewDtrBTN, viewWeeklyReportBTN, viewEvaluationBTN;

    //for Skill Requirements
    private FlexboxLayout skillsContainer;

    String studID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_com_show_intern);
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
        toolbar = findViewById(R.id.com_interns_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Intern Details");
        toolbar.setNavigationIcon(R.drawable.ic_back);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        //initialize widget for student details
        studID = getIntent().getStringExtra("stud_id");
        studImageIV = findViewById(R.id.stud_pic_IV);
        studNameTV = findViewById(R.id.stud_name_tv);
        studNoTV = findViewById(R.id.stud_no_tv);
        studDepTV = findViewById(R.id.stud_school_dep_tv);
        studEmailTV = findViewById(R.id.stud_email_tv);
        studContactTV = findViewById(R.id.stud_contact_tv);
        studGenderTV = findViewById(R.id.stud_gender_tv);
        studBirthdayTV = findViewById(R.id.stud_birthday_tv);
        studAgeTV = findViewById(R.id.stud_age_tv);
        studAddressTV = findViewById(R.id.stud_address_tv);
        viewDtrBTN = findViewById(R.id.view_dtr_btn);
        viewWeeklyReportBTN = findViewById(R.id.view_weekly_btn);
        viewEvaluationBTN = findViewById(R.id.view_evaluation_btn);

        fetchStudDetails();

        //Setting up the Skill Requirements
        skillsContainer = findViewById(R.id.skillscontatiner);
        fetchSkills();
    }

    private void setupListeners() {
        viewDtrBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ComShowIntern.this, ComShowDtr.class);
                intent.putExtra("stud_id", studID);
                startActivity(intent);
            }
        });
    }

    private void fetchStudDetails() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.API_BASE_URL + "/student/get-stud-details/" + studID;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);

                String studImageUrl = jsonObject.getString("image_url");
                String studFname = jsonObject.getString("firstname");
                String studLname = jsonObject.getString("lastname");
                String studMinitial = jsonObject.getString("middle_initial") + ".";
                String studName = studFname + " " + studMinitial + " " + studLname;
                String stud_no = jsonObject.getString("student_number");
                String schoolDepartment = jsonObject.getString("department_name");
                String studEmail = jsonObject.getString("email");
                String studContact = jsonObject.getString("contact");
                String studGender = jsonObject.getString("gender");
                String studBirthday = jsonObject.getString("birthday");
                String studAge = calculateAge(studBirthday);
                String studAddress = jsonObject.getString("address");

                //students details
                studNameTV.setText(studName);
                studNoTV.setText(stud_no);
                studDepTV.setText(schoolDepartment);
                studEmailTV.setText(studEmail);
                studContactTV.setText(studContact);
                studGenderTV.setText(studGender);
                studBirthdayTV.setText(studBirthday);
                studAgeTV.setText(studAge);
                studAddressTV.setText(studAddress);


                Picasso.get().invalidate(studImageUrl);
                if (!studImageUrl.isEmpty()) {
                    Picasso.get()
                            .load(studImageUrl)
                            .placeholder(R.drawable.img_placeholder)
                            .error(R.drawable.img_placeholder)
                            .resize(500, 500)
                            .centerCrop()
                            .into(studImageIV);
                }

            } catch (JSONException e) {
                Toast.makeText(this, "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    private void fetchSkills() {
        String url = Constants.API_BASE_URL + "/student/get-skills/" + studID;

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
                            skillsContainer.removeAllViews();

                            // Loop through the job offers in the response
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                int skillID = obj.getInt("id");
                                String skillTitle = obj.getString("name");

                                // Create TextView for each job offer
                                TextView jobTextView = new TextView(ComShowIntern.this);
                                jobTextView.setText(skillTitle);
                                jobTextView.setPadding(16, 8, 16, 8);
                                jobTextView.setBackgroundResource(R.drawable.job_offer_style);
                                jobTextView.setTextColor(Color.BLACK);

                                Typeface customFont = ResourcesCompat.getFont(ComShowIntern.this, R.font.sf_rounded_regular);
                                jobTextView.setTypeface(customFont);

                                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                                );
                                params.setMargins(2, 2, 2, 2);
                                jobTextView.setLayoutParams(params);

                                // Add the TextView to the container
                                skillsContainer.addView(jobTextView);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ComShowIntern.this, "Error processing skills", Toast.LENGTH_SHORT).show();
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
            return "Invalid Date";
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
}