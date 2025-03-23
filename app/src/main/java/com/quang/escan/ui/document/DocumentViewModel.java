package com.quang.escan.ui.document;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.quang.escan.data.model.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ViewModel for Document-related operations
 */
public class DocumentViewModel extends AndroidViewModel {
    private static final String TAG = "DocumentViewModel";
    
    private final MutableLiveData<List<Document>> documentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    
    public DocumentViewModel(@NonNull Application application) {
        super(application);
    }
    
    /**
     * Load documents, for now creating dummy data
     */
    public void loadDocuments() {
        loadingStateLiveData.setValue(true);
        
        try {
            // In a real app, this would load from a database or repository
            List<Document> documents = new ArrayList<>();
            
            // Add sample documents for testing
            documents.add(new Document(1, "Sample Document 1", 
                    "This is a sample document content for testing purposes.", 
                    "/storage/sample1.txt", new Date(), new Date(), "TEXT"));
            
            documents.add(new Document(2, "Sample Document 2", 
                    "Another sample document with different content. This one is a bit longer to show how text wrapping works.", 
                    "/storage/sample2.txt", new Date(), new Date(), "TEXT"));
            
            documents.add(new Document(3, "Sample Document 3", 
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec auctor, nisl eget ultricies lacinia, nisl nunc aliquam nisl, eget ultricies nisl nunc eget nisl.", 
                    "/storage/sample3.txt", new Date(), new Date(), "TEXT"));
            
            documentsLiveData.setValue(documents);
            errorLiveData.setValue(null);
        } catch (Exception e) {
            Log.e(TAG, "Error loading documents", e);
            errorLiveData.setValue("Error loading documents: " + e.getMessage());
        } finally {
            loadingStateLiveData.setValue(false);
        }
    }
    
    public LiveData<List<Document>> getDocumentsLiveData() {
        return documentsLiveData;
    }
    
    public LiveData<Boolean> getLoadingStateLiveData() {
        return loadingStateLiveData;
    }
    
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
} 