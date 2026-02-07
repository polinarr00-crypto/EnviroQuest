package com.example.enviroquest.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class UI {
    private GameView gv;
    private Paint paint;
    private Inventory inventory;

    public UI(GameView gv, Inventory inventory) {
        this.gv = gv;
        this.inventory = inventory;
        this.paint = new Paint();
    }

    public void draw(Canvas canvas) {
        if (gv.isInventoryOpen) {
            drawInventory(canvas);
        }
    }

    private void drawInventory(Canvas canvas) {
        // 1. Gamay ra nga dim effect (dili tibuok screen)
        paint.setColor(Color.argb(150, 0, 0, 0));

        // 2. I-set ang gidak-on (Mas gamay na siya karon)
        // Atong i-center sa screen pero 50% ra sa width ug height
        int boxWidth = gv.getWidth() / 2;
        int boxHeight = gv.getHeight() / 2;

        int boxLeft = (gv.getWidth() - boxWidth) / 2;
        int boxTop = (gv.getHeight() - boxHeight) / 2;
        int boxRight = boxLeft + boxWidth;
        int boxBottom = boxTop + boxHeight;

        // 3. Main Box (Dark Gray/Blue)
        paint.setColor(Color.parseColor("#1E293B"));
        canvas.drawRoundRect(boxLeft, boxTop, boxRight, boxBottom, 40, 40, paint);

        // 4. Border (Light Blue)
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#38BDF8"));
        paint.setStrokeWidth(6);
        canvas.drawRoundRect(boxLeft, boxTop, boxRight, boxBottom, 40, 40, paint);
        paint.setStyle(Paint.Style.FILL);

        // 5. Header (Gamay nga font)
        paint.setColor(Color.WHITE);
        paint.setTextSize(40); // Gikan sa 70, gihimo natong 40
        paint.setFakeBoldText(true);
        canvas.drawText("BAG SULOD", boxLeft + 40, boxTop + 70, paint);

        // Separator line
        canvas.drawLine(boxLeft + 40, boxTop + 90, boxRight - 40, boxTop + 90, paint);

        // 6. Items List (Mas sikit ug mas gamay)
        paint.setFakeBoldText(false);
        paint.setTextSize(35);
        int itemY = boxTop + 150;

        if (inventory.getItems().isEmpty()) {
            paint.setColor(Color.GRAY);
            canvas.drawText("Empty...", boxLeft + 50, itemY, paint);
        } else {
            for (InventoryItem item : inventory.getItems()) {
                // Item Background
                paint.setColor(Color.argb(30, 255, 255, 255));
                canvas.drawRoundRect(boxLeft + 30, itemY - 45, boxRight - 30, itemY + 20, 10, 10, paint);

                // Text
                paint.setColor(Color.WHITE);
                canvas.drawText(item.name, boxLeft + 50, itemY, paint);

                paint.setColor(Color.parseColor("#38BDF8"));
                canvas.drawText("x" + item.count, boxRight - 100, itemY, paint);

                itemY += 80; // Mas gamay nga spacing
            }
        }
    }
}