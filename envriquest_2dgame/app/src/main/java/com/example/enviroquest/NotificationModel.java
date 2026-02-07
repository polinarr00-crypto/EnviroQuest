package com.example.enviroquest;

public class NotificationModel {
    private String id;
    private String title;
    private int points;
    private String type;
    private boolean submitted;

    public NotificationModel() {}

    public NotificationModel(String id, String title, int points, String type) {
        this.id = id;
        this.title = title;
        this.points = points;
        this.type = type;
        this.submitted = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public boolean isSubmitted() { return submitted; }
    public void setSubmitted(boolean submitted) { this.submitted = submitted; }
}