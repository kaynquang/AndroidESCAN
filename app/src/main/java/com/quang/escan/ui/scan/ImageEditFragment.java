package com.quang.escan.ui.scan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.quang.escan.R;
import com.quang.escan.databinding.FragmentImageEditBinding;

import java.io.File;
import java.io.InputStream;

/**
 * Fragment for editing a captured image
 * Provides features like rotation, cropping, and navigation
 */
public class ImageEditFragment extends Fragment {

    private static final String TAG = "ImageEditFragment";
    private static final String ARG_IMAGE_PATH = "imagePath";
    private static final String ARG_FOR_TEXT_RECOGNITION = "for_text_recognition";
    private static final String ARG_FEATURE_TYPE = "feature_type";
    
    private FragmentImageEditBinding binding;
    private NavController navController;
    private String imagePath;
    private Bitmap originalBitmap;
    private Bitmap currentBitmap;
    private int rotationDegrees = 0;
    private boolean isForTextRecognition = false;
    private int featureType = -1;

    /**
     * Create a new instance of the fragment with image path as argument
     */
    public static ImageEditFragment newInstance(String imagePath, boolean forTextRecognition, int featureType) {
        ImageEditFragment fragment = new ImageEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_PATH, imagePath);
        args.putBoolean(ARG_FOR_TEXT_RECOGNITION, forTextRecognition);
        args.putInt(ARG_FEATURE_TYPE, featureType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imagePath = getArguments().getString(ARG_IMAGE_PATH);
            isForTextRecognition = getArguments().getBoolean(ARG_FOR_TEXT_RECOGNITION, false);
            featureType = getArguments().getInt(ARG_FEATURE_TYPE, -1);
            Log.d(TAG, "Received image path: " + imagePath + 
                       ", forTextRecognition: " + isForTextRecognition + 
                       ", featureType: " + featureType);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating image edit fragment view");
        binding = FragmentImageEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Setting up image edit fragment");
        
        navController = Navigation.findNavController(view);
        
        // Set up toolbar
        binding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        
        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            imagePath = args.getString("imagePath");
            isForTextRecognition = args.getBoolean("for_text_recognition", false);
            featureType = args.getInt("feature_type", -1);
        }
        
        // Load and display the image
        if (imagePath != null) {
            loadImage();
        } else {
            showToast("No image provided");
            navigateUp();
        }
        
        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Load image from path
     */
    private void loadImage() {
        if (imagePath == null || imagePath.isEmpty()) {
            Log.e(TAG, "No image path provided");
            showToast("Error: No image to edit");
            navController.navigateUp();
            return;
        }

        try {
            // First try to parse as a URI
            Uri imageUri = Uri.parse(imagePath);
            
            // Check if it's a content URI
            if (imageUri.getScheme() != null && (imageUri.getScheme().equals("content") || 
                                                 imageUri.getScheme().equals("file"))) {
                Log.d(TAG, "Loading image from URI: " + imageUri);
                
                // Load bitmap from content resolver
                try (InputStream stream = requireContext().getContentResolver().openInputStream(imageUri)) {
                    if (stream != null) {
                        originalBitmap = BitmapFactory.decodeStream(stream);
                    }
                }
            } else {
                // Treat as a file path
                File imageFile = new File(imagePath);
                if (!imageFile.exists()) {
                    Log.e(TAG, "Image file does not exist: " + imagePath);
                    showToast("Error: Image file not found");
                    navController.navigateUp();
                    return;
                }
                
                // Load bitmap from file
                originalBitmap = BitmapFactory.decodeFile(imagePath);
            }
            
            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from: " + imagePath);
                showToast("Error: Could not load image");
                navController.navigateUp();
                return;
            }

            currentBitmap = originalBitmap;
            binding.imagePreview.setImageBitmap(currentBitmap);
            Log.d(TAG, "Image loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
            showToast("Error loading image: " + e.getMessage());
            navController.navigateUp();
        }
    }

    /**
     * Setup click listeners for UI elements
     */
    private void setupClickListeners() {
        // Rotate image button
        binding.btnRotate.setOnClickListener(v -> rotateImage());
        
        // Crop image button
        binding.btnCrop.setOnClickListener(v -> cropImage());
        
        // Next button - navigate to appropriate activity based on flags
        binding.btnNext.setOnClickListener(v -> {
            if (imagePath == null) {
                showToast("Image not available");
                return;
            }
            
            if (isForTextRecognition) {
                // For both text and handwriting, use TextRecognitionActivity
                launchTextRecognition();
            } else {
                // Handle normal flow
                showToast("Image processed successfully");
                navigateUp();
            }
        });
    }

    /**
     * Rotate the image by 90 degrees clockwise
     */
    private void rotateImage() {
        if (currentBitmap == null) {
            Log.e(TAG, "Cannot rotate null bitmap");
            return;
        }

        try {
            rotationDegrees = (rotationDegrees + 90) % 360;
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            currentBitmap = Bitmap.createBitmap(
                    currentBitmap, 
                    0, 
                    0, 
                    currentBitmap.getWidth(), 
                    currentBitmap.getHeight(),
                    matrix, 
                    true);
            
            binding.imagePreview.setImageBitmap(currentBitmap);
            Log.d(TAG, "Image rotated to " + rotationDegrees + " degrees");
        } catch (Exception e) {
            Log.e(TAG, "Error rotating image", e);
            showToast("Error rotating image: " + e.getMessage());
        }
    }

    /**
     * Crop the image
     */
    private void cropImage() {
        // Implementation needed
        showToast("Crop feature coming soon");
    }

    /**
     * Launch the TextRecognitionActivity to process the image
     */
    private void launchTextRecognition() {
        try {
            Uri imageUri;
            if (imagePath.startsWith("content:") || imagePath.startsWith("file:")) {
                // Already a URI string
                imageUri = Uri.parse(imagePath);
            } else {
                // Convert file path to URI
                imageUri = Uri.fromFile(new File(imagePath));
            }
            
            Intent intent = new Intent(requireContext(), com.quang.escan.ui.ocr.TextRecognitionActivity.class);
            intent.putExtra(com.quang.escan.ui.ocr.TextRecognitionActivity.EXTRA_IMAGE_URI, imageUri.toString());
            // Pass feature type to distinguish between text and handwriting recognition
            intent.putExtra("feature_type", featureType);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error launching recognition", e);
            showToast("Error launching recognition: " + e.getMessage());
        }
    }

    /**
     * Show a toast message
     */
    private void showToast(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigate back to the previous screen
     */
    private void navigateUp() {
        if (navController != null) {
            navController.navigateUp();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Cleaning up resources");
        
        // Clean up bitmaps to avoid memory leaks
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
            originalBitmap = null;
        }
        
        if (currentBitmap != null && !currentBitmap.isRecycled() && currentBitmap != originalBitmap) {
            currentBitmap.recycle();
            currentBitmap = null;
        }
        
        binding = null;
    }
} 