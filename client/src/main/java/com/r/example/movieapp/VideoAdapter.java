package com.r.example.movieapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    private final List<VideoInfo> videos;
    public VideoAdapter(List<VideoInfo> videos) { this.videos = videos; }
    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoInfo video = videos.get(position);
        holder.title.setText(video.getTitle()); holder.description.setText(video.getDescription());
        holder.itemView.setOnClickListener(view -> {
            int current = holder.getBindingAdapterPosition();
            if (current == RecyclerView.NO_POSITION) return;
            VideoInfo selected = videos.get(current);
            Intent intent = new Intent(view.getContext(), VideoViewActivity.class);
            intent.putExtra("videoUrl", selected.getVideoUrl());
            intent.putExtra("title", selected.getTitle());
            intent.putExtra("description", selected.getDescription());
            view.getContext().startActivity(intent);
        });
    }
    @Override public int getItemCount() { return videos.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title, description;
        ViewHolder(View view) {
            super(view); title = view.findViewById(R.id.txtTitle); description = view.findViewById(R.id.txtDescription);
        }
    }
}
