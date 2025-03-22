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
        binding.recyclerRecentFiles.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Create and set adapter
        recentFilesAdapter = new RecentFilesAdapter(generateSampleFiles());
        binding.recyclerRecentFiles.setAdapter(recentFilesAdapter);
    }
    
    private List<RecentFile> generateSampleFiles() {
        // Generate some sample files for demonstration
        List<RecentFile> files = new ArrayList<>();
        
        // Sample formats
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
        
        // Add sample files
        files.add(new RecentFile(
                "Job Application Letter", 
                dateFormat.format(new Date(System.currentTimeMillis() - 86400000)), // Yesterday
                null));
        
        files.add(new RecentFile(
                "Requirements Document", 
                dateFormat.format(new Date(System.currentTimeMillis() - 172800000)), // 2 days ago
                null));
        
        files.add(new RecentFile(
                "Recommendation Letter", 
                dateFormat.format(new Date(System.currentTimeMillis() - 259200000)), // 3 days ago
                null));
        
        return files;
    }

    private void setupClickListeners() {
        // Search button
        binding.btnSearch.setOnClickListener(v -> {
            Log.d(TAG, "Search clicked");
            // Handle search action
        });
        
        // Feature buttons
        binding.featureScanCode.setOnClickListener(v -> {
            Log.d(TAG, "Scan Code clicked");
            navController.navigate(R.id.navigation_scan);
        });
        
        binding.featureWatermark.setOnClickListener(v -> {
            Log.d(TAG, "Watermark clicked");
            // Watermark feature implementation
        });
        
        binding.featureEsignPdf.setOnClickListener(v -> {
            Log.d(TAG, "eSign PDF clicked");
            // eSign PDF feature implementation
        });
        
        binding.featureSplitPdf.setOnClickListener(v -> {
            Log.d(TAG, "Split PDF clicked");
            // Split PDF feature implementation
        });
        
        binding.featureMergePdf.setOnClickListener(v -> {
            Log.d(TAG, "Merge PDF clicked");
            // Merge PDF feature implementation
        });
        
        binding.featureProtectPdf.setOnClickListener(v -> {
            Log.d(TAG, "Protect PDF clicked");
            // Protect PDF feature implementation
        });
        
        binding.featureCompressPdf.setOnClickListener(v -> {
            Log.d(TAG, "Compress PDF clicked");
            // Compress PDF feature implementation
        });
        
        binding.featureAllTools.setOnClickListener(v -> {
            Log.d(TAG, "All Tools clicked");
            showAllFeatures();
        });
        
        // View all files button
        binding.btnViewAllFiles.setOnClickListener(v -> {
            Log.d(TAG, "View All Files clicked");
            // Navigate to files screen
        });
        
        // Floating action buttons
        binding.fabScan.setOnClickListener(v -> {
            Log.d(TAG, "Scan FAB clicked");
            navController.navigate(R.id.navigation_scan);
        });
        
        binding.fabImport.setOnClickListener(v -> {
            Log.d(TAG, "Import FAB clicked");
            // Import from gallery implementation
        });
    }

    /**
     * Shows all available features in a dialog or new screen
     */
    private void showAllFeatures() {
        // Create and show a dialog with all features
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("All Tools")
            .setItems(new String[]{
                "Scan Code",
                "Watermark",
                "eSign PDF",
                "Split PDF",
                "Merge PDF",
                "Protect PDF",
                "Compress PDF",
                "OCR (Text Recognition)",
                "PDF to Word",
                "Word to PDF",
                "Image to PDF"
            }, (dialog, which) -> {
                // Handle item selection if needed
                Log.d(TAG, "Feature selected: " + which);
                // Implement feature selection handling
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