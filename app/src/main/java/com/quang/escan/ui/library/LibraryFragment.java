package com.quang.escan.ui.library;

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
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.quang.escan.R;
import com.quang.escan.databinding.FragmentLibraryBinding;

/**
 * Fragment for displaying and managing scanned documents
 * Provides filtering and organization of documents
 */
public class LibraryFragment extends Fragment {

    private static final String TAG = "LibraryFragment";
    private FragmentLibraryBinding binding;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating library fragment view");
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Setting up library fragment");
        
        navController = Navigation.findNavController(view);
        setupTabLayout();
        setupRecyclerView();
        setupClickListeners();
    }

    /**
     * Set up tab layout for document filtering
     */
    private void setupTabLayout() {
        Log.d(TAG, "Setting up tab layout");
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab selected: " + tab.getPosition());
                // Filter documents based on selected tab
                filterDocuments(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }

    /**
     * Filter documents based on the selected tab
     * @param tabPosition The position of the selected tab
     */
    private void filterDocuments(int tabPosition) {
        Log.d(TAG, "Filtering documents by tab position: " + tabPosition);
        // Future implementation to filter documents by type
        // 0 = All, 1 = Documents, 2 = Images, 3 = PDFs
        
        // For now, just display empty state if no documents
        checkIfEmpty(true); // Replace with actual check for documents
    }

    /**
     * Set up the recycler view for displaying documents
     */
    private void setupRecyclerView() {
        Log.d(TAG, "Setting up recycler view");
        binding.recyclerDocuments.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        // Future: set adapter for documents
        
        // Check if empty for initial state
        checkIfEmpty(true); // Replace with actual check for documents
    }

    /**
     * Show empty state or document list based on whether documents exist
     * @param isEmpty Whether there are no documents to display
     */
    private void checkIfEmpty(boolean isEmpty) {
        Log.d(TAG, "Checking if document list is empty: " + isEmpty);
        if (isEmpty) {
            binding.textEmpty.setVisibility(View.VISIBLE);
            binding.recyclerDocuments.setVisibility(View.GONE);
        } else {
            binding.textEmpty.setVisibility(View.GONE);
            binding.recyclerDocuments.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set up click listeners for UI interactions
     */
    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners");
        
        // Add button to create new scan
        binding.fabAdd.setOnClickListener(v -> {
            Log.d(TAG, "Add button clicked: Navigating to scan");
            navController.navigate(R.id.navigation_scan);
        });
        
        // Search button
        binding.btnSearch.setOnClickListener(v -> {
            Log.d(TAG, "Search button clicked");
            // Future: Implement search functionality
        });
        
        // Filter button
        binding.btnFilter.setOnClickListener(v -> {
            Log.d(TAG, "Filter button clicked");
            // Future: Implement additional filtering options
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Cleaning up library fragment");
        binding = null;
    }
} 