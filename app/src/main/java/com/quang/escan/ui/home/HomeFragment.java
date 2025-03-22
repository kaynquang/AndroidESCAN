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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.quang.escan.R;
import com.quang.escan.databinding.FragmentHomeBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Home screen fragment - the main landing page of the application
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private NavController navController;
    private RecentFilesAdapter recentFilesAdapter;

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
        setupRecentFiles();
        setupClickListeners();
    }

    private void setupRecentFiles() {
        // Set up RecyclerView
        binding.recyclerviewRecentFiles.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Create and set adapter with empty list
        recentFilesAdapter = new RecentFilesAdapter(new ArrayList<>());
        binding.recyclerviewRecentFiles.setAdapter(recentFilesAdapter);
        
        // Show empty state view since we have no files
        binding.emptyState.setVisibility(View.VISIBLE);
        binding.recyclerviewRecentFiles.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        // Search button
        binding.btnSearch.setOnClickListener(v -> {
            Log.d(TAG, "Search clicked");
            // Handle search action
        });
        
        // Feature buttons
        binding.featureExtractText.setOnClickListener(v -> {
            Log.d(TAG, "Extract Text clicked");
            // Text extraction feature implementation
        });
        
        binding.featureExtractHandwriting.setOnClickListener(v -> {
            Log.d(TAG, "Extract Handwriting clicked");
            // Handwriting extraction feature implementation
        });
        
        binding.featureWatermark.setOnClickListener(v -> {
            Log.d(TAG, "Watermark clicked");
            // Watermark feature implementation
        });
        
        binding.featurePdfConvert.setOnClickListener(v -> {
            Log.d(TAG, "PDF Convert clicked");
            // PDF conversion feature implementation
        });
        
        binding.featureQrScan.setOnClickListener(v -> {
            Log.d(TAG, "QR Scan clicked");
            // QR code scanning implementation
        });
        
        binding.featureTranslate.setOnClickListener(v -> {
            Log.d(TAG, "Translate clicked");
            // Translation feature implementation
        });
        
        // Collapse recent files
        binding.btnCollapseRecent.setOnClickListener(v -> {
            Log.d(TAG, "Collapse Recent Files clicked");
            toggleRecentFilesVisibility();
        });
        
        // Contact Us button
        binding.fabContactUs.setOnClickListener(v -> {
            Log.d(TAG, "Contact Us clicked");
            showContactDialog();
        });
    }

    /**
     * Toggles the visibility of the recent files section
     */
    private void toggleRecentFilesVisibility() {
        if (binding.emptyState.getVisibility() == View.VISIBLE) {
            binding.emptyState.setVisibility(View.GONE);
            binding.btnCollapseRecent.setRotation(0); // Right arrow
        } else {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.btnCollapseRecent.setRotation(90); // Down arrow
        }
    }
    
    /**
     * Shows the contact dialog with support information
     */
    private void showContactDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Contact Us")
            .setMessage("Need help with the app? Contact our support team:\n\nEmail: support@escan.com\nPhone: +1-800-123-4567")
            .setPositiveButton("OK", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Cleaning up home fragment");
        binding = null;
    }
} 