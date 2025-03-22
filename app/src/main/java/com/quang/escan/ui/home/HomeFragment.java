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
        // Set up quick action cards
        binding.actionScanDocument.setOnClickListener(v -> {
            Log.d(TAG, "Scan document clicked: Navigating to scan");
            navController.navigate(R.id.navigation_scan);
        });

        binding.actionImportImage.setOnClickListener(v -> {
            Log.d(TAG, "Import image clicked");
            // Navigate to gallery import (future implementation)
        });

        binding.actionPdfTools.setOnClickListener(v -> {
            Log.d(TAG, "PDF Convert clicked");
            // PDF tools feature (future implementation)
        });

        binding.actionInkDigital.setOnClickListener(v -> {
            Log.d(TAG, "Ink Digital clicked");
            // Digital ink feature (future implementation)
        });
        
        binding.actionQrScan.setOnClickListener(v -> {
            Log.d(TAG, "QR Scan clicked");
            // QR code scanning feature (future implementation)
        });
        
        binding.viewAllActions.setOnClickListener(v -> {
            Log.d(TAG, "View All clicked");
            // Show all features in a new screen or dialog
            showAllFeatures();
        });
        
        binding.fabHelp.setOnClickListener(v -> {
            Log.d(TAG, "Help FAB clicked");
            showAdminInfo();
        });
    }

    /**
     * Shows admin contact information in a dialog
     */
    private void showAdminInfo() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_info, null);
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Admin Information")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show();
    }

    /**
     * Shows all available features in a dialog or new screen
     */
    private void showAllFeatures() {
        // Create and show a dialog with all features
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("All Features")
            .setItems(new String[]{
                "Scan Now",
                "Import Image",
                "PDF Convert",
                "Handwriting Scan",
                "QR Scan"
            }, (dialog, which) -> {
                // Handle item selection if needed
                switch (which) {
                    case 0: // Scan Now
                        navController.navigate(R.id.navigation_scan);
                        break;
                    case 1: // Import Image
                        Log.d(TAG, "Import image selected from dialog");
                        break;
                    case 2: // PDF Convert
                        Log.d(TAG, "PDF Convert selected from dialog");
                        break;
                    case 3: // Handwriting Scan
                        Log.d(TAG, "Handwriting Scan selected from dialog");
                        break;
                    case 4: // QR Scan
                        Log.d(TAG, "QR Scan selected from dialog");
                        break;
                }
            })
            .setNegativeButton("Close", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Cleaning up home fragment");
        binding = null;
    }
} 