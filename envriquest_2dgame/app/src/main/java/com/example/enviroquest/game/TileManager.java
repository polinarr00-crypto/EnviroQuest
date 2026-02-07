package com.example.enviroquest.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.example.enviroquest.R;
import java.util.HashMap;

public class TileManager {
    private int[][] map;
    private int tileSize;
    private HashMap<Integer, Bitmap> tileStore = new HashMap<>();
    private Bitmap grass, water;
    private RectF destRect = new RectF();

    public TileManager(int[][] map, int tileSize, Context context) {
        this.map = map;
        this.tileSize = tileSize;
        loadAllTiles(context);
    }

    private void loadAllTiles(Context context) {
        // Base layers
        grass = load(context, R.drawable.grass);
        water = load(context, R.drawable.water);

        // Map ID to Bitmaps
        tileStore.put(2, load(context, R.drawable.flower1));
        tileStore.put(3, load(context, R.drawable.island_buttom));
        tileStore.put(4, load(context, R.drawable.island_top));
        tileStore.put(5, load(context, R.drawable.island_left));
        tileStore.put(6, load(context, R.drawable.island_right));
        tileStore.put(7, load(context, R.drawable.island_t_l_corner));
        tileStore.put(8, load(context, R.drawable.island_t_r_corner));
        tileStore.put(9, load(context, R.drawable.island_b_l_corner));
        tileStore.put(10, load(context, R.drawable.island_b_r_corner));
    }

    private Bitmap load(Context ctx, int resId) {
        Bitmap raw = BitmapFactory.decodeResource(ctx.getResources(), resId);
        return Bitmap.createScaledBitmap(raw, tileSize, tileSize, false);
    }

    public void draw(Canvas canvas, float worldX, float worldY, int sW, int sH, Paint paint) {
        float centerX = sW / 2f - (tileSize / 2f);
        float centerY = sH / 2f - (tileSize / 2f);

        int startCol = Math.max(0, (int)((worldX - sW/2)/tileSize) - 1);
        int endCol = Math.min(map[0].length-1, (int)((worldX + sW/2)/tileSize) + 1);
        int startRow = Math.max(0, (int)((worldY - sH/2)/tileSize) - 1);
        int endRow = Math.min(map.length-1, (int)((worldY + sH/2)/tileSize) + 1);

        for (int r = startRow; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                float dx = (c * tileSize) - worldX + centerX;
                float dy = (r * tileSize) - worldY + centerY;
                destRect.set(dx, dy, dx + tileSize + 1, dy + tileSize + 1);

                int id = map[r][c];
                canvas.drawBitmap(id == 0 ? water : grass, null, destRect, paint);
                if (tileStore.containsKey(id) && id != 0) {
                    canvas.drawBitmap(tileStore.get(id), null, destRect, paint);
                }
            }
        }
    }

    public int getTileAt(int r, int c) { return map[r][c]; }
    public int getRows() { return map.length; }
    public int getCols() { return map[0].length; }
}