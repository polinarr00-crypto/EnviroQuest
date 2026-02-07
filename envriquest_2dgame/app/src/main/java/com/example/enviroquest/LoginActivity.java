package com.example.enviroquest;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.login_progress);
        TextView tvRegister = findViewById(R.id.tv_go_to_register);

        btnLogin.setOnClickListener(v -> performLogin());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserStatus(mAuth.getCurrentUser().getUid());
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserStatus(String uid) {
        db.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);

            if (documentSnapshot.exists()) {
                String status = documentSnapshot.getString("status");
                if ("approved".equals(status)) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    mAuth.signOut();
                    Toast.makeText(this, "Account is pending admin approval.", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
        });
    }
}