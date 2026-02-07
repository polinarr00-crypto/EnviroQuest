package com.example.enviroquest;

public class RankingModel {
    private String name;
    private int rank;
    private int points;

    public RankingModel(int rank, String name, int points) {
        this.rank = rank;
        this.name = name;
        this.points = points;
    }

    public int getRank() { return rank; }
    public String getName() { return name; }
    public int getPoints() { return points; }
}