package com.example.enviroquest.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.example.enviroquest.R;

public class Player {
    public float worldX, worldY;
    public int size;
    private Bitmap slimeIdle, slimeSquish;
    private boolean isSquished = false;
    private int animTick = 0;

    public Player(float startX, float startY, int size, Context context) {
        this.worldX = startX;
        this.worldY = startY;
        this.size = size;

        Bitmap raw1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.slime_1);
        Bitmap raw2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.slime_2);
        this.slimeIdle = Bitmap.createScaledBitmap(raw1, size, size, false);
        this.slimeSquish = Bitmap.createScaledBitmap(raw2, size, size, false);
    }

    public void update(boolean up, boolean down, boolean left, boolean right, float deltaTime, CollisionChecker checker) {
        float baseSpeed = 600f;
        float dx = 0;
        float dy = 0;

        // 1. Kuhaon ang "Directional Intent"
        if (up) dy -= 1;
        if (down) dy += 1;
        if (left) dx -= 1;
        if (right) dx += 1;

        // 2. NORMALIZE: Kini ang mo-fix sa "pas-pas sa diagonal"
        // Kon mag-diagonal, ang dx ug dy i-adjust para ang total distance 1.0 gihapon
        if (dx != 0 && dy != 0) {
            dx *= 0.7071f; // 1 / sqrt(2)
            dy *= 0.7071f;
        }

        // 3. I-calculate ang final speed base sa deltaTime
        float speedX = dx * baseSpeed * deltaTime;
        float speedY = dy * baseSpeed * deltaTime;

        // 4. Collision Check (X ug Y separately para sa smooth sliding)
        if (dx != 0 && !checker.checkTile(worldX + speedX, worldY)) {
            worldX += speedX;
        }
        if (dy != 0 && !checker.checkTile(worldX, worldY + speedY)) {
            worldY += speedY;
        }

        // 5. Animation logic
        animTick++;
        if (animTick % 10 == 0) isSquished = !isSquished;
    }

    public void draw(Canvas canvas, float screenX, float screenY, Paint paint) {
        Bitmap frame = isSquished ? slimeSquish : slimeIdle;
        canvas.drawBitmap(frame, screenX, screenY, paint);
    }
}