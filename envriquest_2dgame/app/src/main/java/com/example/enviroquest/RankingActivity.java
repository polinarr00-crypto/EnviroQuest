package com.example.enviroquest;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        ImageButton btnBack = findViewById(R.id.btn_back_ranking);
        btnBack.setOnClickListener(v -> finish());

        // Setup RecyclerView
        RecyclerView rvRanking = findViewById(R.id.rv_ranking);
        rvRanking.setLayoutManager(new LinearLayoutManager(this));

        // Maghimo og Dummy Data
        List<RankingModel> players = new ArrayList<>();
        players.add(new RankingModel(1, "EcoMaster_99", 15000));
        players.add(new RankingModel(2, "GreenWarrior", 12400));
        players.add(new RankingModel(3, "NatureHero", 10200));
        players.add(new RankingModel(4, "Bohol_Explorer", 8500));
        players.add(new RankingModel(5, "RecycleKing", 7200));
        players.add(new RankingModel(6, "TreePlanter", 6100));
        players.add(new RankingModel(7, "EarthLover", 5400));

        // I-set ang Adapter
        RankingAdapter adapter = new RankingAdapter(players);
        rvRanking.setAdapter(adapter);
    }
}