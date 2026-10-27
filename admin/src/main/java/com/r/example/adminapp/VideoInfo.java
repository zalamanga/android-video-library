package com.r.example.adminapp;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class VideoInfo {
    private String videoUrl, title, description, storagePath, key;
    public VideoInfo() {}
    public VideoInfo(String videoUrl, String title, String description, String storagePath) {
        this.videoUrl = videoUrl; this.title = title;
        this.description = description; this.storagePath = storagePath;
    }
    public String getVideoUrl() { return videoUrl; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStoragePath() { return storagePath; }
    @Exclude public String getKey() { return key; }
    public void setKey(String value) { key = value; }
}
