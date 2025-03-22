package com.quang.escan.ui.library;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.quang.escan.R;
import com.quang.escan.databinding.FragmentDocumentViewerBinding;
import com.quang.escan.model.ExtractedDocument;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Fragment for viewing document details
 * Shows the saved document with its image and extracted text
 */
public class DocumentViewerFragment extends Fragment {

    private static final String TAG = "DocumentViewerFragment";
    private static final String ARG_DOCUMENT_ID = "document_id";
    
    private FragmentDocumentViewerBinding binding;
    private LibraryRepository repository;
    private ExtractedDocument document;
    private long documentId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    
    /**
     * Create a new instance of the fragment with document ID
     */
    public static DocumentViewerFragment newInstance(long documentId) {
        DocumentViewerFragment fragment = new DocumentViewerFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DOCUMENT_ID, documentId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get document ID from arguments
        if (getArguments() != null) {
            documentId = getArguments().getLong(ARG_DOCUMENT_ID, -1);
        }
        
        // Initialize repository
        repository = new LibraryRepository(requireContext());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDocumentViewerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Setup toolbar
        setupToolbar();
        
        // Load document data
        loadDocument();
        
        // Setup button listeners
        setupClickListeners();
    }
    
    private void setupToolbar() {
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material); // Use system back arrow
        binding.toolbar.setNavigationOnClickListener(v -> navigateBack());
        
        // More options menu
        binding.btnMoreOptions.setOnClickListener(v -> showOptionsMenu());
    }
    
    private void loadDocument() {
        if (documentId == -1) {
            showError("Invalid document ID");
            return;
        }
        
        // Load document from repository
        document = repository.getDocumentById(documentId);
        
        if (document == null) {
            showError("Document not found");
            return;
        }
        
        // Display document info
        displayDocumentInfo();
        
        // Load and display image
        loadDocumentImage();
        
        // Display text content
        binding.textContent.setText(document.getExtractedText());
    }
    
    private void displayDocumentInfo() {
        // Set file name
        binding.textFileName.setText(document.getFileName());
        
        // Set category
        binding.textCategory.setText(document.getCategory());
        
        // Set date
        if (document.getCreationDate() != null) {
            binding.textDate.setText(dateFormat.format(document.getCreationDate()));
        } else {
            binding.textDate.setText("Unknown date");
        }
        
        // Set toolbar title to file name
        binding.toolbar.setTitle(document.getFileName());
    }
    
    private void loadDocumentImage() {
        String imagePath = document.getImagePath();
        
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                
                if (bitmap != null) {
                    binding.imagePreview.setImageBitmap(bitmap);
                    return;
                }
            }
        }
        
        // If no image or error loading, show placeholder
        binding.imagePreview.setImageResource(R.drawable.ic_verified);
    }
    
    private void setupClickListeners() {
        // Edit button
        binding.btnEdit.setOnClickListener(v -> {
            // In a real implementation, you'd launch an edit activity here
            Toast.makeText(requireContext(), "Edit functionality coming soon", Toast.LENGTH_SHORT).show();
        });
        
        // Share button
        binding.btnShare.setOnClickListener(v -> shareDocument());
        
        // Delete button
        binding.btnDelete.setOnClickListener(v -> confirmDelete());
    }
    
    private void shareDocument() {
        if (document == null) return;
        
        // Create intent to share text
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, document.getFileName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, document.getExtractedText());
        
        // If there's an image, add it as well
        if (document.getImagePath() != null && !document.getImagePath().isEmpty()) {
            File imageFile = new File(document.getImagePath());
            
            if (imageFile.exists()) {
                Uri imageUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".provider",
                        imageFile);
                
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.setType("image/jpeg");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share document via"));
    }
    
    private void confirmDelete() {
        if (document == null) return;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Document");
        builder.setMessage("Are you sure you want to delete '" + document.getFileName() + "'?");
        
        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Delete from repository
            boolean deleted = repository.deleteDocument(document.getId());
            
            if (deleted) {
                // Also delete the image file if it exists
                if (document.getImagePath() != null && !document.getImagePath().isEmpty()) {
                    File imageFile = new File(document.getImagePath());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }
                
                Toast.makeText(requireContext(), "Document deleted", Toast.LENGTH_SHORT).show();
                
                // Navigate back to library
                navigateBack();
            } else {
                Toast.makeText(requireContext(), "Error deleting document", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showOptionsMenu() {
        if (document == null) return;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(document.getFileName());
        
        String[] options = {"Copy Text", "Print", "Move to Category", "Rename"};
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    // Copy text to clipboard
                    android.content.ClipboardManager clipboard = 
                            (android.content.ClipboardManager) requireActivity().getSystemService(
                                    requireContext().CLIPBOARD_SERVICE);
                    android.content.ClipData clip = 
                            android.content.ClipData.newPlainText("Extracted Text", 
                                    document.getExtractedText());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(requireContext(), "Text copied to clipboard", 
                            Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    // Print functionality would go here
                    Toast.makeText(requireContext(), "Print functionality coming soon", 
                            Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    // Move to category functionality would go here
                    Toast.makeText(requireContext(), "Move functionality coming soon", 
                            Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    // Rename functionality would go here
                    Toast.makeText(requireContext(), "Rename functionality coming soon", 
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void navigateBack() {
        Navigation.findNavController(requireView()).navigateUp();
    }
    
    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        navigateBack();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 