package com.example.enviroquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.enviroquest.game.GameActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView tvUsername, tvNotifBadge;
    private ImageView imgMainProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int notifCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvUsername = findViewById(R.id.tv_username);
        tvNotifBadge = findViewById(R.id.tv_notif_badge);
        imgMainProfile = findViewById(R.id.img_main_profile);

        fetchUserData();
        listenForNotifications();

        Button btnGame = findViewById(R.id.btn_go_game);
        Button btnQuest = findViewById(R.id.btn_go_quests);
        View btnProfile = findViewById(R.id.btn_profile);
        ImageButton btnNotif = findViewById(R.id.btn_notifications);
        Button btnRanking = findViewById(R.id.btn_main_ranking);

        final Animation buttonClick = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);

        btnProfile.setOnClickListener(v -> {
            v.startAnimation(buttonClick);
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnNotif.setOnClickListener(v -> {
            v.startAnimation(buttonClick);
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        btnGame.setOnClickListener(v -> {
            v.startAnimation(buttonClick);
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        btnQuest.setOnClickListener(v -> {
            v.startAnimation(buttonClick);
            Intent intent = new Intent(MainActivity.this, QuestListActivity.class);
            startActivity(intent);
        });

        btnRanking.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RankingActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserData();
    }

    private void listenForNotifications() {
        String userId = mAuth.getUid();
        if (userId == null) return;

        Set<String> submittedTitles = new HashSet<>();

        db.collection("Submissions")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        submittedTitles.clear();
                        int approvedCount = 0;
                        for (QueryDocumentSnapshot doc : value) {
                            String title = doc.getString("questTitle");
                            if (title != null) submittedTitles.add(title.toLowerCase());
                            
                            if ("Approved".equals(doc.getString("status"))) {
                                approvedCount++;
                            }
                        }
                        
                        final int finalApprovedCount = approvedCount;
                        
                        // check for NEW quests that haven't been submitted
                        db.collection("Quests")
                                .orderBy("createdAt", Query.Direction.DESCENDING)
                                .limit(10)
                                .addSnapshotListener((qValue, qError) -> {
                                    if (qValue != null) {
                                        int newQuestCount = 0;
                                        for (QueryDocumentSnapshot qDoc : qValue) {
                                            String title = qDoc.getString("title");
                                            if (title != null && !submittedTitles.contains(title.toLowerCase())) {
                                                newQuestCount++;
                                            }
                                        }
                                        updateBadge(finalApprovedCount + newQuestCount);
                                    }
                                });
                    }
                });
    }

    private void updateBadge(int count) {
        if (count > 0) {
            tvNotifBadge.setText(String.valueOf(count));
            tvNotifBadge.setVisibility(View.VISIBLE);
        } else {
            tvNotifBadge.setVisibility(View.GONE);
        }
    }

    private void fetchUserData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("fullname");
                            tvUsername.setText(name != null ? name : "Hero");

                            String url = documentSnapshot.getString("profilePic");
                            if (url != null && !url.isEmpty()) {
                                imgMainProfile.setPadding(0, 0, 0, 0);
                                imgMainProfile.clearColorFilter();

                                Glide.with(this)
                                        .load(url)
                                        .circleCrop()
                                        .placeholder(android.R.drawable.ic_menu_gallery)
                                        .into(imgMainProfile);
                            }
                        }
                    });
        }
    }
}