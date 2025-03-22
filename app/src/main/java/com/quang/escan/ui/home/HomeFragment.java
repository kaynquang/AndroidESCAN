package com.quang.escan.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.quang.escan.R;
import com.quang.escan.databinding.FragmentHomeBinding;

/**
 * Home screen fragment - the main landing page of the application
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating home fragment view");
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Setting up home fragment");
        
        navController = Navigation.findNavController(view);
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Set up FAB for quick scan
        binding.fabScan.setOnClickListener(v -> {
            Log.d(TAG, "Fab clicked: Navigating to scan");
            navController.navigate(R.id.navigation_scan);
        });

        // Set up quick action cards
        binding.actionScanDocument.setOnClickListener(v -> {
            Log.d(TAG, "Scan document clicked: Navigating to scan");
            navController.navigate(R.id.navigation_scan);
        });

        binding.actionImportImage.setOnClickListener(v -> {
            Log.d(TAG, "Import image clicked");
            // Navigate to gallery import (future implementation)
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Cleaning up home fragment");
        binding = null;
    }
} 