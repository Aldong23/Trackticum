package com.example.trackticum.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.trackticum.R;
import com.example.trackticum.databinding.ActivityComMainBinding;
import com.example.trackticum.databinding.ActivityComMainBinding;
import com.example.trackticum.fragments.ComHomeFragment;
import com.example.trackticum.fragments.ComProfileFragment;
import com.example.trackticum.fragments.ComSettingsFragment;

public class ComMainActivity extends AppCompatActivity {

    ActivityComMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityComMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new ComHomeFragment());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if(itemId == R.id.home){
                replaceFragment(new ComHomeFragment());
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
}