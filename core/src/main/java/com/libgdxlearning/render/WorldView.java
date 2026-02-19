package com.libgdxlearning.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.libgdxlearning.rooms.RoomPlacementSystem;
import com.libgdxlearning.world.Grid;
import com.libgdxlearning.world.tiles.TileCell;
import com.libgdxlearning.world.tiles.TileType;

/**
 * Scene2D Actor that renders the world grid and room placement preview.
 * Breaks out of the Stage's Batch to draw with ShapeRenderer, then resumes.
 */
public class WorldView extends Actor {

    /** Pixels rendered per grid cell (screen scale). */
    public static final float TILE_PX = 64f;

    private final Grid grid;
    private final RoomPlacementSystem placer;
    private final ShapeRenderer shapes;
    private final OrthographicCamera camera;

    public WorldView(Grid grid, RoomPlacementSystem placer) {
        this.grid   = grid;
        this.placer = placer;
        shapes = new ShapeRenderer();
        camera = new OrthographicCamera();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Actor's bottom-left in stage coords (ScreenViewport: stage == screen pixels).
        Vector2 pos = localToStageCoordinates(new Vector2(0, 0));
        int vpX = (int) pos.x;
        int vpY = (int) pos.y;
        int vpW = (int) getWidth();
        int vpH = (int) getHeight();
        if (vpW <= 0 || vpH <= 0) return;

        // Flush the Stage batch before switching to ShapeRenderer.
        batch.end();

        // Restrict rendering to this actor's screen area.
        Gdx.gl.glViewport(vpX, vpY, vpW, vpH);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(vpX, vpY, vpW, vpH);

        // Camera covers exactly the actor's local area (1 unit = 1 pixel).
        camera.setToOrtho(false, vpW, vpH);
        camera.update();
        shapes.setProjectionMatrix(camera.combined);

        // --- Background ---
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.18f, 0.22f, 0.18f, 1f);
        shapes.rect(0, 0, vpW, vpH);
        shapes.end();

        // --- Grid lines (only visible tiles) ---
        int colMax = Math.min(grid.width,  (int)(vpW / TILE_PX) + 2);
        int rowMax = Math.min(grid.height, (int)(vpH / TILE_PX) + 2);

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.28f, 0.33f, 0.28f, 1f);
        for (int gx = 0; gx <= colMax; gx++) {
            float px = gx * TILE_PX;
            shapes.line(px, 0, px, rowMax * TILE_PX);
        }
        for (int gy = 0; gy <= rowMax; gy++) {
            float py = gy * TILE_PX;
            shapes.line(0, py, colMax * TILE_PX, py);
        }
        shapes.end();

        // --- Placed tiles ---
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int gx = 0; gx < colMax && gx < grid.width; gx++) {
            for (int gy = 0; gy < rowMax && gy < grid.height; gy++) {
                TileCell c = grid.get(gx, gy);
                if (c != null && c.type != TileType.EMPTY) {
                    shapes.setColor(tileColor(c.type));
                    shapes.rect(gx * TILE_PX, gy * TILE_PX, TILE_PX, TILE_PX);
                }
            }
        }
        shapes.end();

        // --- Preview overlay (semi-transparent) ---
        int hx = placer.hoverX;
        int hy = placer.hoverY;
        if (hx >= 0 && hy >= 0) {
            boolean valid = placer.canPlaceAt(hx, hy);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(valid ? 0f : 1f, valid ? 1f : 0f, 0f, 0.35f);
            shapes.rect(
                hx * TILE_PX, hy * TILE_PX,
                placer.activeTemplate.width  * TILE_PX,
                placer.activeTemplate.height * TILE_PX
            );
            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        // Restore full-screen state for subsequent actors.
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.begin();
    }

    // Cached tile colours to avoid per-frame allocations.
    private static final Color COL_FLOOR_WOOD    = new Color(0.60f, 0.45f, 0.25f, 1f);
    private static final Color COL_FLOOR_KITCHEN = new Color(0.50f, 0.55f, 0.70f, 1f);
    private static final Color COL_WALL_WOOD     = new Color(0.35f, 0.25f, 0.15f, 1f);
    private static final Color COL_DOOR_WOOD     = new Color(0.80f, 0.60f, 0.20f, 1f);

    private Color tileColor(TileType type) {
        switch (type) {
            case FLOOR_WOOD:    return COL_FLOOR_WOOD;
            case FLOOR_KITCHEN: return COL_FLOOR_KITCHEN;
            case WALL_WOOD:     return COL_WALL_WOOD;
            case DOOR_WOOD:     return COL_DOOR_WOOD;
            default:            return Color.BLACK;
        }
    }

    /**
     * Converts screen coordinates (Gdx.input style, Y from top) to grid cell coords.
     * Returns null if the pointer is outside this actor's bounds or off the grid.
     */
    public int[] screenToGrid(int screenX, int screenY) {
        if (getStage() == null) return null;

        // Stage.screenToStageCoordinates handles the Y flip.
        Vector2 stageCoords = getStage().screenToStageCoordinates(new Vector2(screenX, screenY));
        Vector2 local = stageToLocalCoordinates(stageCoords);

        if (local.x < 0 || local.y < 0 || local.x >= getWidth() || local.y >= getHeight()) {
            return null;
        }

        int gx = (int)(local.x / TILE_PX);
        int gy = (int)(local.y / TILE_PX);
        if (gx < 0 || gy < 0 || gx >= grid.width || gy >= grid.height) return null;
        return new int[]{gx, gy};
    }

    public void dispose() {
        shapes.dispose();
    }
}
