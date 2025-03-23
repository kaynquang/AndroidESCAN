package com.quang.escan.ui.library;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.quang.escan.R;
import com.quang.escan.data.model.Document;
import com.quang.escan.databinding.FragmentDocumentSelectorBinding;
import com.quang.escan.ui.document.DocumentAdapter;
import com.quang.escan.ui.document.DocumentViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog fragment to select a document and extract its text content
 */
public class DocumentSelectorDialogFragment extends DialogFragment {

    private FragmentDocumentSelectorBinding binding;
    private DocumentViewModel viewModel;
    private DocumentAdapter adapter;
    private DocumentTextListener listener;

    public interface DocumentTextListener {
        void onDocumentTextSelected(String text);
    }

    public void setDocumentTextListener(DocumentTextListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_ESCAN_FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDocumentSelectorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup toolbar
        binding.toolbar.setTitle("Select Document");
        binding.toolbar.setNavigationOnClickListener(v -> dismiss());

        // Initialize RecyclerView
        adapter = new DocumentAdapter(new ArrayList<>(), this::onDocumentSelected);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DocumentViewModel.class);
        observeViewModel();
        
        // Load documents
        viewModel.loadDocuments();
    }

    private void observeViewModel() {
        viewModel.getDocumentsLiveData().observe(getViewLifecycleOwner(), documents -> {
            if (documents.isEmpty()) {
                binding.emptyStateText.setVisibility(View.VISIBLE);
            } else {
                binding.emptyStateText.setVisibility(View.GONE);
                adapter.updateDocuments(documents);
            }
        });

        viewModel.getLoadingStateLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            binding.loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onDocumentSelected(Document document) {
        // Extract text from document and pass to listener
        String text = document.getContent();
        
        if (text == null || text.isEmpty()) {
            Toast.makeText(requireContext(), "No text content found in document", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (listener != null) {
            listener.onDocumentTextSelected(text);
            dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 