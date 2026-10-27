package com.r.example.movieapp;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class VideoViewActivity extends AppCompatActivity {
    private VideoView player;
    private TextView status;
    private int position;
    private boolean prepared, active, resumePlayback = true;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!FirebaseSetup.requireUser(this, LoginActivity.class)) return;
        setContentView(R.layout.activity_video_view);
        ((TextView) findViewById(R.id.txtTitle)).setText(getIntent().getStringExtra("title"));
        ((TextView) findViewById(R.id.txtDescription)).setText(getIntent().getStringExtra("description"));
        status = findViewById(R.id.status);
        String value = getIntent().getStringExtra("videoUrl");
        Uri uri = value == null ? null : Uri.parse(value);
        if (uri == null || !"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            status.setText("URL video tidak valid. Pengelola perlu menyimpan URL download HTTPS.");
            return;
        }
        if (state != null) { position = state.getInt("position"); resumePlayback = state.getBoolean("playing", true); }
        player = findViewById(R.id.videoView);
        MediaController controls = new MediaController(this);
        controls.setAnchorView(player); player.setMediaController(controls);
        status.setText("Menyiapkan video...");
        player.setOnPreparedListener(media -> {
            prepared = true; status.setText("");
            if (position > 0) player.seekTo(position);
            if (active && resumePlayback) player.start();
        });
        player.setOnCompletionListener(media -> { resumePlayback = false; status.setText("Video selesai."); });
        player.setOnErrorListener((media, what, extra) -> {
            prepared = false;
            status.setText("Video gagal diputar. Periksa koneksi, URL, dan dukungan format Android.");
            return true;
        });
        player.setVideoURI(uri);
    }

    @Override protected void onResume() {
        super.onResume(); active = true;
        if (player != null && prepared && resumePlayback) player.start();
    }
    @Override protected void onPause() {
        active = false;
        if (player != null && prepared) { position = player.getCurrentPosition(); resumePlayback = player.isPlaying(); player.pause(); }
        super.onPause();
    }
    @Override protected void onSaveInstanceState(@NonNull Bundle state) {
        state.putInt("position", player != null && prepared ? player.getCurrentPosition() : position);
        state.putBoolean("playing", player != null && prepared ? player.isPlaying() || resumePlayback : resumePlayback);
        super.onSaveInstanceState(state);
    }
    @Override protected void onDestroy() { if (player != null) player.stopPlayback(); super.onDestroy(); }
}
