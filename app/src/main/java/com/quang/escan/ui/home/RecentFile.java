package com.quang.escan.ui.home;

import android.graphics.Bitmap;

/**
 * Model class representing a recent file in the app
 */
public class RecentFile {
    private String fileName;
    private String dateModified;
    private Bitmap thumbnail;
    private long tag; // Used to store document ID

    public RecentFile(String fileName, String dateModified, Bitmap thumbnail) {
        this.fileName = fileName;
        this.dateModified = dateModified;
        this.thumbnail = thumbnail;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }
    
    public long getTag() {
        return tag;
    }
    
    public void setTag(long tag) {
        this.tag = tag;
    }
} 