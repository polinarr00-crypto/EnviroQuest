package com.example.enviroquest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView imgProfile;
    private TextView tvProfileName;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // API Key sa ImgBB
    private final String IMGBB_API_KEY = "50e84f16749c86ccdd982a6f310f9fe3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        imgProfile = findViewById(R.id.img_profile_display);
        tvProfileName = findViewById(R.id.profile_name);
        ImageButton btnBack = findViewById(R.id.btn_back_profile_symbol);
        ImageButton btnSettings = findViewById(R.id.btn_settings);
        View btnChangePhoto = findViewById(R.id.btn_change_photo);
        MaterialButton btnLogout = findViewById(R.id.btn_logout);

        loadUserProfile();

        btnBack.setOnClickListener(v -> finish());

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        btnChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("Users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            tvProfileName.setText(doc.getString("fullname"));
                            String url = doc.getString("profilePic");
                            if (url != null && !url.isEmpty()) {
                                // I-update ang UI para sa tinuod nga image
                                imgProfile.setPadding(0, 0, 0, 0);
                                imgProfile.clearColorFilter(); // Tangtangon ang blue tint
                                Glide.with(this).load(url).circleCrop().into(imgProfile);
                            }
                        }
                    });
        }
    }

    private void uploadToImgBB(Uri imageUri) {
        Toast.makeText(this, "Uploading to Cloud... Please wait", Toast.LENGTH_SHORT).show();

        try {
            InputStream is = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("key", IMGBB_API_KEY)
                    .add("image", encodedImage)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Upload Failed!", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            String url = json.getJSONObject("data").getString("url");
                            updateFirestore(url);
                        } catch (Exception e) {
                            Log.e("IMGBB_ERROR", e.getMessage());
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateFirestore(String url) {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("Users").document(uid)
                .update("profilePic", url)
                .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "Hero Profile Updated!", Toast.LENGTH_SHORT).show();

                    imgProfile.setPadding(0, 0, 0, 0);
                    imgProfile.clearColorFilter();
                    Glide.with(ProfileActivity.this)
                            .load(url)
                            .circleCrop()
                            .into(imgProfile);
                }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            uploadToImgBB(data.getData());
        }
    }
}
