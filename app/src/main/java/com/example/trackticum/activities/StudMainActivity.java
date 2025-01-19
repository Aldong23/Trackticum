package com.example.trackticum.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsetsController;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.trackticum.R;
import com.example.trackticum.databinding.ActivityStudMainBinding;
import com.example.trackticum.fragments.StudHomeFragment;
import com.example.trackticum.fragments.StudNotificationFragment;
import com.example.trackticum.fragments.StudProfileFragment;
import com.example.trackticum.fragments.StudQrFragment;
import com.example.trackticum.fragments.StudSettingsFragment;

public class StudMainActivity extends AppCompatActivity {

    ActivityStudMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //setting up the status bar
        getWindow().setStatusBarColor(getResources().getColor(R.color.deepTeal));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().getInsetsController().setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(0); // Clear the flag for light status bar
        }

        binding = ActivityStudMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String notificationType = getIntent().getStringExtra("notification_type");
        if (notificationType != null) {
            handleNotificationType(notificationType);
        } else {
            replaceFragment(new StudHomeFragment());
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if(itemId == R.id.home){
                replaceFragment(new StudHomeFragment());
            }else if(itemId == R.id.notification){
                replaceFragment(new StudNotificationFragment());
            }else if(itemId == R.id.gen_qr){
                replaceFragment(new StudQrFragment());
            }else if(itemId == R.id.profile){
                replaceFragment(new StudProfileFragment());
            }else if(itemId == R.id.settings){
                replaceFragment(new StudSettingsFragment());
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void setSelectedBottomMenu(int itemId) {
        binding.bottomNavigation.setSelectedItemId(itemId);
    }

    //for push notif
    private void handleNotificationType(String type) {
        if (type.equalsIgnoreCase("profile")) {
            setSelectedBottomMenu(R.id.profile);
            replaceFragment(new StudProfileFragment());
        } else if (type.equalsIgnoreCase("settings")) {
            setSelectedBottomMenu(R.id.settings);
            replaceFragment(new StudSettingsFragment());
        } else {
            setSelectedBottomMenu(R.id.home);
            replaceFragment(new StudHomeFragment());
        }
        // Add more types if needed
    }
}