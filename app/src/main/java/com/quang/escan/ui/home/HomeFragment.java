package com.quang.escan.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.quang.escan.R;
import com.quang.escan.databinding.FragmentHomeBinding;
import com.quang.escan.ui.scan.ImageSourceDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Home screen fragment - the main landing page of the application
 */
public class HomeFragment extends Fragment implements 
        ImageSourceDialogFragment.ImageSourceListener {

    private static final String TAG = "HomeFragment";
    private static final int REQUEST_IMAGE_PICK = 1;
    
    private FragmentHomeBinding binding;
    private NavController navController;
    private RecentFilesAdapter recentFilesAdapter;
    
    /**
     * Track which feature button was last clicked
     */
    private int lastClickedFeature = -1;
    private static final int FEATURE_EXTRACT_TEXT = 0;
    private static final int FEATURE_EXTRACT_HANDWRITING = 1;
    private static final int FEATURE_WATERMARK = 2;
    private static final int FEATURE_QR_SCAN = 3;

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
        setupFeatureButtons();
        
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
     * Set up feature buttons
     */
    private void setupFeatureButtons() {
        // Extract Text feature
        binding.featureExtractText.setOnClickListener(v -> {
            Log.d(TAG, "Extract Text button clicked");
            lastClickedFeature = FEATURE_EXTRACT_TEXT;
            showImageSourceDialog();
        });
        
        // Extract Handwriting feature
        binding.featureExtractHandwriting.setOnClickListener(v -> {
            Log.d(TAG, "Extract Handwriting button clicked");
            lastClickedFeature = FEATURE_EXTRACT_HANDWRITING;
            showImageSourceDialog();
        });
        
        // Watermark feature
        binding.featureWatermark.setOnClickListener(v -> {
            Log.d(TAG, "Watermark button clicked");
            lastClickedFeature = FEATURE_WATERMARK;
            // Implement watermark feature
            Toast.makeText(requireContext(), "Watermark feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        // QR Scan feature
        binding.featureQrScan.setOnClickListener(v -> {
            Log.d(TAG, "QR Scan button clicked");
            lastClickedFeature = FEATURE_QR_SCAN;
            // Navigate directly to dedicated QR scan fragment
            navController.navigate(R.id.navigation_qr_scan);
        });
    }

    /**
     * Shows the image source selection dialog
     */
    private void showImageSourceDialog() {
        ImageSourceDialogFragment dialog = new ImageSourceDialogFragment();
        dialog.setImageSourceListener(this);
        dialog.show(getChildFragmentManager(), "ImageSourceDialog");
    }
    
    /**
     * Opens the gallery for image selection
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }
    
    /**
     * Handle camera selection from dialog
     */
    @Override
    public void onCameraSelected() {
        Log.d(TAG, "Camera option selected");
        
        // Navigate to scan fragment with appropriate flag
        Bundle args = new Bundle();
        
        if (lastClickedFeature == FEATURE_QR_SCAN) {
            // For QR scanning, use dedicated fragment
            navController.navigate(R.id.navigation_qr_scan);
            return;
        }
        
        // For both text and handwriting recognition, use for_text_recognition flag
        if (lastClickedFeature == FEATURE_EXTRACT_TEXT || lastClickedFeature == FEATURE_EXTRACT_HANDWRITING) {
            args.putBoolean("for_text_recognition", true);
            // Store the feature type to distinguish between text and handwriting later
            args.putInt("feature_type", lastClickedFeature);
        }
        
        navController.navigate(R.id.navigation_scan, args);
    }
    
    /**
     * Handle gallery selection from dialog
     */
    @Override
    public void onGallerySelected() {
        Log.d(TAG, "Gallery option selected");
        openGallery();
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

    /**
     * Handle activity result for gallery image picking
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        try {
            if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
                Uri selectedImageUri = data.getData();
                
                if (selectedImageUri != null) {
                    Log.d(TAG, "Image picked from gallery: " + selectedImageUri);
                    
                    // Navigate to the image edit fragment with appropriate flags
                    Bundle args = new Bundle();
                    args.putString("imagePath", selectedImageUri.toString());
                    
                    // For both text and handwriting recognition, use for_text_recognition flag
                    if (lastClickedFeature == FEATURE_EXTRACT_TEXT || lastClickedFeature == FEATURE_EXTRACT_HANDWRITING) {
                        args.putBoolean("for_text_recognition", true);
                        // Store the feature type to distinguish between text and handwriting later
                        args.putInt("feature_type", lastClickedFeature);
                    }
                    // For QR scan
                    else if (lastClickedFeature == FEATURE_QR_SCAN) {
                        // For QR scan, we'll use the path to create a File in the QrScanFragment
                        // and process it directly
                        String imagePath = getPathFromUri(selectedImageUri);
                        Bundle qrArgs = new Bundle();
                        qrArgs.putString("image_uri", selectedImageUri.toString());
                        navController.navigate(R.id.navigation_qr_scan, qrArgs);
                        return;
                    }
                    
                    Log.d(TAG, "Navigating to image edit with args: " + args);
                    navController.navigate(R.id.navigation_image_edit, args);
                } else {
                    Log.e(TAG, "Selected image URI is null");
                    Toast.makeText(requireContext(), "Failed to get selected image", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onActivityResult", e);
            Toast.makeText(requireContext(), "Error processing selected image: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Get file path from URI
     */
    private String getPathFromUri(Uri uri) {
        try {
            if ("content".equals(uri.getScheme())) {
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = requireActivity().getContentResolver().query(
                        uri, projection, null, null, null);
                
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    String path = cursor.getString(columnIndex);
                    cursor.close();
                    return path;
                }
            } else if ("file".equals(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file path from URI", e);
        }
        
        // Return the URI string as a fallback
        return uri.toString();
    }
    
    // Define inner interface for image source dialog listeners
    public interface ImageSourceListener {
        void onCameraSelected();
        void onGallerySelected();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Cleaning up home fragment");
        binding = null;
    }
} 