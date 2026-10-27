package com.r.example.movieapp;

import android.app.Activity;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.FirebaseApp;

public final class FirebaseSetup {
    private FirebaseSetup() {}

    public static boolean requireUser(Activity activity, Class<? extends Activity> loginClass) {
        if (!configureOrShow(activity)) return false;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) return true;
        activity.startActivity(new Intent(activity, loginClass)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        activity.finish();
        return false;
    }

    public static boolean configureOrShow(Activity activity) {
        int appId = activity.getResources().getIdentifier("google_app_id", "string", activity.getPackageName());
        if (appId != 0) {
            try {
                if (FirebaseApp.initializeApp(activity) != null) return true;
            } catch (IllegalArgumentException | IllegalStateException ignored) {
                // Invalid local configuration is handled by the same setup screen.
            }
        }
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * activity.getResources().getDisplayMetrics().density);
        content.setPadding(padding, padding, padding, padding);
        TextView title = new TextView(activity);
        title.setText("Konfigurasi Firebase belum tersedia");
        title.setTextSize(22);
        content.addView(title);
        TextView description = new TextView(activity);
        description.setText("Aplikasi belum terhubung ke Firebase.\n\nBuat project Firebase milik Anda, daftarkan applicationId modul ini, lalu simpan file konfigurasi pribadi di client/google-services.json dan build ulang.\n\nIkuti README untuk mengaktifkan layanan dan memasang aturan akses. File konfigurasi dan data cloud lama tidak disertakan.");
        description.setTextSize(16);
        description.setTextIsSelectable(true);
        content.addView(description);
        activity.setContentView(content);
        return false;
    }
}
