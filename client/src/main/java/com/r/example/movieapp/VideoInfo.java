package com.r.example.movieapp;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class VideoInfo {
    private String videoUrl, title, description;
    public VideoInfo() {}
    public String getVideoUrl() { return videoUrl; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
