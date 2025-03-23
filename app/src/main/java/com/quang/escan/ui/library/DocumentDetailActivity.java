package com.quang.escan.ui.library;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.quang.escan.databinding.ActivityDocumentDetailBinding;
import com.quang.escan.model.ExtractedDocument;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Activity for displaying document details
 */
public class DocumentDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DOCUMENT_ID = "extra_document_id";
    private static final String TAG = "DocumentDetailActivity";
    
    private ActivityDocumentDetailBinding binding;
    private LibraryRepository libraryRepository;
    private ExtractedDocument document;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDocumentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize repository
        libraryRepository = new LibraryRepository(this);
        
        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Get document ID from intent
        long documentId = getIntent().getLongExtra(EXTRA_DOCUMENT_ID, -1);
        if (documentId == -1) {
            Toast.makeText(this, "Error: Document not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Load document
        document = libraryRepository.getDocumentById(documentId);
        if (document == null) {
            Toast.makeText(this, "Error: Document not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Display document details
        displayDocumentDetails();
        
        // Set up click listeners
        setupClickListeners();
    }
    
    /**
     * Display document details in the UI
     */
    private void displayDocumentDetails() {
        // Set document image if available
        if (!TextUtils.isEmpty(document.getImagePath())) {
            File imgFile = new File(document.getImagePath());
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(document.getImagePath());
                binding.imageDocument.setImageBitmap(bitmap);
            }
        }
        
        // Set filename
        binding.textFilename.setText(document.getFileName());
        
        // Set category
        binding.textCategory.setText(document.getCategory());
        
        // Format and set date
        String formattedDate = new SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault())
                .format(document.getCreationDate());
        binding.textDate.setText(formattedDate);
        
        // Set extracted text content
        binding.textContent.setText(document.getExtractedText());
    }
    
    /**
     * Set up click listeners for actions
     */
    private void setupClickListeners() {
        // Copy button
        binding.btnCopy.setOnClickListener(v -> {
            String content = document.getExtractedText();
            if (!TextUtils.isEmpty(content)) {
                android.content.ClipboardManager clipboard = 
                        (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = 
                        android.content.ClipData.newPlainText("Extracted Text", content);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Share button
        binding.btnShare.setOnClickListener(v -> {
            String content = document.getExtractedText();
            if (!TextUtils.isEmpty(content)) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, content);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            }
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 