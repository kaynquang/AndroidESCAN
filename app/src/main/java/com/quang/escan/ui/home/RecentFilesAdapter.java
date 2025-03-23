package com.quang.escan.ui.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quang.escan.R;
import com.quang.escan.ui.library.DocumentDetailActivity;

import java.util.List;

/**
 * Adapter for displaying recent files in a RecyclerView
 */
public class RecentFilesAdapter extends RecyclerView.Adapter<RecentFilesAdapter.FileViewHolder> {

    private List<RecentFile> recentFiles;

    public RecentFilesAdapter(List<RecentFile> recentFiles) {
        this.recentFiles = recentFiles;
    }
    
    /**
     * Update the data in the adapter
     * @param newData The new data to display
     */
    public void updateData(List<RecentFile> newData) {
        this.recentFiles = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        RecentFile file = recentFiles.get(position);
        
        // Set file name and date
        holder.fileName.setText(file.getFileName());
        holder.fileDate.setText(file.getDateModified());
        
        // Set thumbnail if available
        if (file.getThumbnail() != null) {
            holder.fileThumbnail.setImageBitmap(file.getThumbnail());
        } else {
            // Set a default thumbnail for the file type (PDF, image, etc.)
            holder.fileThumbnail.setImageResource(R.drawable.ic_library);
        }
        
        // Set click listeners for buttons
        holder.shareButton.setOnClickListener(v -> {
            // Handle share action
            String fileName = file.getFileName();
            Toast.makeText(v.getContext(), "Sharing " + fileName, Toast.LENGTH_SHORT).show();
            // TODO: Implement sharing functionality
        });
        
        holder.moreButton.setOnClickListener(v -> {
            // Show more options menu
            Toast.makeText(v.getContext(), "More options", Toast.LENGTH_SHORT).show();
            // TODO: Implement options menu
        });
        
        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            // Open the file in DocumentDetailActivity
            long documentId = file.getTag();
            Intent intent = new Intent(v.getContext(), DocumentDetailActivity.class);
            intent.putExtra(DocumentDetailActivity.EXTRA_DOCUMENT_ID, documentId);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recentFiles.size();
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView fileThumbnail;
        TextView fileName;
        TextView fileDate;
        ImageButton shareButton;
        ImageButton moreButton;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileThumbnail = itemView.findViewById(R.id.image_file_thumbnail);
            fileName = itemView.findViewById(R.id.text_file_name);
            fileDate = itemView.findViewById(R.id.text_file_date);
            shareButton = itemView.findViewById(R.id.btn_share);
            moreButton = itemView.findViewById(R.id.btn_more);
        }
    }
} 