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

public class AlivePackScreen implements Screen {

    private SpriteBatch batch;
    private BitmapFont font;

    private SimState sim;

    private Stage stage;
    private Skin skin;
    private ToastManager toast;

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
    private WorldSim worldSim;

    // World area bounds (screen coords)
    private float worldX, worldY, worldW, worldH;

    public AlivePackScreen() {
        // Intentionally empty: create resources in show() to avoid leaks / lifecycle issues.
    }

    @Override
    public void show() {
        sim = new SimState();

        batch = new SpriteBatch();
        font = new BitmapFont();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        whitePixel = DebugPixel.createWhitePixel();
        worldSim = new WorldSim();

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

        topHud.add(timeLabel).left().expandX().fillX();
        topHud.add(cashLabel).padLeft(12);
        topHud.add(repLabel).padLeft(12);
        topHud.add(chaosLabel).padLeft(12);
        topHud.add(moraleLabel).padLeft(12);

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
                sim.addChaos(2);
                toast.show("Chaos increased");
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
        worldSim.setBounds(worldX, worldY, worldW, worldH);
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

    private void pop(com.badlogic.gdx.scenes.scene2d.Actor actor) {
        actor.clearActions();
        actor.setOrigin(Align.center);
        actor.setScale(1f);

        actor.addAction(Actions.sequence(
                Actions.scaleTo(1.12f, 1.12f, 0.08f),
                Actions.scaleTo(1.0f, 1.0f, 0.12f)
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
        batch.begin();

        // Floor
        batch.setColor(0.08f, 0.08f, 0.12f, 1f);
        batch.draw(whitePixel, worldX, worldY, worldW, worldH);

        float time = (float) sim.minutes * 0.02f; // slow oscillation

        for (Agent a : worldSim.getAgents()) {
            float size = 12f;

            // Bob = tiny up/down movement
            float bob = (float) Math.sin((a.x + a.y) * 0.01f + time) * 2f;

            // Per-agent colour variance (requires Agent.r/g/b fields + spawn init)
            batch.setColor(a.r, a.g, a.b, 1f);

            batch.draw(
                    whitePixel,
                    a.x - size / 2f,
                    a.y - size / 2f + bob,
                    size,
                    size
            );
        }

        // Reset for safety
        batch.setColor(1f, 1f, 1f, 1f);

        batch.end();
    }



    private void update(float delta) {
        sim.update(delta);

        float dt = Math.min(delta, 1f / 30f);
        if (!sim.paused) {
            worldSim.update(dt * sim.speedMultiplier);
        }
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

        if (sim.cash != lastCash) { pop(cashLabel); lastCash = sim.cash; }
        if (sim.reputation != lastRep) { pop(repLabel); lastRep = sim.reputation; }
        if (sim.chaos != lastChaos) { pop(chaosLabel); lastChaos = sim.chaos; }
        if (sim.morale != lastMorale) { pop(moraleLabel); lastMorale = sim.morale; }

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
        worldSim.setBounds(worldX, worldY, worldW, worldH);

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
}
