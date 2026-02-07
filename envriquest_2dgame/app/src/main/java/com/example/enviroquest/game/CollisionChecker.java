package com.example.enviroquest.game;

public class CollisionChecker {
    private TileManager tm;
    private int tileSize;

    public CollisionChecker(TileManager tm, int tileSize) {
        this.tm = tm;
        this.tileSize = tileSize;
    }

    public boolean checkTile(float x, float y) {
        float p = 50f; // Collision padding
        return isSolid(x + p, y + p) ||
                isSolid(x + tileSize - p, y + p) ||
                isSolid(x + p, y + tileSize - p) ||
                isSolid(x + tileSize - p, y + tileSize - p);
    }

    private boolean isSolid(float x, float y) {
        int col = (int)(x / tileSize);
        int row = (int)(y / tileSize);
        if (row < 0 || row >= tm.getRows() || col < 0 || col >= tm.getCols()) return true;
        int id = tm.getTileAt(row, col);
        return (id == 0 || (id >= 3 && id <= 10)); // Flower (ID 2) is now walkable
    }
}