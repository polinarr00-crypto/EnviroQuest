package com.example.enviroquest.game;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.example.enviroquest.R;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;
    private View joystickCenter;
    private TextView tvScore, tvTimer, tvTrashCount;
    private Handler hudHandler = new Handler();
    private Runnable hudUpdater;

    private int activePointerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        hideSystemUI();
        setContentView(R.layout.activity_game);

        gameView = findViewById(R.id.gameView);
        joystickCenter = findViewById(R.id.joystick_center);
        tvScore = findViewById(R.id.tv_game_score);
        tvTimer = findViewById(R.id.tv_game_timer);
        tvTrashCount = findViewById(R.id.tv_trash_count);
        ImageButton btnInventory = findViewById(R.id.btn_inventory);

        setupMovementController();
        setupActionButton(findViewById(R.id.btn_a), "A");
        setupActionButton(findViewById(R.id.btn_b), "B");

        findViewById(R.id.btn_menu).setOnClickListener(v -> {
        });

        btnInventory.setOnClickListener(v -> {
            gameView.isInventoryOpen = !gameView.isInventoryOpen;
        });

        // Sugdan ang pag-update sa HUD
        startHUDUpdates();
    }

    private void startHUDUpdates() {
        hudUpdater = new Runnable() {
            @Override
            public void run() {
                if (gameView != null && gameView.getInventory() != null) {
                    // I-update ang Trash Count gikan sa Inventory
                    int totalTrash = gameView.getInventory().getTotalTrashCount();
                    tvTrashCount.setText(String.valueOf(totalTrash));
                    
                    // Pwede sad i-update ang score diri kung naa
                    // tvScore.setText("ENERGY: " + gameView.getScore());
                }
                hudHandler.postDelayed(this, 200); // I-update matag 200ms
            }
        };
        hudHandler.post(hudUpdater);
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUI();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupMovementController() {
        ConstraintLayout controlsLayout = findViewById(R.id.controls_layout);

        controlsLayout.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            int pointerIndex = event.getActionIndex();
            int pointerId = event.getPointerId(pointerIndex);

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (activePointerId == -1) {
                        activePointerId = pointerId;
                        if (joystickCenter != null) joystickCenter.setPressed(true);
                        updateMovementZones(event.getX(pointerIndex), event.getY(pointerIndex), v.getWidth(), v.getHeight());
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (activePointerId != -1) {
                        int activeIndex = event.findPointerIndex(activePointerId);
                        if (activeIndex != -1) {
                            updateMovementZones(event.getX(activeIndex), event.getY(activeIndex), v.getWidth(), v.getHeight());
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (pointerId == activePointerId) {
                        activePointerId = -1;
                        resetDirections();
                        updateButtonVisuals();
                        if (joystickCenter != null) joystickCenter.setPressed(false);
                    }
                    break;
            }
            return true;
        });
    }

    private void updateMovementZones(float x, float y, int width, int height) {
        float centerX = width / 2f;
        float centerY = height / 2f;
        float deadZone = 35f;

        resetDirections();

        if (y < centerY - deadZone) gameView.moveUp = true;
        if (y > centerY + deadZone) gameView.moveDown = true;
        if (x < centerX - deadZone) gameView.moveLeft = true;
        if (x > centerX + deadZone) gameView.moveRight = true;

        updateButtonVisuals();
    }

    private void updateButtonVisuals() {
        findViewById(R.id.btn_up).setPressed(gameView.moveUp);
        findViewById(R.id.btn_down).setPressed(gameView.moveDown);
        findViewById(R.id.btn_left).setPressed(gameView.moveLeft);
        findViewById(R.id.btn_right).setPressed(gameView.moveRight);
    }

    private void resetDirections() {
        gameView.moveUp = false; gameView.moveDown = false;
        gameView.moveLeft = false; gameView.moveRight = false;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupActionButton(View btn, final String type) {
        if (btn == null) return;
        btn.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            boolean isPressed = (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN);
            boolean isReleased = (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL);

            if (isPressed) {
                if (type.equals("A")) gameView.isAPressed = true;
                if (type.equals("B")) gameView.isBPressed = true;
                v.setPressed(true);
            } else if (isReleased) {
                if (type.equals("A")) gameView.isAPressed = false;
                if (type.equals("B")) gameView.isBPressed = false;
                v.setPressed(false);
            }
            return true;
        });
    }

    @Override
    protected void onResume() { 
        super.onResume(); 
        gameView.resume(); 
        if (hudUpdater != null) hudHandler.post(hudUpdater);
    }
    
    @Override
    protected void onPause() { 
        super.onPause(); 
        gameView.pause(); 
        hudHandler.removeCallbacks(hudUpdater);
    }
}
