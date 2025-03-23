package com.quang.escan.ui.document;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.quang.escan.R;
import com.quang.escan.data.model.Document;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying documents in a RecyclerView
 */
public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<Document> documents;
    private final DocumentClickListener clickListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface DocumentClickListener {
        void onDocumentClick(Document document);
    }

    public DocumentAdapter(List<Document> documents, DocumentClickListener clickListener) {
        this.documents = documents;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        Document document = documents.get(position);
        holder.titleTextView.setText(document.getTitle());
        
        // Truncate content if too long
        String content = document.getContent();
        if (content != null && content.length() > 100) {
            content = content.substring(0, 97) + "...";
        }
        holder.contentPreviewTextView.setText(content);
        
        // Format and set date
        if (document.getUpdatedAt() != null) {
            holder.dateTextView.setText(dateFormat.format(document.getUpdatedAt()));
        }
        
        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onDocumentClick(document);
            }
        });
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    /**
     * Update the adapter with new documents
     */
    public void updateDocuments(List<Document> newDocuments) {
        this.documents = newDocuments;
        notifyDataSetChanged();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView titleTextView;
        TextView contentPreviewTextView;
        TextView dateTextView;

        DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            titleTextView = itemView.findViewById(R.id.document_title);
            contentPreviewTextView = itemView.findViewById(R.id.document_preview);
            dateTextView = itemView.findViewById(R.id.document_date);
        }
    }
} 