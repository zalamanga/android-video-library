package com.r.example.adminapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    public interface Actions { void edit(VideoInfo video); void delete(VideoInfo video); }
    private final List<VideoInfo> videos;
    private final Actions actions;
    public VideoAdapter(List<VideoInfo> videos, Actions actions) { this.videos = videos; this.actions = actions; }
    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoInfo video = videos.get(position);
        holder.title.setText(video.getTitle()); holder.description.setText(video.getDescription());
        holder.update.setOnClickListener(view -> {
            int current = holder.getBindingAdapterPosition();
            if (current != RecyclerView.NO_POSITION) actions.edit(videos.get(current));
        });
        holder.delete.setOnClickListener(view -> {
            int current = holder.getBindingAdapterPosition();
            if (current != RecyclerView.NO_POSITION) actions.delete(videos.get(current));
        });
    }
    @Override public int getItemCount() { return videos.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title, description;
        final Button update, delete;
        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.txtTitle); description = view.findViewById(R.id.txtDescription);
            update = view.findViewById(R.id.btnUpdate); delete = view.findViewById(R.id.btnDelete);
        }
    }
}
