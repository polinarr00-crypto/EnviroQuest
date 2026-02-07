package com.example.enviroquest;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotificationActivity extends AppCompatActivity {

    private NotificationAdapter adapter;
    private List<NotificationModel> list;
    private FirebaseFirestore db;
    private Set<String> submittedQuestTitles = new HashSet<>();
    private Set<String> clearedNotificationIds = new HashSet<>();
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        ImageButton btnBack = findViewById(R.id.btn_back_notif);
        btnBack.setOnClickListener(v -> finish());

        TextView tvDeleteAll = findViewById(R.id.tv_delete_all);
        tvDeleteAll.setOnClickListener(v -> showClearConfirmationDialog());

        RecyclerView rvNotif = findViewById(R.id.rv_notifications);
        rvNotif.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new NotificationAdapter(list);
        rvNotif.setAdapter(adapter);

        if (currentUserId != null) {
            fetchClearedNotifications();
        }
    }

    private void fetchClearedNotifications() {
        db.collection("Users").document(currentUserId)
                .collection("ClearedNotifications")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        clearedNotificationIds.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            clearedNotificationIds.add(doc.getId());
                        }
                        // Once we know what's cleared, fetch everything else
                        fetchUserSubmissionsAndNotifications();
                    }
                });
    }

    private void showClearConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Notifications")
                .setMessage("Are you sure you want to permanently clear all read notifications?")
                .setPositiveButton("Clear All", (dialog, which) -> saveClearedToFirestore())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveClearedToFirestore() {
        List<NotificationModel> itemsToRemove = new ArrayList<>();
        for (NotificationModel item : list) {
            boolean isHighlighted = "NEW".equals(item.getType()) && !item.isSubmitted();
            if (!isHighlighted) {
                itemsToRemove.add(item);
            }
        }

        for (NotificationModel item : itemsToRemove) {
            Map<String, Object> data = new HashMap<>();
            data.put("clearedAt", FieldValue.serverTimestamp());
            
            db.collection("Users").document(currentUserId)
                    .collection("ClearedNotifications").document(item.getId())
                    .set(data);
        }
        
        // The snapshot listener for ClearedNotifications will handle removing them from the UI
    }

    private void fetchUserSubmissionsAndNotifications() {
        db.collection("Submissions")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        submittedQuestTitles.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            String title = doc.getString("questTitle");
                            if (title != null) submittedQuestTitles.add(title.toLowerCase());
                        }
                        fetchQuests();
                        fetchApprovedSubmissions();
                    }
                });
    }

    private void fetchQuests() {
        db.collection("Quests")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(15)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            if (clearedNotificationIds.contains(doc.getId())) continue;
                            
                            NotificationModel m = doc.toObject(NotificationModel.class);
                            m.setId(doc.getId());
                            m.setType("NEW");
                            checkIfSubmitted(m);
                            addUniqueToList(m);
                        }
                        removeClearedFromLocalList();
                    }
                });
    }

    private void fetchApprovedSubmissions() {
        db.collection("Submissions")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", "Approved")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            if (clearedNotificationIds.contains(doc.getId())) continue;

                            String title = doc.getString("questTitle");
                            Long pts = doc.getLong("points");
                            int points = (pts != null) ? pts.intValue() : 0;

                            NotificationModel m = new NotificationModel(doc.getId(), title, points, "APPROVED");
                            m.setSubmitted(true);
                            addUniqueToList(m);
                        }
                        removeClearedFromLocalList();
                    }
                });
    }

    private void removeClearedFromLocalList() {
        Iterator<NotificationModel> it = list.iterator();
        while (it.hasNext()) {
            if (clearedNotificationIds.contains(it.next().getId())) {
                it.remove();
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void checkIfSubmitted(NotificationModel m) {
        if (m.getTitle() != null && submittedQuestTitles.contains(m.getTitle().toLowerCase())) {
            m.setSubmitted(true);
        } else {
            m.setSubmitted(false);
        }
    }

    private void addUniqueToList(NotificationModel newNotif) {
        int existingIndex = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(newNotif.getId())) {
                existingIndex = i;
                break;
            }
        }
        
        if (existingIndex != -1) {
            list.set(existingIndex, newNotif);
        } else {
            list.add(0, newNotif);
        }
        adapter.notifyDataSetChanged();
    }
}