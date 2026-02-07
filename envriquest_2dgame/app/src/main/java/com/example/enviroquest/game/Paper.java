package com.example.enviroquest.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Paper {
    public int worldX, worldY;
    public boolean isCollected = false;
    private int size;

    public Paper(int worldX, int worldY, int size) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.size = size;
    }

    public void draw(Canvas canvas, int playerWorldX, int playerWorldY, int screenWidth, int screenHeight, Paint paint) {
        if (isCollected) return;

        // Calculate position relative to the player (Camera logic)
        int screenX = worldX - playerWorldX + (screenWidth / 2) - (size / 2);
        int screenY = worldY - playerWorldY + (screenHeight / 2) - (size / 2);

        // Only draw if it's within the screen bounds to save performance
        if (screenX + size > 0 && screenX < screenWidth && screenY + size > 0 && screenY < screenHeight) {
            paint.setColor(Color.WHITE); // Color for the "paper"
            canvas.drawRect(screenX + 20, screenY + 20, screenX + size - 20, screenY + size - 20, paint);

            // Optional: Add a small border so it looks like a box
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.GRAY);
            paint.setStrokeWidth(5);
            canvas.drawRect(screenX + 20, screenY + 20, screenX + size - 20, screenY + size - 20, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    public Rect getBounds() {
        return new Rect(worldX, worldY, worldX + size, worldY + size);
    }
}