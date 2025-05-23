package com.example.trackticum.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.trackticum.activities.ComManageJoboffer;
import com.example.trackticum.activities.ComShowApplicants;
import com.example.trackticum.activities.ComStudConversation;
import com.example.trackticum.activities.ComViewCoordinators;
import com.example.trackticum.activities.ComViewInterns;
import com.example.trackticum.adapters.ComApplicantsAdapter;
import com.example.trackticum.adapters.StudCompaniesAdapter;
import com.example.trackticum.models.ComApplicants;
import com.example.trackticum.models.StudCompanies;
import com.example.trackticum.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComHomeFragment extends Fragment implements ComApplicantsAdapter.ComApplicantsAction {

    public ComHomeFragment() {
        // Required empty public constructor
    }

    private Toolbar toolbar;
    ProgressDialog progressDialog;

    SharedPreferences sharedPreferences;
    private LottieAnimationView emptyLottie;

    TextView numInternsTV, availabelSlotTV, moaDurationTV;
    private Button viewInternsBTN;

    //fetching list of companies
    private RecyclerView recyclerView;
    private ComApplicantsAdapter adapter;
    private List<ComApplicants> applicantsList;

    private String studentID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_com_home, container, false);

        // Add code below
        initializeData(view);
        setupListeners(view);

        return view;
    }

    private void initializeData(View view) {
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        progressDialog = new ProgressDialog(requireContext());

        toolbar = view.findViewById(R.id.com_home_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Home");

        numInternsTV = view.findViewById(R.id.num_intern_tv);
        availabelSlotTV = view.findViewById(R.id.slot_tv);
        moaDurationTV = view.findViewById(R.id.moa_duration_tv);
        viewInternsBTN = view.findViewById(R.id.view_interns_btn);

        fetchComDashboard();

        emptyLottie = view.findViewById(R.id.empty_lottie);
        recyclerView = view.findViewById(R.id.com_applicants_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        applicantsList = new ArrayList<>();
        adapter = new ComApplicantsAdapter(requireContext(), applicantsList, this);
        recyclerView.setAdapter(adapter);

        fetchApplicants();
    }

    private void setupListeners(View view) {
        viewInternsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ComViewInterns.class);
                startActivity(intent);
            }
        });
    }

    private void fetchApplicants() {
        String comId = sharedPreferences.getString("com_id", null);
        String syID = sharedPreferences.getString("sy_id", null);
        String url = Constants.API_BASE_URL + "/company/get-applicants/" + comId + "/" + syID;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if(isAdded()){
                        if (response != null && response.length() > 0) {
                            emptyLottie.setVisibility(View.GONE);
                            applicantsList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject obj = response.getJSONObject(i);

                                    String applicantsID = obj.getString("applicants_id");
                                    String studID = obj.getString("stud_id");
                                    String studName = obj.getString("stud_name");
                                    String department = obj.getString("department");
                                    String studImage = obj.getString("stud_image");
                                    String imageUrl = Constants.API_BASE_URL + "/" + studImage;

                                    applicantsList.add(new ComApplicants(applicantsID, studID, studName, department, imageUrl));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(requireContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            emptyLottie.setVisibility(View.VISIBLE);
                            applicantsList.clear();
                            adapter.notifyDataSetChanged();
                        }
                    }
                },
                error -> {
                    if(isAdded()){
                        Toast.makeText(requireContext(), "Failed to fetch applicants", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsonArrayRequest);
    }

    private void fetchComDashboard() {
        // Get student ID from SharedPreferences
        String syID = sharedPreferences.getString("sy_id", null);
        String comId = sharedPreferences.getString("com_id", null);
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = Constants.API_BASE_URL + "/company/com-dashboard-details/" + comId + "/" + syID;

        // API Request
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            if(isAdded()){
                try {
                    // Parse the JSON response
                    JSONObject jsonObject = new JSONObject(response);
                    boolean status = jsonObject.getBoolean("status");

                    if (status) {
                        // Extract data from "data" object
                        JSONObject data = jsonObject.getJSONObject("data");

                        String numInterns = data.getString("num_interns");
                        String availableSlots = data.getString("available_slots");
                        String moaDuration = data.optString("moa_duration", "No MOA available");

                        numInternsTV.setText(numInterns);
                        availabelSlotTV.setText(availableSlots);
                        moaDurationTV.setText(moaDuration);
                    }
                } catch (JSONException e) {
                    Toast.makeText(requireActivity(), "Error Fetching Details", Toast.LENGTH_SHORT).show();
                }
            }

        }, error -> {
            if(isAdded()){
                Toast.makeText(requireActivity(), "Failed to fetch dashboard details", Toast.LENGTH_SHORT).show();
            }
        });

        // Add request to queue
        queue.add(request);
    }


    @Override
    public void onViewApplicants(String studID) {
        Intent intent = new Intent(requireContext(), ComShowApplicants.class);
        intent.putExtra("stud_id", studID);
        startActivity(intent);
    }

    @Override
    public void onDeclineApplicants(String applicationID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to decline this student?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            declineStudent(applicationID);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onAcceptApplicants(String studID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirm Acceptance");
        builder.setMessage("Are you sure you want to accept this student?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            acceptStudent(studID);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void declineStudent(String applicationID) {
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Deleting");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String url = Constants.API_BASE_URL + "/company/delete-applicant/" + applicationID;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(requireContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            fetchApplicants();
                        } else {
                            Toast.makeText(requireContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(requireContext(), "Response parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Failed to delete applicant. Please try again.", Toast.LENGTH_SHORT).show();
                });

        queue.add(stringRequest);
    }


    private void acceptStudent(String studID) {
        String comId = sharedPreferences.getString("com_id", null);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Accepting");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = Constants.API_BASE_URL + "/company/accept-student";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(requireContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            fetchApplicants();
                        } else {
                            Toast.makeText(requireContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(requireContext(), "Response parsing error", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            progressDialog.dismiss();
            Toast.makeText(requireContext(), "Failed to accept student. Please try again.", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("student_id", studID);
                params.put("company_id", comId);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    private void getLatestSchoolYear() {
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Refreshing");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        String url = Constants.API_BASE_URL + "/school-year/get-default";

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            if(isAdded()){
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    boolean status = jsonObject.getBoolean("status");

                    if (status) {
                        String school_year_id = jsonObject.getString("id");

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("sy_id", school_year_id);
                        editor.apply();
                        Toast.makeText(requireContext(), "All data has been refreshed.", Toast.LENGTH_SHORT).show();
                        fetchApplicants();
                        fetchComDashboard();
                        progressDialog.dismiss();
                    }

                } catch (JSONException e) {
                    Toast.makeText(requireContext(), "Error Fetching Announcement", Toast.LENGTH_SHORT).show();
                }
            }

        }, error -> {

        });

        queue.add(request);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.com_home_actionbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.refresh) {
            getLatestSchoolYear();
            return true;
        } else if(id == R.id.coordinators){
            Intent intent = new Intent(getActivity(), ComViewCoordinators.class);
            startActivity(intent);
            return true;
        } else if(id == R.id.students){
            Intent intent = new Intent(getActivity(), ComStudConversation.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        boolean refreshApplicantsList = prefs.getBoolean("refreshApplicantsList", false);
        if(refreshApplicantsList){
            fetchApplicants();
            prefs.edit().putBoolean("refreshApplicantsList", false).apply();
        }
    }


}