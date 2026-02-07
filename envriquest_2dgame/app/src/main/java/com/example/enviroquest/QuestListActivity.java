package com.example.enviroquest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class QuestListActivity extends AppCompatActivity {

    private static final String TAG = "QuestListActivity";
    private FirebaseFirestore db;
    private QuestAdapter adapter;
    private List<QuestModel> questList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_list);

        db = FirebaseFirestore.getInstance();

        // --- BACK BUTTON LOGIC ---
        ImageButton btnBack = findViewById(R.id.btn_back_symbol);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        RecyclerView rvQuest = findViewById(R.id.rv_quest_list);
        rvQuest.setLayoutManager(new LinearLayoutManager(this));

        questList = new ArrayList<>();
        
        // Setup Adapter with click listener
        adapter = new QuestAdapter(questList, quest -> {
            Intent intent = new Intent(QuestListActivity.this, QuestActivity.class);
            intent.putExtra("QUEST_ID", quest.getQuestId());
            intent.putExtra("QUEST_TITLE", quest.getTitle());
            intent.putExtra("QUEST_DESC", quest.getDescription());
            intent.putExtra("QUEST_POINTS", quest.getPoints());
            startActivity(intent);
        });

        rvQuest.setAdapter(adapter);

        // Fetch data from Firebase
        fetchAvailableQuests();
    }

    private void fetchAvailableQuests() {
        String userId = FirebaseAuth.getInstance().getUid();

        db.collection("Submissions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(submissionSnapshots -> {

                    List<String> excludedQuestIds = new ArrayList<>();
                    for (DocumentSnapshot doc : submissionSnapshots) {
                        String status = doc.getString("status");
                        if ("Pending".equals(status) || "Approved".equals(status)) {
                            excludedQuestIds.add(doc.getString("questId"));
                        }
                    }

                    db.collection("Quests").get().addOnSuccessListener(questSnapshots -> {
                        questList.clear();
                        for (DocumentSnapshot qDoc : questSnapshots) {
                            if (!excludedQuestIds.contains(qDoc.getId())) {
                                QuestModel quest = qDoc.toObject(QuestModel.class);
                                quest.setQuestId(qDoc.getId());
                                questList.add(quest);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    });
                });
    }
}