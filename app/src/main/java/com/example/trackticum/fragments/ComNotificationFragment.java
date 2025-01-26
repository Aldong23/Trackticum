package com.example.trackticum.fragments;

import android.Manifest;
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
import com.example.trackticum.activities.ComStudConversation;
import com.example.trackticum.activities.ComViewCoordinators;
import com.example.trackticum.activities.StudAnnouncementList;
import com.example.trackticum.activities.StudMainActivity;
import com.example.trackticum.adapters.NotificationAdapter;
import com.example.trackticum.models.Notification;
import com.example.trackticum.utils.Constants;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ComNotificationFragment extends Fragment implements NotificationAdapter.NotificationActions {

    public ComNotificationFragment() {
        // Required empty public constructor
    }

    private Toolbar toolbar;

    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    RecyclerView recyclerView;
    LottieAnimationView emptyLA;

    private NotificationAdapter adapter;
    private List<Notification> notificationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_com_notification, container, false);

        // Add code below
        initializeData(view);
        setupListeners(view);

        return view;
    }

    private void initializeData(View view) {
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        progressDialog = new ProgressDialog(requireContext());

        //For action bar
        toolbar = view.findViewById(R.id.com_notif_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Notifications");

        emptyLA = view.findViewById(R.id.empty_lottie);
        recyclerView = view.findViewById(R.id.notification_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(requireContext(), notificationList,this);
        recyclerView.setAdapter(adapter);

        fetchNotification();
    }

    private void setupListeners(View view) {

    }

    private void fetchNotification() {
        String comId = sharedPreferences.getString("com_id", null);
        String url = Constants.API_BASE_URL + "/company/com-get-notification/" + comId;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        notificationList.clear();
                        if (response != null && response.length() > 0){
                            emptyLA.setVisibility(View.GONE);
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject obj = response.getJSONObject(i);
                                    String notificationID = obj.getString("id");
                                    String senderName = obj.getString("sender_name");
                                    String message = obj.getString("message");
                                    String type = obj.getString("type");
                                    String date = obj.getString("formatted_date");
                                    String isRead = obj.getString("is_read");

                                    notificationList.add(new Notification(notificationID, senderName, message, type, date, isRead));
                                } catch (JSONException e) {
                                    Toast.makeText(requireContext(), "Failed to fetch Notification", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        }else{
                            emptyLA.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(requireContext(), "Failed to fetch Notification", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsonArrayRequest);
    }

    private void markASAllRead() {
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String comId = sharedPreferences.getString("com_id", null);
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = Constants.API_BASE_URL + "/company/mark-notification-read/" + comId;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                // Parse the response as JSON
                JSONObject jsonResponse = new JSONObject(response);
                boolean status = jsonResponse.getBoolean("status");
                String message = jsonResponse.getString("message");

                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                fetchNotification();
                progressDialog.dismiss();

            } catch (JSONException e) {
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }, error -> {
            Log.e("Failed", error.toString());
            progressDialog.dismiss();
        });

        queue.add(request);
    }


    @Override
    public void onClick(String type) {
        if(type.equals("student_message")){
            Intent intent = new Intent(requireContext(), ComStudConversation.class);
            startActivity(intent);
        }else if(type.equals("user_message")){
            Intent intent = new Intent(requireContext(), ComViewCoordinators.class);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        Dexter.withContext(requireContext())
                .withPermission(Manifest.permission.POST_NOTIFICATIONS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // No action needed when permission is granted
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(requireContext(), "Notification permission is required.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.notification_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.mark_as_read) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Mark All as Read")
                    .setMessage("Are you sure you want to mark all notifications as read?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        markASAllRead();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Volley.newRequestQueue(requireContext()).cancelAll(request -> true);
    }
}