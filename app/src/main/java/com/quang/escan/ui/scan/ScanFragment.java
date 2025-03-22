package com.quang.escan.ui.scan;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.Navigation;

import com.google.common.util.concurrent.ListenableFuture;
import com.quang.escan.R;
import com.quang.escan.databinding.FragmentScanBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment for the document scanning functionality
 * Handles camera preview, photo capture, and scan options
 */
public class ScanFragment extends Fragment {

    private static final String TAG = "ScanFragment";
    private FragmentScanBinding binding;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private boolean flashEnabled = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating scan fragment view");
        binding = FragmentScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Setting up scan fragment");

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Set up UI interactions
        setupClickListeners();

        // Request camera permissions and start camera
        if (allPermissionsGranted()) {
            Log.d(TAG, "Permissions already granted, starting camera");
            startCamera();
        } else {
            Log.d(TAG, "Requesting camera permissions");
            ActivityCompat.requestPermissions(
                    requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    /**
     * Set up all UI interaction listeners
     */
    private void setupClickListeners() {
        try {
            Log.d(TAG, "Setting up click listeners");
            
            // Capture button
            binding.btnCapture.setOnClickListener(v -> {
                Log.d(TAG, "Capture button clicked");
                takePhoto();
            });

            // Gallery button
            binding.btnGallery.setOnClickListener(v -> {
                Log.d(TAG, "Gallery button clicked");
                Toast.makeText(requireContext(), "Gallery feature coming soon", Toast.LENGTH_SHORT).show();
            });

            // Scan mode button
            binding.btnMode.setOnClickListener(v -> {
                Log.d(TAG, "Mode button clicked");
                Toast.makeText(requireContext(), "Scan mode changed", Toast.LENGTH_SHORT).show();
            });
            
            // Back navigation
            binding.toolbar.setNavigationOnClickListener(v -> {
                Log.d(TAG, "Navigation back clicked");
                Navigation.findNavController(requireView()).navigateUp();
            });
            
            // Flash toggle
            binding.btnFlash.setOnClickListener(v -> {
                Log.d(TAG, "Flash button clicked");
                flashEnabled = !flashEnabled;
                Toast.makeText(requireContext(), 
                        flashEnabled ? "Flash enabled" : "Flash disabled", 
                        Toast.LENGTH_SHORT).show();
                // Flash implementation would be here
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
        }
    }

    /**
     * Capture a photo using CameraX
     */
    private void takePhoto() {
        if (imageCapture == null) {
            Log.e(TAG, "Cannot take photo, imageCapture is null");
            Toast.makeText(requireContext(), "Camera not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create timestamped file
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                    .format(System.currentTimeMillis());
            String fileName = "SCAN_" + timestamp + ".jpg";
            File photoFile = new File(requireContext().getExternalFilesDir(null), fileName);
            Log.d(TAG, "Photo will be saved to: " + photoFile.getAbsolutePath());

            // Create output options object
            ImageCapture.OutputFileOptions outputOptions =
                    new ImageCapture.OutputFileOptions.Builder(photoFile).build();

            // Capture the image
            imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(requireContext()),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            if (getContext() != null) {
                                Log.d(TAG, "Photo capture succeeded");
                                Toast.makeText(requireContext(), 
                                        "Photo saved: " + photoFile.getName(), 
                                        Toast.LENGTH_SHORT).show();
                                
                                // Future: Navigate to edit/preview fragment with captured image
                            }
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            if (getContext() != null) {
                                Log.e(TAG, "Photo capture failed", exception);
                                Toast.makeText(requireContext(), 
                                        "Photo capture failed: " + exception.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error taking photo", e);
            if (getContext() != null) {
                Toast.makeText(requireContext(), 
                        "Error taking photo: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Initialize and start the camera
     */
    private void startCamera() {
        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null, cannot start camera");
            return;
        }
        
        try {
            // Get the camera provider
            ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
                    ProcessCameraProvider.getInstance(context);

            cameraProviderFuture.addListener(() -> {
                try {
                    // Check if fragment is still attached
                    if (!isAdded() || getContext() == null) {
                        Log.d(TAG, "Fragment not attached, aborting camera start");
                        return;
                    }
                    
                    // Get camera provider
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    // Create preview use case
                    Preview preview = new Preview.Builder().build();
                    
                    if (binding.previewView == null) {
                        Log.e(TAG, "Preview view is null");
                        return;
                    }
                    
                    preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                    // Create image capture use case
                    imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build();

                    // Select back camera
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                    // Unbind any existing use cases
                    cameraProvider.unbindAll();

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                            (LifecycleOwner) this, cameraSelector, preview, imageCapture);
                    
                    Log.d(TAG, "Camera successfully initialized");

                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Error starting camera", e);
                    showToast("Error starting camera: " + e.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Camera error", e);
                    showToast("Camera error: " + e.getMessage());
                }
            }, ContextCompat.getMainExecutor(context));
        } catch (Exception e) {
            Log.e(TAG, "Error accessing camera", e);
            showToast("Error accessing camera: " + e.getMessage());
        }
    }

    /**
     * Check if all required permissions are granted
     */
    private boolean allPermissionsGranted() {
        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context is null, cannot check permissions");
            return false;
        }
        
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Handle permission request results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted, starting camera");
                startCamera();
            } else {
                Log.e(TAG, "Camera permission denied");
                showToast("Camera permission is required for scanning documents");
            }
        }
    }
    
    /**
     * Helper method for showing toasts only when fragment is attached
     */
    private void showToast(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Cleaning up resources");
        cameraExecutor.shutdown();
        binding = null;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Scan fragment resumed");
        if (allPermissionsGranted() && imageCapture == null) {
            Log.d(TAG, "Starting camera on resume");
            startCamera();
        }
    }
}