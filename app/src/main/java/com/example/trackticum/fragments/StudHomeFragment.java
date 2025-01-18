package com.example.trackticum.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trackticum.R;
import com.example.trackticum.activities.StudAnnouncementList;
import com.example.trackticum.activities.StudShowCompany;
import com.example.trackticum.activities.StudShowWeekly;
import com.example.trackticum.activities.StudViewWeekly;
import com.example.trackticum.adapters.StudCompaniesAdapter;
import com.example.trackticum.models.StudCompanies;
import com.example.trackticum.utils.Constants;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StudHomeFragment extends Fragment implements StudCompaniesAdapter.StudCompaniesAction {

    public StudHomeFragment() {
        // Required empty public constructor
    }

    private Toolbar toolbar;
    SharedPreferences sharedPreferences;
    LottieAnimationView notApprovedLottie;

    //fetching list of companies
    private RecyclerView recyclerView;
    private StudCompaniesAdapter adapter;
    private List<StudCompanies> companiesList;

    //for latest announcement
    ConstraintLayout annContainer;
    TextView annMessageTv, annDateTV;
    TextView hoursRenderedTV, hoursToBeRenderedTV, trainingDurationTV;

    private String companyId, isApproved, announcementID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stud_home, container, false);

        // Add code below
        initializeData(view);
        setupListeners(view);

        return view;
    }

    private void initializeData(View view) {
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        //For action bar
        toolbar = view.findViewById(R.id.stud_home_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Home");

        // for fetching companies
        notApprovedLottie = view.findViewById(R.id.not_approved_lottie);
        recyclerView = view.findViewById(R.id.stud_companies_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        //for announcement
        annContainer = view.findViewById(R.id.announcement_container);
        annMessageTv = view.findViewById(R.id.ann_message);
        annDateTV = view.findViewById(R.id.ann_date);

        fetchAnnouncement();

        hoursRenderedTV = view.findViewById(R.id.hours_rendered);
        hoursToBeRenderedTV = view.findViewById(R.id.hours_to_be_rendered);
        trainingDurationTV = view.findViewById(R.id.training_duration);

        fetchDashboard();

        companiesList = new ArrayList<>();
        adapter = new StudCompaniesAdapter(requireContext(), companiesList,this);
        recyclerView.setAdapter(adapter);

        fetchStudDetails();

    }

    private void setupListeners(View view) {
        annContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), StudAnnouncementList.class);
                startActivity(intent);
            }
        });
    }

    private void fetchStudDetails() {
        String studID = sharedPreferences.getString("stud_id", null);
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = Constants.API_BASE_URL + "/student/get-stud-details/" + studID;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject studDetails = new JSONObject(response);

                String stud_id = studDetails.getString("id");
                String company_id = studDetails.getString("company_id");
                String is_approved = studDetails.getString("is_approve");

                companyId = company_id;
                isApproved = is_approved;

                notApprovedLottie.setVisibility(View.GONE);
                fetchCompanies();

            } catch (JSONException e) {
                Toast.makeText(requireContext(), "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
        });

        queue.add(request);
    }

    private void fetchCompanies() {
        String studID = sharedPreferences.getString("stud_id", null);
        String url = Constants.API_BASE_URL + "/student/get-companies/" + studID;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        companiesList.clear();
                        if (response != null && response.length() > 0){
                            notApprovedLottie.setVisibility(View.GONE);
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject obj = response.getJSONObject(i);
                                    String comID = obj.getString("id");
                                    String comLogo = obj.getString("image");
                                    String imageUrl = Constants.API_BASE_URL + "/" + comLogo;
                                    String comName = obj.getString("name");
                                    String comAddress = obj.getString("address");
                                    String comDescription = obj.getString("description");
                                    String comSlot = obj.getString("slot");

                                    companiesList.add(new StudCompanies(comID, comName, imageUrl, comAddress, comDescription, comSlot));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }else{
                            notApprovedLottie.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(requireContext(), "Failed to fetch companies", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsonArrayRequest);
    }

    private void fetchAnnouncement() {
        String depID = sharedPreferences.getString("dep_id", null);
        String syID = sharedPreferences.getString("sy_id", null);

        String url = Constants.API_BASE_URL + "/announcement/get-latest-announcement/" + depID + "/" + syID;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);

                boolean status = jsonObject.getBoolean("status");

                if (status) {
                    String annID = jsonObject.getString("id");
                    String annTitle = jsonObject.getString("title");
                    String annMessage = jsonObject.getString("message");
                    String annDate = jsonObject.getString("date");

                    announcementID = annID;
                    annDateTV.setText(annDate);
                    annMessageTv.setText(Html.fromHtml(annMessage, Html.FROM_HTML_MODE_LEGACY));
                    annContainer.setVisibility(View.VISIBLE);
                } else {
                    annContainer.setVisibility(View.GONE);
                }

            } catch (JSONException e) {
                Toast.makeText(requireContext(), "Error Fetching Announcement", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Announcement", error.toString());
        });

        queue.add(request);
    }

    private void fetchDashboard() {
        // Get student ID from SharedPreferences
        String studID = sharedPreferences.getString("stud_id", null);
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = Constants.API_BASE_URL + "/student/get-dashboard-details/" + studID;

        // API Request
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                // Parse the JSON response
                JSONObject jsonObject = new JSONObject(response);
                boolean status = jsonObject.getBoolean("status");

                if (status) {
                    // Extract data from "data" object
                    JSONObject data = jsonObject.getJSONObject("data");
                    String hoursRendered = data.getString("hours_rendered");
                    String hoursToBeRendered = data.getString("hours_to_be_rendered");
                    String trainingDuration = data.getString("training_duration");

                    // Set the values to the TextViews
                    hoursRenderedTV.setText(hoursRendered + " hrs");
                    hoursToBeRenderedTV.setText(hoursToBeRendered + " hrs");
                    trainingDurationTV.setText(trainingDuration + " hrs");
                }

            } catch (JSONException e) {
                Toast.makeText(requireActivity(), "Error Fetching Details", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Error Fetching Details", error.toString());
            Toast.makeText(requireActivity(), "Failed to fetch dashboard details", Toast.LENGTH_SHORT).show();
        });

        // Add request to queue
        queue.add(request);
    }



    @Override
    public void onViewCompanies(String comID) {
        Intent intent = new Intent(requireContext(), StudShowCompany.class);
        intent.putExtra("com_id", comID);
        intent.putExtra("stud_com_id", companyId);
        intent.putExtra("stud_is_approved", isApproved);
        startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.stud_home_action_bar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.refresh) {
            fetchStudDetails();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Volley.newRequestQueue(requireContext()).cancelAll(request -> true);
    }
}