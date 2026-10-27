package com.r.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!FirebaseSetup.configureOrShow(this)) return;
        setContentView(R.layout.activity_register);
        EditText email = findViewById(R.id.et_email);
        EditText password = findViewById(R.id.et_password);
        Button submit = findViewById(R.id.bt_signup);
        submit.setOnClickListener(view -> {
            String address = email.getText().toString().trim();
            String secret = password.getText().toString();
            if (!Patterns.EMAIL_ADDRESS.matcher(address).matches()) { email.setError("Masukkan email yang valid."); return; }
            if (secret.length() < 6) { password.setError("Password minimal 6 karakter."); return; }
            submit.setEnabled(false);
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(address, secret).addOnCompleteListener(task -> {
                submit.setEnabled(true);
                if (!task.isSuccessful()) {
                    Toast.makeText(this, "Pendaftaran gagal. Periksa akun, koneksi, dan konfigurasi Firebase.", Toast.LENGTH_LONG).show();
                    return;
                }
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            });
        });
    }
}
