package com.r.example.movieapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final ArrayList<VideoInfo> items = new ArrayList<>();
    private DatabaseReference videos;
    private ValueEventListener listener;
    private VideoAdapter adapter;
    private TextView status;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!FirebaseSetup.requireUser(this, LoginActivity.class)) return;
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.status);
        RecyclerView list = findViewById(R.id.recyclerView);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoAdapter(items); list.setAdapter(adapter);
        videos = FirebaseDatabase.getInstance().getReference("videos");
        findViewById(R.id.btnLogout).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            FirebaseSetup.requireUser(this, LoginActivity.class);
        });
    }

    @Override protected void onStart() {
        super.onStart();
        if (videos == null || !FirebaseSetup.requireUser(this, LoginActivity.class)) return;
        status.setText("Memuat video...");
        listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        VideoInfo item = child.getValue(VideoInfo.class);
                        if (item != null && item.getTitle() != null && item.getVideoUrl() != null) items.add(item);
                    } catch (DatabaseException ignored) { /* Skip records that do not follow the video schema. */ }
                }
                adapter.notifyDataSetChanged();
                status.setText(items.isEmpty() ? "Belum ada video. Pengelola dapat menambahkan dari aplikasi admin." : items.size() + " video tersedia.");
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                status.setText("Video gagal dimuat. Periksa koneksi dan rules Firebase.");
            }
        };
        videos.addValueEventListener(listener);
    }

    @Override protected void onStop() {
        if (videos != null && listener != null) videos.removeEventListener(listener);
        listener = null; super.onStop();
    }
}
