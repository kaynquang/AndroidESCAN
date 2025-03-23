package com.quang.escan.ui.translate;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.quang.escan.R;
import com.quang.escan.databinding.FragmentTranslateBinding;
import com.quang.escan.ui.library.DocumentSelectorDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TranslateFragment extends Fragment implements DocumentSelectorDialogFragment.DocumentTextListener {

    private static final String TAG = "TranslateFragment";
    private FragmentTranslateBinding binding;
    private Translator translator;
    
    // Default language codes
    private String sourceLanguageCode = TranslateLanguage.ENGLISH;
    private String targetLanguageCode = TranslateLanguage.INDONESIAN;
    
    // Available languages
    private List<LanguageItem> availableLanguages;
    
    // Track if models are downloaded
    private boolean isModelDownloaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTranslateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize language list
        initLanguageList();
        
        // Set flag images
        binding.sourceFlag.setImageResource(R.drawable.flag_us);
        binding.targetFlag.setImageResource(R.drawable.flag_indonesia);
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup text change listener
        setupTextWatcher();
        
        // Create translator with initial languages
        createTranslator();
    }
    
    private void initLanguageList() {
        availableLanguages = new ArrayList<>();
        
        // Add some common languages - you can extend this list
        availableLanguages.add(new LanguageItem(TranslateLanguage.ENGLISH, "English", R.drawable.flag_us));
        availableLanguages.add(new LanguageItem(TranslateLanguage.INDONESIAN, "Indonesian", R.drawable.flag_indonesia));
        // Add more languages here
    }
    
    private void setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
        
        // Source language selection
        binding.sourceLanguageCard.setOnClickListener(v -> {
            showLanguageSelectionDialog(true);
        });
        
        // Target language selection
        binding.targetLanguageCard.setOnClickListener(v -> {
            showLanguageSelectionDialog(false);
        });
        
        // Swap languages
        binding.swapLanguages.setOnClickListener(v -> {
            swapLanguages();
        });
        
        // Copy translated text
        binding.btnCopy.setOnClickListener(v -> {
            copyTextToClipboard(binding.targetText.getText().toString());
        });
        
        // Library selection
        binding.btnFromLibrary.setOnClickListener(v -> {
            showDocumentSelector();
        });
    }
    
    private void setupTextWatcher() {
        binding.sourceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateCharCount(s.toString());
                translateText(s.toString());
            }
        });
    }
    
    private void updateCharCount(String text) {
        int charCount = text.length();
        binding.charCount.setText(String.format(Locale.getDefault(), "%d / 5,000", charCount));
    }
    
    private void updateTranslatedCharCount(String text) {
        int charCount = text.length();
        binding.translatedCharCount.setText(String.format(Locale.getDefault(), "%d / 5,000", charCount));
    }
    
    private void createTranslator() {
        // Close previous translator if it exists
        if (translator != null) {
            translator.close();
        }
        
        // Create translator options
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(targetLanguageCode)
                .build();
        
        // Get translator
        translator = Translation.getClient(options);
        
        // Check if model is downloaded
        checkModelDownloaded();
    }
    
    private void checkModelDownloaded() {
        RemoteModelManager modelManager = RemoteModelManager.getInstance();
        
        // Check downloaded models
        modelManager.getDownloadedModels(TranslateRemoteModel.class)
                .addOnSuccessListener(models -> {
                    boolean sourceLanguageDownloaded = false;
                    boolean targetLanguageDownloaded = false;
                    
                    for (TranslateRemoteModel model : models) {
                        if (model.getLanguage().equals(sourceLanguageCode)) {
                            sourceLanguageDownloaded = true;
                        }
                        if (model.getLanguage().equals(targetLanguageCode)) {
                            targetLanguageDownloaded = true;
                        }
                    }
                    
                    isModelDownloaded = sourceLanguageDownloaded && targetLanguageDownloaded;
                    
                    // If models are not downloaded, download them
                    if (!isModelDownloaded) {
                        downloadLanguageModels();
                    } else {
                        // Translate any existing text in the source field
                        String text = binding.sourceText.getText().toString();
                        if (!text.isEmpty()) {
                            translateText(text);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking downloaded models", e);
                    Toast.makeText(requireContext(), "Error checking language models", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void downloadLanguageModels() {
        // Show loading indicator or message
        Toast.makeText(requireContext(), "Downloading language models...", Toast.LENGTH_SHORT).show();
        
        // Download source language model if needed
        TranslateRemoteModel sourceModel = new TranslateRemoteModel.Builder(sourceLanguageCode).build();
        DownloadConditions conditions = new DownloadConditions.Builder().build();
        
        RemoteModelManager modelManager = RemoteModelManager.getInstance();
        modelManager.download(sourceModel, conditions)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Source language model downloaded successfully");
                    // Download target language model
                    downloadTargetLanguageModel();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error downloading source language model", e);
                    Toast.makeText(requireContext(), "Error downloading language model", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void downloadTargetLanguageModel() {
        TranslateRemoteModel targetModel = new TranslateRemoteModel.Builder(targetLanguageCode).build();
        DownloadConditions conditions = new DownloadConditions.Builder().build();
        
        RemoteModelManager modelManager = RemoteModelManager.getInstance();
        modelManager.download(targetModel, conditions)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Target language model downloaded successfully");
                    isModelDownloaded = true;
                    Toast.makeText(requireContext(), "Language models downloaded successfully", Toast.LENGTH_SHORT).show();
                    
                    // Translate any existing text in the source field
                    String text = binding.sourceText.getText().toString();
                    if (!text.isEmpty()) {
                        translateText(text);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error downloading target language model", e);
                    Toast.makeText(requireContext(), "Error downloading language model", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void translateText(String text) {
        if (text.isEmpty()) {
            binding.targetText.setText("");
            updateTranslatedCharCount("");
            return;
        }
        
        if (!isModelDownloaded) {
            binding.targetText.setText("Downloading language models...");
            return;
        }
        
        binding.targetText.setText("Translating...");
        
        translator.translate(text)
                .addOnSuccessListener(translatedText -> {
                    binding.targetText.setText(translatedText);
                    updateTranslatedCharCount(translatedText);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error translating text", e);
                    binding.targetText.setText("Translation error: " + e.getMessage());
                });
    }
    
    private void showLanguageSelectionDialog(boolean isSourceLanguage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(isSourceLanguage ? "Select Source Language" : "Select Target Language");
        
        // Create adapter for language list
        ArrayAdapter<LanguageItem> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                availableLanguages
        );
        
        builder.setAdapter(adapter, (dialog, which) -> {
            LanguageItem selectedLanguage = availableLanguages.get(which);
            
            if (isSourceLanguage) {
                sourceLanguageCode = selectedLanguage.getCode();
                binding.sourceLanguage.setText(selectedLanguage.getName());
                binding.sourceFlag.setImageResource(selectedLanguage.getFlagResource());
            } else {
                targetLanguageCode = selectedLanguage.getCode();
                binding.targetLanguage.setText(selectedLanguage.getName());
                binding.targetFlag.setImageResource(selectedLanguage.getFlagResource());
            }
            
            // Create new translator with updated languages
            createTranslator();
            
            // Retranslate existing text
            String text = binding.sourceText.getText().toString();
            if (!text.isEmpty()) {
                translateText(text);
            }
        });
        
        builder.show();
    }
    
    private void swapLanguages() {
        // Swap language codes
        String tempCode = sourceLanguageCode;
        sourceLanguageCode = targetLanguageCode;
        targetLanguageCode = tempCode;
        
        // Swap language names and flags
        String tempName = binding.sourceLanguage.getText().toString();
        binding.sourceLanguage.setText(binding.targetLanguage.getText());
        binding.targetLanguage.setText(tempName);
        
        // Get flag resources
        LanguageItem sourceItem = findLanguageByCode(sourceLanguageCode);
        LanguageItem targetItem = findLanguageByCode(targetLanguageCode);
        
        if (sourceItem != null) {
            binding.sourceFlag.setImageResource(sourceItem.getFlagResource());
        }
        
        if (targetItem != null) {
            binding.targetFlag.setImageResource(targetItem.getFlagResource());
        }
        
        // Create new translator with swapped languages
        createTranslator();
        
        // Swap text content
        String sourceText = binding.sourceText.getText().toString();
        String targetText = binding.targetText.getText().toString();
        
        if (!targetText.isEmpty() && !targetText.equals("Translating...")) {
            binding.sourceText.setText(targetText);
            // translateText will be triggered by the text change listener
        }
    }
    
    private LanguageItem findLanguageByCode(String code) {
        for (LanguageItem item : availableLanguages) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
    
    private void copyTextToClipboard(String text) {
        if (text.isEmpty()) {
            return;
        }
        
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Translated Text", text);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(requireContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }
    
    private void showDocumentSelector() {
        DocumentSelectorDialogFragment dialogFragment = new DocumentSelectorDialogFragment();
        dialogFragment.setDocumentTextListener(this);
        dialogFragment.show(getChildFragmentManager(), "DocumentSelectorDialog");
    }
    
    @Override
    public void onDocumentTextSelected(String text) {
        binding.sourceText.setText(text);
        // translateText will be called by text change listener
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Close translator to free up resources
        if (translator != null) {
            translator.close();
        }
        
        binding = null;
    }
    
    /**
     * Class to represent a language item with its code, name, and flag resource
     */
    private static class LanguageItem {
        private final String code;
        private final String name;
        private final int flagResource;
        
        public LanguageItem(String code, String name, int flagResource) {
            this.code = code;
            this.name = name;
            this.flagResource = flagResource;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getName() {
            return name;
        }
        
        public int getFlagResource() {
            return flagResource;
        }
        
        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }
} 