package com.example.trackticum.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsetsController;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.trackticum.R;
import com.example.trackticum.databinding.ActivityComMainBinding;
import com.example.trackticum.databinding.ActivityComMainBinding;
import com.example.trackticum.fragments.ComHomeFragment;
import com.example.trackticum.fragments.ComNotificationFragment;
import com.example.trackticum.fragments.ComProfileFragment;
import com.example.trackticum.fragments.ComScanQrFragment;
import com.example.trackticum.fragments.ComSettingsFragment;
import com.example.trackticum.fragments.StudHomeFragment;
import com.example.trackticum.fragments.StudProfileFragment;
import com.example.trackticum.fragments.StudSettingsFragment;

public class ComMainActivity extends AppCompatActivity {

    ActivityComMainBinding binding;

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

        binding = ActivityComMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String notificationType = getIntent().getStringExtra("notification_type");
        if (notificationType != null) {
            handleNotificationType(notificationType);
        } else {
            replaceFragment(new ComHomeFragment());
        }

        binding.bottomNavigation.setItemActiveIndicatorColor(ContextCompat.getColorStateList(this, R.color.lighterTeal));

        binding.bottomNavigation.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if(itemId == R.id.home){
                replaceFragment(new ComHomeFragment());
            }else if(itemId == R.id.notification){
                replaceFragment(new ComNotificationFragment());
            }else if(itemId == R.id.scan_qr){
                replaceFragment(new ComScanQrFragment());
            }else if(itemId == R.id.profile){
                replaceFragment(new ComProfileFragment());
            }else if(itemId == R.id.settings){
                replaceFragment(new ComSettingsFragment());
            }

            return true;
        });
    }

    private void replaceFragment (Fragment fragment){
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
        if (type.equalsIgnoreCase("company_profile")) {
            setSelectedBottomMenu(R.id.profile);
            replaceFragment(new ComProfileFragment());
        } else {
            setSelectedBottomMenu(R.id.home);
            replaceFragment(new ComHomeFragment());
        }
        // Add more types if needed
    }
}