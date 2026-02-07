package com.example.enviroquest;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etMuni, etBrgy, etEmail, etPass, etBirthday;
    private Button btnRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.et_reg_name);
        etMuni= findViewById(R.id.et_reg_muni);
        etBrgy = findViewById(R.id.et_reg_brgy);
        etBirthday = findViewById(R.id.et_reg_birthday);
        etEmail = findViewById(R.id.et_reg_email);
        etPass = findViewById(R.id.et_reg_password);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.reg_progress);

        // Date Picker para sa Birthday
        etBirthday.setOnClickListener(v -> showDatePicker());

        btnRegister.setOnClickListener(v -> registerHero());
        findViewById(R.id.tv_go_to_login).setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR) - 18; // Default to 18 years ago
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            // I-save nato sa format nga dali ma-calculate ang age: YYYY-MM-DD
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
            etBirthday.setText(date);
        }, year, month, day);
        dpd.show();
    }

    private void registerHero() {
        String name = etName.getText().toString().trim();
        String muni = etMuni.getText().toString().trim();
        String brgy = etBrgy.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(muni) || TextUtils.isEmpty(brgy) || TextUtils.isEmpty(birthday) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Palihug sulati tanan fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveToFirestore(mAuth.getCurrentUser().getUid(), name, muni, brgy, email, pass, birthday);
            } else {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToFirestore(String uid, String name, String muni, String brgy, String email, String pass, String birthday) {
        String dateReg = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> user = new HashMap<>();
        user.put("fullname", name);
        user.put("municipal", muni);
        user.put("brgy", brgy);
        user.put("email", email);
        user.put("password", pass);
        user.put("birthday", birthday); // YYYY-MM-DD
        user.put("profilePic", "");     // Default empty
        user.put("points", 100);        // Bonus starting points!
        user.put("status", "pending");
        user.put("dateRegistered", dateReg);

        db.collection("Users").document(uid).set(user).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Hero Created! ✨", Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
        });
    }
}