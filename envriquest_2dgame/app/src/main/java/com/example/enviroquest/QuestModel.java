package com.example.enviroquest;

import com.google.firebase.Timestamp;

public class QuestModel {
    private String questId;
    private String title;
    private String description;
    private int points;
    private String status;
    private Timestamp createdAt;

    // No-argument constructor required for Firebase
    public QuestModel() {}

    public QuestModel(String title, String description) {
        this.title = title;
        this.description = description;
    }
    public String getQuestId() { return questId; }
    public void setQuestId(String questId) { this.questId = questId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}