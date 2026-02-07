package com.example.enviroquest.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import java.util.ArrayList;

public class TrashManager {
    private ArrayList<Paper> trashList;
    private int scaledSize;

    public TrashManager(int scaledSize) {
        this.scaledSize = scaledSize;
        this.trashList = new ArrayList<>();
        spawnTrash();
    }

    private void spawnTrash() {
        // Dire nimo i-set ang mga positions sa basura sa world
        trashList.add(new Paper(26 * scaledSize, 26 * scaledSize, scaledSize));
        trashList.add(new Paper(28 * scaledSize, 24 * scaledSize, scaledSize));
        trashList.add(new Paper(32 * scaledSize, 28 * scaledSize, scaledSize));
        // Pwede ra ka magdugang pa dire
    }

    // Sa TrashManager.java, usba ang update method:
    public void update(Rect playerRect, Inventory inventory) {
        for (Paper p : trashList) {
            if (!p.isCollected && Rect.intersects(playerRect, p.getBounds())) {
                p.isCollected = true;

                // I-add sa inventory!
                inventory.addItem("Papel");

            }
        }
    }

    public void draw(Canvas canvas, int playerX, int playerY, int screenW, int screenH, Paint paint) {
        for (Paper p : trashList) {
            p.draw(canvas, playerX, playerY, screenW, screenH, paint);
        }
    }
}