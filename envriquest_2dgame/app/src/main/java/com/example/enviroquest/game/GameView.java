package com.example.enviroquest.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {
    public boolean isAPressed;
    public boolean isBPressed;
    public boolean isInventoryOpen;
    private UI ui;
    private Thread gameThread;
    private boolean isPlaying;
    private final SurfaceHolder holder;
    private final Paint paint;

    // Movement flags gikan sa imong Activity/Buttons
    public boolean moveUp, moveDown, moveLeft, moveRight;

    // Mao na lang ni ang imong main components
    private Player player;
    private TileManager tileManager;
    private CollisionChecker collisionChecker;
    private TrashManager trashManager;
    private Inventory inventory;
    private int score = 0;

    private final int SCALED_SIZE = 128;
    private long lastFrameTime;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        paint = new Paint();
        paint.setFilterBitmap(false);

        initGame();
    }

    private void initGame() {
        // 1. I-setup ang Map (Puwede nimo ibalhin ang array sa TileManager later)
        int[][] mapData = {
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,7,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,8,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,2,1,1,1,1,1,1,1,1,1,1,1,2,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,5,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,6,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,9,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,10,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
        };
        tileManager = new TileManager(mapData, SCALED_SIZE, getContext());

        // 2. I-setup ang Player (worldX, worldY, size, context)
        player = new Player(25 * SCALED_SIZE, 25 * SCALED_SIZE, SCALED_SIZE, getContext());

        // 3. I-setup ang Collision
        collisionChecker = new CollisionChecker(tileManager, SCALED_SIZE);

        trashManager = new TrashManager(SCALED_SIZE);

        inventory = new Inventory();
        ui = new UI(this, inventory);

    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getScore() {
        return score;
    }

    @Override
    public void run() {
        // 1. I-calculate nato ang target speed (1,000,000,000 nanoseconds / 60 FPS)
        double drawInterval = 1000000000 / 60.0;
        double nextDrawTime = System.nanoTime() + drawInterval;

        lastFrameTime = System.currentTimeMillis();

        while (isPlaying) {
            // Tawgon ang update ug draw
            update();
            draw();

            // Lock frame rate to 60 FPS
            try {
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1000000; // convert to ms

                if (remainingTime < 0) {
                    remainingTime = 0;
                }

                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000f;
        lastFrameTime = currentTime;

        if (deltaTime > 0.05f) deltaTime = 0.05f;

        player.update(moveUp, moveDown, moveLeft, moveRight, deltaTime, collisionChecker);

        // I-cast nato ang float padung int gamit ang (int)
        Rect playerRect = new Rect(
                (int) player.worldX,
                (int) player.worldY,
                (int) (player.worldX + SCALED_SIZE),
                (int) (player.worldY + SCALED_SIZE)
        );

        // Ang trashManager na ang mo-check kung naay napunit
        // Imbis nga score += ..., tawgon na nato ang inventory
        trashManager.update(playerRect, inventory);

    }

    private void draw() {
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();
            if (canvas == null) return;

            // 1. Background
            canvas.drawColor(Color.BLACK);

            // 2. Draw the Map (TileManager)
            tileManager.draw(canvas, player.worldX, player.worldY, getWidth(), getHeight(), paint);

            // 3. Draw the Trash (Manager)
            trashManager.draw(canvas,(int) player.worldX,(int) player.worldY, getWidth(), getHeight(), paint);

            // I-display ang listahan sa items
            int yOffset = 150; // Sugod sa ubos sa score

            // 4. Draw the Player
            int screenCenterX = Math.round(getWidth() / 2f - (SCALED_SIZE / 2f));
            int screenCenterY = Math.round(getHeight() / 2f - (SCALED_SIZE / 2f));
            player.draw(canvas, screenCenterX, screenCenterY, paint);

            for (InventoryItem item : inventory.getItems()) {
                canvas.drawText(item.name + ": x" + item.count, 50, yOffset, paint);
                yOffset += 50; // I-move sa ubos para sa sunod nga item
            }

            ui.draw(canvas);

            // 6. PINAKA-UBOS: Unlock and Post
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void resume() { isPlaying = true; gameThread = new Thread(this); gameThread.start(); }
    public void pause() { isPlaying = false; try { gameThread.join(); } catch (InterruptedException e) { e.printStackTrace(); } }
}