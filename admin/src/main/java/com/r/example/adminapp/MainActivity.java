package com.r.example.adminapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements VideoAdapter.Actions {
    private DatabaseReference videos;
    private ValueEventListener listener;
    private StorageReference storage;
    private FirebaseAuth auth;
    private VideoAdapter adapter;
    private final ArrayList<VideoInfo> items = new ArrayList<>();
    private TextView status;
    private Button choose, upload;
    private Uri selected;
    private boolean started, authorized, busy;

    private final ActivityResultLauncher<String[]> picker = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                String type = getContentResolver().getType(uri);
                if (type != null && !type.startsWith("video/")) { message("Pilih file video."); return; }
                selected = uri;
                try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
                catch (SecurityException ignored) { /* Some document providers grant access only for this session. */ }
                message("Video dipilih. Tekan Upload Video untuk mengisi detail.");
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!FirebaseSetup.requireUser(this, LoginActivity.class)) return;
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        status = findViewById(R.id.status);
        choose = findViewById(R.id.btnChooseVideo); upload = findViewById(R.id.btnUploadVideo);
        if (state != null && state.getString("selected") != null) selected = Uri.parse(state.getString("selected"));
        RecyclerView list = findViewById(R.id.recyclerView);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoAdapter(items, this); list.setAdapter(adapter);
        findViewById(R.id.btnLogout).setOnClickListener(view -> {
            auth.signOut();
            FirebaseSetup.requireUser(this, LoginActivity.class);
        });
        controls();
    }

    @Override protected void onStart() {
        super.onStart();
        started = true;
        if (auth == null || !FirebaseSetup.requireUser(this, LoginActivity.class)) return;
        authorized = false; controls();
        message("Memeriksa izin pengelola...");
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        user.getIdToken(false).addOnCompleteListener(task -> {
            if (!started || isFinishing()) return;
            if (!task.isSuccessful() || !Boolean.TRUE.equals(task.getResult().getClaims().get("admin"))) {
                message("Akun ini belum memiliki custom claim admin. Hubungi pengelola project Firebase, lalu login ulang.");
                return;
            }
            authorized = true;
            videos = FirebaseDatabase.getInstance().getReference("videos");
            storage = FirebaseStorage.getInstance().getReference("videos");
            controls(); listen();
        });
    }

    private void listen() {
        if (listener != null) return;
        listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        VideoInfo item = child.getValue(VideoInfo.class);
                        if (item != null && item.getTitle() != null) { item.setKey(child.getKey()); items.add(item); }
                    } catch (DatabaseException ignored) { /* Ignore records with an incompatible schema. */ }
                }
                adapter.notifyDataSetChanged();
                if (!busy) message(items.isEmpty() ? "Belum ada video. Pilih file untuk menambahkan." : items.size() + " video tersedia.");
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { message("Daftar gagal dimuat. Periksa koneksi dan rules Firebase."); }
        };
        videos.addValueEventListener(listener);
    }

    public void chooseVideo(View view) { if (authorized && !busy) picker.launch(new String[]{"video/*"}); }

    public void uploadVideo(View view) {
        if (!authorized || busy) return;
        if (selected == null) { message("Pilih video terlebih dahulu."); return; }
        View fields = getLayoutInflater().inflate(R.layout.dialog_video_details, null);
        EditText title = fields.findViewById(R.id.edtTitle), description = fields.findViewById(R.id.edtDescription);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Detail video").setView(fields)
                .setNegativeButton("Batal", null).setPositiveButton("Upload", null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button -> {
            String name = title.getText().toString().trim(), summary = description.getText().toString().trim();
            if (name.isEmpty() || name.length() > 200) { title.setError("Judul wajib diisi, maksimal 200 karakter."); return; }
            if (summary.length() > 5000) { description.setError("Deskripsi maksimal 5000 karakter."); return; }
            dialog.dismiss(); sendVideo(name, summary);
        }));
        dialog.show();
    }

    private void sendVideo(String title, String description) {
        busy = true; controls(); message("Mengunggah video...");
        String mime = getContentResolver().getType(selected);
        String extension = mime == null ? null : MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
        StorageReference file = storage.child(UUID.randomUUID() + "." + (extension == null ? "mp4" : extension));
        StorageMetadata.Builder metadata = new StorageMetadata.Builder();
        if (mime != null) metadata.setContentType(mime);
        file.putFile(selected, metadata.build()).addOnProgressListener(progress -> {
            if (progress.getTotalByteCount() > 0) message("Mengunggah " + (100 * progress.getBytesTransferred() / progress.getTotalByteCount()) + "%");
        }).addOnSuccessListener(result -> file.getDownloadUrl().addOnSuccessListener(url -> {
            String key = videos.push().getKey();
            if (key == null) { cleanup(file, "Identitas metadata tidak tersedia."); return; }
            VideoInfo value = new VideoInfo(url.toString(), title, description, file.getPath());
            videos.child(key).setValue(value).addOnCompleteListener(saved -> {
                if (!saved.isSuccessful()) { cleanup(file, "Metadata gagal disimpan."); return; }
                selected = null; busy = false; controls(); message("Video dan metadata tersimpan.");
            });
        }).addOnFailureListener(error -> cleanup(file, "URL download gagal diperoleh.")))
                .addOnFailureListener(error -> { busy = false; controls(); message("Upload gagal. Periksa koneksi, ukuran file, dan rules Storage."); });
    }

    private void cleanup(StorageReference file, String reason) {
        file.delete().addOnCompleteListener(task -> {
            busy = false; controls();
            message(reason + (task.isSuccessful() ? " File upload dibersihkan." : " File mungkin tertinggal di Storage; periksa melalui Firebase Console."));
        });
    }

    @Override public void edit(VideoInfo item) {
        if (!authorized || busy || item.getKey() == null) return;
        View fields = getLayoutInflater().inflate(R.layout.dialog_video_details, null);
        EditText title = fields.findViewById(R.id.edtTitle), description = fields.findViewById(R.id.edtDescription);
        title.setText(item.getTitle()); description.setText(item.getDescription());
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Ubah detail video").setView(fields)
                .setNegativeButton("Batal", null).setPositiveButton("Simpan", null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button -> {
            String name = title.getText().toString().trim(), summary = description.getText().toString().trim();
            if (name.isEmpty() || name.length() > 200) { title.setError("Judul wajib diisi, maksimal 200 karakter."); return; }
            if (summary.length() > 5000) { description.setError("Deskripsi maksimal 5000 karakter."); return; }
            dialog.dismiss(); busy = true; controls();
            Map<String, Object> update = new HashMap<>(); update.put("title", name); update.put("description", summary);
            videos.child(item.getKey()).updateChildren(update).addOnCompleteListener(task -> {
                busy = false; controls(); message(task.isSuccessful() ? "Detail tersimpan." : "Detail gagal disimpan.");
            });
        }));
        dialog.show();
    }

    @Override public void delete(VideoInfo item) {
        if (!authorized || busy || item.getKey() == null) return;
        new AlertDialog.Builder(this).setTitle("Hapus video?").setMessage(item.getTitle())
                .setNegativeButton("Batal", null).setPositiveButton("Hapus", (dialog, which) -> {
                    StorageReference file;
                    try {
                        if (item.getStoragePath() != null) file = FirebaseStorage.getInstance().getReference(item.getStoragePath());
                        else if (item.getVideoUrl() != null) file = FirebaseStorage.getInstance().getReferenceFromUrl(item.getVideoUrl());
                        else { message("Lokasi file tidak tersedia."); return; }
                        if (!file.getPath().startsWith("/videos/")) { message("Lokasi file berada di luar folder videos."); return; }
                    } catch (IllegalArgumentException error) { message("Lokasi file tidak valid."); return; }
                    busy = true; controls();
                    file.delete().addOnCompleteListener(task -> {
                        boolean missing = task.getException() instanceof StorageException
                                && ((StorageException) task.getException()).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND;
                        if (!task.isSuccessful() && !missing) { busy = false; controls(); message("File gagal dihapus."); return; }
                        videos.child(item.getKey()).removeValue().addOnCompleteListener(removed -> {
                            busy = false; controls();
                            message(removed.isSuccessful() ? "Video dihapus." : "File sudah terhapus tetapi metadata belum. Ulangi penghapusan.");
                        });
                    });
                }).show();
    }

    private void controls() {
        if (choose != null) choose.setEnabled(authorized && !busy);
        if (upload != null) upload.setEnabled(authorized && !busy);
    }
    private void message(String text) { if (status != null && !isDestroyed()) status.setText(text); }
    @Override protected void onSaveInstanceState(@NonNull Bundle state) {
        if (selected != null) state.putString("selected", selected.toString());
        super.onSaveInstanceState(state);
    }
    @Override protected void onStop() {
        started = false; authorized = false;
        if (videos != null && listener != null) videos.removeEventListener(listener);
        listener = null; super.onStop();
    }
}
