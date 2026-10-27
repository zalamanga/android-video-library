package com.r.example.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private Button signin, signup;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!FirebaseSetup.configureOrShow(this)) return;
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) { openHome(); return; }
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        signin = findViewById(R.id.bt_signin);
        signup = findViewById(R.id.bt_signup);
        signup.setOnClickListener(view -> startActivity(new Intent(this, RegisterActivity.class)));
        signin.setOnClickListener(view -> signIn());
    }

    private void signIn() {
        String address = email.getText().toString().trim();
        String secret = password.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(address).matches()) { email.setError("Masukkan email yang valid."); return; }
        if (secret.isEmpty()) { password.setError("Masukkan password."); return; }
        signin.setEnabled(false); signup.setEnabled(false);
        auth.signInWithEmailAndPassword(address, secret).addOnCompleteListener(task -> {
            signin.setEnabled(true); signup.setEnabled(true);
            if (task.isSuccessful()) openHome();
            else Toast.makeText(this, "Login gagal. Periksa akun, koneksi, dan konfigurasi Firebase.", Toast.LENGTH_LONG).show();
        });
    }

    private void openHome() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
