package com.libgdxlearning;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Renders the Intees.tmx pub map with player movement and rectangle collision.
 *
 * Layer render order:
 *   ground → floor → Walls → Furnis → Objects  (below player)
 *   [player rectangle drawn here]
 *   Above → Above2 → Above3                     (above player)
 *
 * Press ESC to return to AlivePackScreen.
 */
public class TiledWorldScreen implements Screen {

    private static final float SPEED    = 150f; // world-units per second
    private static final float PLAYER_W = 20f;
    private static final float PLAYER_H = 28f;

    private final Game game;

    private TiledMap                    map;
    private OrthogonalTiledMapRenderer  mapRenderer;
    private OrthographicCamera          camera;
    private ScreenViewport              viewport;
    private ShapeRenderer               shapes;

    private final Vector2        playerPos = new Vector2();
    private final Array<Rectangle> colliders = new Array<>();
    private final Array<Rectangle> zones     = new Array<>();
    private final Rectangle      playerRect = new Rectangle();

    public TiledWorldScreen(Game game) {
        this.game = game;
    }

    // -------------------------------------------------------------------------
    // Screen lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void show() {
        camera   = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        shapes   = new ShapeRenderer();

        map         = new TmxMapLoader().load("maps/Intees.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        spawnPlayer();
        loadColliders();
        loadZones();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new AlivePackScreen());
            return;
        }

        updatePlayer(delta);
        centerCamera();

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.setView(camera);

        // Below-player tile layers
        renderNamedLayers("ground", "floor", "Walls", "Furnis", "Objects");

        // Player rectangle
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.2f, 0.6f, 1f, 1f);
        shapes.rect(playerPos.x - PLAYER_W / 2f, playerPos.y - PLAYER_H / 2f, PLAYER_W, PLAYER_H);
        shapes.end();

        // Above-player tile layers
        renderNamedLayers("Above", "Above2", "Above3");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        if (mapRenderer != null) mapRenderer.dispose();
        if (map         != null) map.dispose();
        if (shapes      != null) shapes.dispose();
    }

    // -------------------------------------------------------------------------
    // Map loading helpers
    // -------------------------------------------------------------------------

    private void spawnPlayer() {
        MapLayer layer = map.getLayers().get("Spawns");
        if (layer == null) { playerPos.set(200, 200); return; }
        for (MapObject obj : layer.getObjects()) {
            if ("Player_Spawn".equals(obj.getName())) {
                float ox = obj.getProperties().get("x", Float.class);
                float oy = obj.getProperties().get("y", Float.class);
                playerPos.set(ox, mapHeightPx() - oy);
                return;
            }
        }
        playerPos.set(200, 200);
    }

    private void loadColliders() {
        colliders.clear();
        MapLayer layer = map.getLayers().get("Colliders");
        if (layer == null) return;
        float h = mapHeightPx();
        for (MapObject obj : layer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                colliders.add(new Rectangle(r.x, h - r.y - r.height, r.width, r.height));
            }
        }
    }

    private void loadZones() {
        zones.clear();
        MapLayer layer = map.getLayers().get("Zones");
        if (layer == null) return;
        float h = mapHeightPx();
        for (MapObject obj : layer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                zones.add(new Rectangle(r.x, h - r.y - r.height, r.width, r.height));
            }
        }
    }

    /** Total map height in world pixels. */
    private float mapHeightPx() {
        int rows  = map.getProperties().get("height",     Integer.class);
        int tileH = map.getProperties().get("tileheight", Integer.class);
        return rows * tileH;
    }

    // -------------------------------------------------------------------------
    // Rendering helpers
    // -------------------------------------------------------------------------

    /** Renders only the named tile layers (object layers are ignored by the renderer). */
    private void renderNamedLayers(String... names) {
        int count = 0;
        for (String name : names) {
            if (map.getLayers().getIndex(name) >= 0) count++;
        }
        if (count == 0) return;
        int[] indices = new int[count];
        int i = 0;
        for (String name : names) {
            int idx = map.getLayers().getIndex(name);
            if (idx >= 0) indices[i++] = idx;
        }
        mapRenderer.render(indices);
    }

    /** Keeps the camera inside the map boundaries. */
    private void centerCamera() {
        float hw   = viewport.getWorldWidth()  / 2f;
        float hh   = viewport.getWorldHeight() / 2f;
        float mapW = map.getProperties().get("width",  Integer.class)
                   * map.getProperties().get("tilewidth", Integer.class);
        float mapH = mapHeightPx();
        camera.position.set(
            Math.max(hw, Math.min(playerPos.x, mapW - hw)),
            Math.max(hh, Math.min(playerPos.y, mapH - hh)),
            0f
        );
        camera.update();
    }

    // -------------------------------------------------------------------------
    // Player movement + collision
    // -------------------------------------------------------------------------

    private void updatePlayer(float delta) {
        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))    dy += SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))  dy -= SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))  dx -= SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx += SPEED * delta;
        if (dx != 0 || dy != 0) tryMove(dx, dy);
    }

    /** Axis-separated collision: attempt X, then Y independently. */
    private void tryMove(float dx, float dy) {
        playerPos.x += dx;
        if (collidesWithAny()) playerPos.x -= dx;
        playerPos.y += dy;
        if (collidesWithAny()) playerPos.y -= dy;
    }

    private boolean collidesWithAny() {
        playerRect.set(
            playerPos.x - PLAYER_W / 2f, playerPos.y - PLAYER_H / 2f, PLAYER_W, PLAYER_H);
        for (Rectangle r : colliders) {
            if (playerRect.overlaps(r)) return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Sim integration bridge
    // -------------------------------------------------------------------------

    /** Returns a copy of the player's current world position. */
    public Vector2 getPlayerPosition() { return new Vector2(playerPos); }

    /** Returns a copy of the loaded Zone rectangles for sim logic. */
    public Array<Rectangle> getZones() { return new Array<>(zones); }
}
