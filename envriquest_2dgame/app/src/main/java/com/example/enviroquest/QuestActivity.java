package com.example.enviroquest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QuestActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private final String IMGBB_API_KEY = "50e84f16749c86ccdd982a6f310f9fe3";

    private ImageView imgPreview;
    private MaterialCardView cardImagePreview;
    private TextView tvQuestTitle, tvQuestDesc, tvQuestPoints;
    private Button btnSubmit;
    private Uri selectedImageUri;
    private String currentQuestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest);

        // Bind Views
        imgPreview = findViewById(R.id.img_preview);
        cardImagePreview = findViewById(R.id.card_image_preview);
        tvQuestTitle = findViewById(R.id.tv_quest_title);
        tvQuestDesc = findViewById(R.id.tv_instruction);
        tvQuestPoints = findViewById(R.id.tv_quest_points_detail);
        btnSubmit = findViewById(R.id.btn_submit_quest);
        Button btnGallery = findViewById(R.id.btn_gallery);
        ImageButton btnBack = findViewById(R.id.btn_back_quest_details);

        btnBack.setOnClickListener(v -> finish());

        // Receive Intent Data
        Intent intentData = getIntent();
        if (intentData != null) {
            currentQuestId = intentData.getStringExtra("QUEST_ID");
            tvQuestTitle.setText(intentData.getStringExtra("QUEST_TITLE"));
            tvQuestDesc.setText(intentData.getStringExtra("QUEST_DESC"));
            tvQuestPoints.setText("+" + intentData.getIntExtra("QUEST_POINTS", 0) + " Points");
        }

        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnSubmit.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadToImgBB(currentQuestId);
            }
        });
    }

    private void uploadToImgBB(String questId) {
        btnSubmit.setEnabled(false);
        btnSubmit.setText("UPLOADING...");

        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", IMGBB_API_KEY)
                    .addFormDataPart("image", base64Image)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("RETRY SUBMIT");
                        Toast.makeText(QuestActivity.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response.body().string());
                            String imageUrl = jsonResponse.getJSONObject("data").getString("url");
                            saveSubmissionToFirestore(questId, imageUrl);
                        } catch (Exception e) {
                            Log.e("ImgBB", "JSON Error", e);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveSubmissionToFirestore(String questId, String imageUrl) {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : "Anonymous";
        String userEmail = (user != null) ? user.getEmail() : "No Email";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> submission = new HashMap<>();

        submission.put("userId", userId);
        submission.put("userEmail", userEmail);
        submission.put("questId", questId);
        submission.put("questTitle", tvQuestTitle.getText().toString());
        submission.put("proofImageUrl", imageUrl);
        submission.put("status", "Pending");
        submission.put("submittedAt", com.google.firebase.Timestamp.now());

        db.collection("Submissions").add(submission)
                .addOnSuccessListener(documentReference -> {
                    runOnUiThread(() -> {
                        Toast.makeText(QuestActivity.this, "Proof Submitted! 🏆", Toast.LENGTH_LONG).show();
                        finish();
                    });
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("FINISH QUEST 🏆");
                        Toast.makeText(QuestActivity.this, "Error saving to Firestore", Toast.LENGTH_SHORT).show();
                    });
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && requestCode == PICK_IMAGE) {
            selectedImageUri = data.getData();
            cardImagePreview.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);
            imgPreview.setImageURI(selectedImageUri);
            imgPreview.setImageTintList(null);
            imgPreview.clearColorFilter();
            imgPreview.setTag("HAS_IMAGE");
        }
    }
}