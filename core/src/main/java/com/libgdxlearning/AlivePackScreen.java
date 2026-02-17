package com.libgdxlearning;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.Input;


public class AlivePackScreen implements Screen {
    private static final byte T_VOID   = 0;
    private static final byte T_FLOOR  = 1;
    private static final byte T_WALL   = 2;
    private static final byte T_BAR    = 3;
    private static final byte T_KITCH  = 4;
    private static final byte T_TABLE  = 5;
    private static final byte T_TOILET = 6;


    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera worldCam;

    private SimState sim;


    private Stage stage;
    private Skin skin;
    private ToastManager toast;
    private ChaosFX chaosFX;
    private int lastChaosForSpike = 0;

    // tiny camera shake offsets
    private float shakeTime = 0f;
    private float shakeStrength = 0f;
    private Container<Label> cashBox;
    private Container<Label> repBox;
    private Container<Label> chaosBox;
    private Container<Label> moraleBox;


    private Table root;
    private Label cashLabel;
    private Label repLabel;
    private Label chaosLabel;
    private Label moraleLabel;
    private Label timeLabel;

    // Track last values so we can detect changes
    private int lastCash, lastRep, lastChaos, lastMorale;

    private TextButton pauseBtn;
    private TextButton speed1Btn, speed2Btn, speed4Btn;

    private Table leftDrawer;
    private Table rightDrawer;

    private boolean leftOpen = false;
    private boolean rightOpen = false;

    private float drawerWidth = 320f;
    private float drawerAnimTime = 0.25f;

    private TextButton leftDrawerBtn;
    private TextButton rightDrawerBtn;

    private Texture whitePixel;
    private TileWorldSim worldSim;

    // World area bounds (screen coords)
    private float worldX, worldY, worldW, worldH;
    private int gridW = 40;
    private int gridH = 26;
    private int tileSize = 16; // Gameboy-ish chunk
    private byte[][] tiles;

    // Camera tuning
    private float camSpeed = 300f;
    private float zoomSpeed = 0.08f;

    public AlivePackScreen() {
        // Intentionally empty: create resources in show() to avoid leaks / lifecycle issues.
    }

    @Override
    public void show() {
        sim = new SimState();
        chaosFX = new ChaosFX();
        lastChaosForSpike = sim.chaos;


        batch = new SpriteBatch();
        font = new BitmapFont();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        whitePixel = DebugPixel.createWhitePixel();
        worldCam = new OrthographicCamera();
        worldCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        worldCam.position.set((gridW * tileSize) / 2f, (gridH * tileSize) / 2f, 0f);
        worldCam.zoom = 1.0f;
        worldCam.update();

        tiles = new byte[gridW][gridH];
        buildDemoPubLayout();

        worldSim = new TileWorldSim();
        worldSim.setMap(tiles, gridW, gridH, tileSize);
        worldSim.spawn(30);
        // Your assets are inside desktop/resources/assets/, so include "assets/" in the path:
        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));

        toast = new ToastManager(stage.getRoot(), skin);
        toast.show("Alive Pack Gym: Phase 6 (World Online)");

        root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);

        // Layout rows: Top HUD, Center World, Bottom Controls
        Table topHud = new Table(skin);
        topHud.defaults().pad(6);

        Table worldArea = new Table(skin);
        worldArea.defaults().pad(6);

        Table bottomControls = new Table(skin);
        bottomControls.defaults().pad(6);

        // --- HUD (Phase 5)
        cashLabel = new Label("Cash: £0", skin);
        repLabel = new Label("Rep: 0", skin);
        chaosLabel = new Label("Chaos: 0", skin);
        moraleLabel = new Label("Morale: 0", skin);
        timeLabel = new Label("Day 1  00:00", skin);

// Wrap stats in containers so scale + flash is visible
        cashBox = new Container<>(cashLabel);
        repBox = new Container<>(repLabel);
        chaosBox = new Container<>(chaosLabel);
        moraleBox = new Container<>(moraleLabel);

// Important: allow transforms (so scaling works reliably)
        cashBox.setTransform(true);
        repBox.setTransform(true);
        chaosBox.setTransform(true);
        moraleBox.setTransform(true);

        topHud.add(timeLabel).left().expandX().fillX();
        topHud.add(cashBox).padLeft(12);
        topHud.add(repBox).padLeft(12);
        topHud.add(chaosBox).padLeft(12);
        topHud.add(moraleBox).padLeft(12);


        lastCash = sim.cash;
        lastRep = sim.reputation;
        lastChaos = sim.chaos;
        lastMorale = sim.morale;

        // --- Bottom controls
        pauseBtn = new TextButton("Pause", skin);

        TextButton testToastBtn = new TextButton("Test Toast", skin);
        testToastBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                toast.show("Toast #" + (int)(Math.random() * 999));
            }
        });

        TextButton saleBtn = new TextButton("Sim Sale (+£50)", skin);
        TextButton chaosUpBtn = new TextButton("Chaos (+2)", skin);
        TextButton chaosDownBtn = new TextButton("Chaos (-5)", skin);

        TextButton repDownBtn = new TextButton("Rep (-3)", skin);
        TextButton moraleUpBtn = new TextButton("Morale (+2)", skin);

        saleBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                sim.addCash(50);
                toast.show("+£50 sale");
            }
        });

        chaosUpBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                sim.addChaos(20);
                toast.show("Chaos increased");
            }
        });
        chaosDownBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                sim.addChaos(-50);
                toast.show("Chaos decreased");
            }
        });

        repDownBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                sim.addRep(-3);
                toast.show("Rep hit");
            }
        });

        moraleUpBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                sim.addMorale(2);
                toast.show("Morale boosted");
            }
        });

        speed1Btn = new TextButton("x1", skin);
        speed2Btn = new TextButton("x2", skin);
        speed4Btn = new TextButton("x4", skin);

        pauseBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                sim.paused = !sim.paused;
                toast.show(sim.paused ? "Paused" : "Resumed");
            }
        });

        speed1Btn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                sim.speedMultiplier = 1f;
                sim.paused = false;
                toast.show("Speed set to x1");
            }
        });

        speed2Btn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                sim.speedMultiplier = 2f;
                sim.paused = false;
                toast.show("Speed set to x2");
            }
        });

        speed4Btn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                sim.speedMultiplier = 4f;
                sim.paused = false;
                toast.show("Speed set to x4");
            }
        });

        // Drawer buttons (Phase 3)
        leftDrawerBtn = new TextButton("Feed", skin);
        rightDrawerBtn = new TextButton("Manage", skin);

        leftDrawerBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                toggleLeftDrawer();
            }
        });

        rightDrawerBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                toggleRightDrawer();
            }
        });

        // Bottom bar assembly
        bottomControls.add(testToastBtn);
        bottomControls.add(saleBtn);
        bottomControls.add(chaosUpBtn);
        bottomControls.add(chaosDownBtn);
        bottomControls.add(repDownBtn);
        bottomControls.add(moraleUpBtn);
        bottomControls.add(pauseBtn);
        bottomControls.add(speed1Btn);
        bottomControls.add(speed2Btn);
        bottomControls.add(speed4Btn);
        bottomControls.add(leftDrawerBtn);
        bottomControls.add(rightDrawerBtn);

        Label worldPlaceholder = new Label("WORLD VIEW", skin);
        worldPlaceholder.getColor().a = 0.6f; // subtle
        worldArea.add(worldPlaceholder).left().top().pad(8);

        // Root layout
        root.top().left();
        root.add(topHud).expandX().fillX().row();
        root.add(worldArea).expand().fill().row();
        root.add(bottomControls).expandX().fillX();

        // Drawers
        buildDrawers();

        // Initial world bounds guess (also set properly in resize)
        computeWorldBounds(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        worldSim.spawn(30);

        toast.show("Agents spawned: " + worldSim.getAgents().size());
    }

    private void buildDrawers() {
        leftDrawer = new Table(skin);
        leftDrawer.setBackground("default-round");
        leftDrawer.defaults().pad(6).left();

        Label leftTitle = new Label("Activity Feed", skin);
        leftTitle.setAlignment(Align.left);

        leftDrawer.add(leftTitle).expandX().fillX().row();
        leftDrawer.add(new Label("- Booted sim", skin)).row();
        leftDrawer.add(new Label("- Drawers online", skin)).row();

        leftDrawer.setSize(drawerWidth, Gdx.graphics.getHeight());
        leftDrawer.setPosition(-drawerWidth, 0);
        leftDrawer.setTouchable(Touchable.disabled);
        stage.addActor(leftDrawer);

        rightDrawer = new Table(skin);
        rightDrawer.setBackground("default-round");
        rightDrawer.defaults().pad(6).left();

        Label rightTitle = new Label("Management", skin);
        rightTitle.setAlignment(Align.left);

        rightDrawer.add(rightTitle).expandX().fillX().row();
        rightDrawer.add(new Label("Placeholder controls", skin)).row();
        rightDrawer.add(new TextButton("Do Thing", skin)).row();

        rightDrawer.setSize(drawerWidth, Gdx.graphics.getHeight());
        rightDrawer.setPosition(Gdx.graphics.getWidth(), 0);
        rightDrawer.setTouchable(Touchable.disabled);
        stage.addActor(rightDrawer);
    }

    private void computeWorldBounds(int width, int height) {
        worldX = 30;
        worldY = 120;

        // Clamp so world never becomes negative / invisible.
        worldW = Math.max(200, width - 60);
        worldH = Math.max(200, height - 220);
    }

    private void toggleLeftDrawer() {
        leftOpen = !leftOpen;
        if (leftOpen) openLeftDrawer(); else closeLeftDrawer();
    }

    private void toggleRightDrawer() {
        rightOpen = !rightOpen;
        if (rightOpen) openRightDrawer(); else closeRightDrawer();
    }

    private void openLeftDrawer() {
        leftDrawer.clearActions();
        leftDrawer.setTouchable(Touchable.enabled);
        leftDrawer.addAction(Actions.moveTo(0, 0, drawerAnimTime));
    }

    private void closeLeftDrawer() {
        leftDrawer.clearActions();
        leftDrawer.addAction(Actions.sequence(
                Actions.moveTo(-drawerWidth, 0, drawerAnimTime),
                Actions.run(() -> leftDrawer.setTouchable(Touchable.disabled))
        ));
    }

    private void openRightDrawer() {
        float screenW = stage.getViewport().getWorldWidth();
        rightDrawer.clearActions();
        rightDrawer.setTouchable(Touchable.enabled);
        rightDrawer.addAction(Actions.moveTo(screenW - drawerWidth, 0, drawerAnimTime));
    }

    private void closeRightDrawer() {
        float screenW = stage.getViewport().getWorldWidth();
        rightDrawer.clearActions();
        rightDrawer.addAction(Actions.sequence(
                Actions.moveTo(screenW, 0, drawerAnimTime),
                Actions.run(() -> rightDrawer.setTouchable(Touchable.disabled))
        ));
    }

    private void popAndFlash(com.badlogic.gdx.scenes.scene2d.Actor actor) {
        actor.clearActions();

        actor.setOrigin(Align.center);
        actor.setScale(1f);

        // Flash: brighten then return (works on container tint)
        actor.getColor().a = 1f;

        actor.addAction(Actions.parallel(
                Actions.sequence(
                        Actions.scaleTo(1.18f, 1.18f, 0.07f),
                        Actions.scaleTo(1.0f, 1.0f, 0.12f)
                ),
                        Actions.sequence(
                                Actions.color(new com.badlogic.gdx.graphics.Color(1f, 0.9f, 0.3f, 1f), 0.06f),
                                Actions.color(new com.badlogic.gdx.graphics.Color(1f, 1f, 1f, 1f), 0.14f)
                        )


        ));
    }


    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.06f, 0.06f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawWorld();

        stage.act(Math.min(delta, 1f / 30f));
        stage.draw();
    }

    private void drawWorld() {
        batch.setProjectionMatrix(worldCam.combined);

        batch.begin();
        float ox = 0f, oy = 0f;
        if (shakeTime > 0f) {
            // deterministic-ish jitter using time
            float t = (float)sim.minutes * 0.2f;
            ox = (float)Math.sin(t * 13.7f) * shakeStrength;
            oy = (float)Math.cos(t * 11.3f) * shakeStrength;
        }

        batch.setProjectionMatrix(worldCam.combined);

        // Floor
        // Clear-ish background “outside building”
        batch.setColor(0.04f, 0.04f, 0.06f, 1f);
        batch.draw(whitePixel, -5000, -5000, 10000, 10000);

// Draw tiles
        for (int x = 0; x < gridW; x++) {
            for (int y = 0; y < gridH; y++) {
                byte t = tiles[x][y];
                if (t == T_VOID) continue;

                // pick a color per tile type (simple palette)
                switch (t) {
                    case T_FLOOR:  batch.setColor(0.16f, 0.14f, 0.12f, 1f); break;
                    case T_WALL:   batch.setColor(0.28f, 0.26f, 0.23f, 1f); break;
                    case T_BAR:    batch.setColor(0.25f, 0.14f, 0.08f, 1f); break;
                    case T_KITCH:  batch.setColor(0.14f, 0.20f, 0.16f, 1f); break;
                    case T_TABLE:  batch.setColor(0.22f, 0.16f, 0.10f, 1f); break;
                    case T_TOILET: batch.setColor(0.14f, 0.16f, 0.22f, 1f); break;
                    default:       batch.setColor(1, 1, 1, 1); break;
                }

                batch.draw(whitePixel, x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }
        batch.setColor(1f, 1f, 1f, 1f);
        for (Agent a : worldSim.getAgents()) {
            float size = tileSize * 0.6f;
            batch.setColor(a.r, a.g, a.b, 1f);
            batch.draw(
                    whitePixel,
                    a.x - size / 2f,
                    a.y - size / 2f,
                    size,
                    size
            );
        }


        // Spills (semi-transparent, fade out)
        for (Spill s : chaosFX.getSpills()) {
            float alpha = Math.max(0f, s.life / s.maxLife);
            batch.setColor(0.6f, 0.2f, 0.2f, 0.25f * alpha); // reddish stain
            float r = s.radius;
            batch.draw(whitePixel, s.x + ox - r, s.y + oy - r, r * 2, r * 2);
        }

        // Alert bubbles above random agents
        if (!worldSim.getAgents().isEmpty()) {
            int agentCount = worldSim.getAgents().size();
            for (AlertBubble b : chaosFX.getBubbles()) {
                // choose a target agent if not set or out of range
                if (b.agentIndex < 0 || b.agentIndex >= agentCount) {
                    b.agentIndex = (int)(Math.random() * agentCount);
                }

                Agent a = worldSim.getAgents().get(b.agentIndex);
                float alpha = Math.max(0f, b.life / b.maxLife);

                // bubble background
                batch.setColor(1f, 1f, 1f, 0.75f * alpha);
                batch.draw(whitePixel, a.x + ox - 7, a.y + oy + 18, 14, 14);

                // exclamation mark (tiny rectangle)
                batch.setColor(0.1f, 0.1f, 0.1f, 0.9f * alpha);
                batch.draw(whitePixel, a.x + ox - 1, a.y + oy + 21, 2, 8);
                batch.draw(whitePixel, a.x + ox - 1, a.y + oy + 18, 2, 2);
            }
        }


        // Reset for safety
        batch.setColor(1f, 1f, 1f, 1f);

        batch.end();
    }
    private boolean isWalkable(byte t) {
        return t == T_FLOOR; // keep strict for now
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < gridW && y < gridH;
    }



    private void update(float delta) {
        sim.update(delta);
        handleWorldCamera(delta);


        float dt = Math.min(delta, 1f / 30f);
        if (!sim.paused) {
            worldSim.update(dt * sim.speedMultiplier);
        }
        // Chaos FX update (spills + bubbles)
        chaosFX.update(dt, sim.chaos, worldX, worldY, worldW, worldH);

// If chaos jumped up, trigger a tiny shake
        if (sim.chaos > lastChaosForSpike) {
            int jump = sim.chaos - lastChaosForSpike;
            shakeTime = 0.25f;
            shakeStrength = Math.min(6f, 2f + jump * 1.5f);
        }
        lastChaosForSpike = sim.chaos;

// decay shake
        if (shakeTime > 0f) shakeTime -= dt;

        int targetCount = 20 + sim.chaos * 2;

        if (worldSim.getAgents().size() != targetCount) {
            worldSim.spawn(targetCount);
        }

        timeLabel.setText(String.format("Day %d  %02d:%02d  |  %s  x%.0f",
                sim.day, sim.getHour(), sim.getMinute(),
                sim.paused ? "PAUSED" : "RUNNING",
                sim.speedMultiplier));

        cashLabel.setText("Cash: £" + sim.cash);
        repLabel.setText("Rep: " + sim.reputation);
        chaosLabel.setText("Chaos: " + sim.chaos);
        moraleLabel.setText("Morale: " + sim.morale);

        if (sim.cash != lastCash) { popAndFlash(cashLabel); lastCash = sim.cash; }
        if (sim.reputation != lastRep) { popAndFlash(repLabel); lastRep = sim.reputation; }
        if (sim.chaos != lastChaos) { popAndFlash(chaosLabel); lastChaos = sim.chaos; }
        if (sim.morale != lastMorale) { popAndFlash(moraleLabel); lastMorale = sim.morale; }

        // Optional keyboard controls still work
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            sim.paused = !sim.paused;
            toast.show(sim.paused ? "Paused" : "Resumed");
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_1)) sim.speedMultiplier = 1f;
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_2)) sim.speedMultiplier = 2f;
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_3)) sim.speedMultiplier = 4f;
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        // World bounds
        computeWorldBounds(width, height);

        // Drawers height + positions
        if (leftDrawer != null) leftDrawer.setHeight(height);
        if (rightDrawer != null) rightDrawer.setHeight(height);

        if (leftDrawer != null) {
            if (leftOpen) leftDrawer.setPosition(0, 0);
            else leftDrawer.setPosition(-drawerWidth, 0);
        }

        float screenW = stage.getViewport().getWorldWidth();
        if (rightDrawer != null) {
            if (rightOpen) rightDrawer.setPosition(screenW - drawerWidth, 0);
            else rightDrawer.setPosition(screenW, 0);
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (whitePixel != null) whitePixel.dispose();
    }

    private void buildDemoPubLayout() {
        // Default void
        for (int x = 0; x < gridW; x++) {
            for (int y = 0; y < gridH; y++) tiles[x][y] = T_VOID;
        }

        // Pub footprint
        int x0 = 3, y0 = 3;
        int x1 = gridW - 4, y1 = gridH - 4;

        // Floors
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) tiles[x][y] = T_FLOOR;
        }

        // Walls border
        for (int x = x0; x <= x1; x++) {
            tiles[x][y0] = T_WALL;
            tiles[x][y1] = T_WALL;
        }
        for (int y = y0; y <= y1; y++) {
            tiles[x0][y] = T_WALL;
            tiles[x1][y] = T_WALL;
        }

        // Bar (top-left-ish)
        for (int x = x0 + 3; x <= x0 + 14; x++) {
            for (int y = y1 - 6; y <= y1 - 5; y++) tiles[x][y] = T_BAR;
        }

        // Kitchen room (top-right)
        for (int x = x1 - 10; x <= x1 - 2; x++) {
            for (int y = y1 - 9; y <= y1 - 2; y++) tiles[x][y] = T_KITCH;
        }
        // Kitchen “walls” outline
        for (int x = x1 - 10; x <= x1 - 2; x++) {
            tiles[x][y1 - 9] = T_WALL;
            tiles[x][y1 - 2] = T_WALL;
        }
        for (int y = y1 - 9; y <= y1 - 2; y++) {
            tiles[x1 - 10][y] = T_WALL;
            tiles[x1 - 2][y]  = T_WALL;
        }

        // Toilets (bottom-right)
        for (int x = x1 - 8; x <= x1 - 2; x++) {
            for (int y = y0 + 2; y <= y0 + 7; y++) tiles[x][y] = T_TOILET;
        }
        for (int x = x1 - 8; x <= x1 - 2; x++) {
            tiles[x][y0 + 2] = T_WALL;
            tiles[x][y0 + 7] = T_WALL;
        }
        for (int y = y0 + 2; y <= y0 + 7; y++) {
            tiles[x1 - 8][y] = T_WALL;
            tiles[x1 - 2][y] = T_WALL;
        }

        // Tables scattered
        for (int x = x0 + 6; x <= x1 - 12; x += 5) {
            for (int y = y0 + 6; y <= y1 - 12; y += 4) {
                tiles[x][y] = T_TABLE;
            }
        }
    }

    private void handleWorldCamera(float delta) {
        float dt = Math.min(delta, 1f/30f);

        float move = camSpeed * dt * (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 2f : 1f);

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))  worldCam.position.x -= move;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) worldCam.position.x += move;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))    worldCam.position.y += move;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))  worldCam.position.y -= move;

        // Zoom (mouse wheel)
        float scroll = -Gdx.input.getRoll(); // lwjgl3 uses scrollY
        if (scroll != 0) {
            worldCam.zoom = clamp(worldCam.zoom - scroll * zoomSpeed, 0.5f, 3.0f);
        }

        worldCam.update();
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }


}
