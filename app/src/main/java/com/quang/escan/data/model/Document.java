package com.quang.escan.data.model;

import java.util.Date;

/**
 * Model class representing a document
 */
public class Document {
    private long id;
    private String title;
    private String content;
    private String filePath;
    private Date createdAt;
    private Date updatedAt;
    private String fileType;
    
    public Document() {
        // Default constructor
    }
    
    public Document(long id, String title, String content, String filePath, Date createdAt, Date updatedAt, String fileType) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.filePath = filePath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.fileType = fileType;
    }
    
    // Getters and setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
} 