package com.libgdxlearning;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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

    // UI root + HUD
    private Table root;
    private Label cashLabel, repLabel, chaosLabel, moraleLabel, timeLabel;

    private Container<Label> cashBox, repBox, chaosBox, moraleBox;
    private int lastCash, lastRep, lastChaos, lastMorale;

    // Buttons
    private TextButton pauseBtn;
    private TextButton speed1Btn, speed2Btn, speed4Btn;

    // Drawers
    private Table leftDrawer, rightDrawer;
    private boolean leftOpen = false, rightOpen = false;
    private float drawerWidth = 320f;
    private float drawerAnimTime = 0.25f;
    private TextButton leftDrawerBtn, rightDrawerBtn;

    // Camera controls
    private float camSpeed = 500f;
    private float zoomSpeed = 0.08f;

    // ---- TILED MAP ----
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledRenderer;
    private OrthographicCamera worldCam;

    public AlivePackScreen() {
        // Create resources in show()
    }

    @Override
    public void show() {
        sim = new SimState();

        batch = new SpriteBatch();
        font = new BitmapFont();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Skin (your assets path setup)
        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));

        toast = new ToastManager(stage.getRoot(), skin);
        toast.show("Phase 11: Loading TMX map…");

        // Load TMX map (put in core/assets/maps/)
        tiledMap = new TmxMapLoader().load("maps/pub_test.tmx");
        tiledRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1f);

        // Camera: start by showing the screen-size view
        worldCam = new OrthographicCamera();
        worldCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        worldCam.position.set(worldCam.viewportWidth / 2f, worldCam.viewportHeight / 2f, 0f);
        worldCam.update();

        // UI layout
        root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);

        Table topHud = new Table(skin);
        topHud.defaults().pad(6);

        Table worldArea = new Table(skin);
        worldArea.defaults().pad(6);

        Table bottomControls = new Table(skin);
        bottomControls.defaults().pad(6);

        // HUD labels
        cashLabel = new Label("Cash: £0", skin);
        repLabel = new Label("Rep: 0", skin);
        chaosLabel = new Label("Chaos: 0", skin);
        moraleLabel = new Label("Morale: 0", skin);
        timeLabel = new Label("Day 1  00:00", skin);

        // Wrap HUD stats in containers so scaling + flash is visible
        cashBox = new Container<>(cashLabel);
        repBox = new Container<>(repLabel);
        chaosBox = new Container<>(chaosLabel);
        moraleBox = new Container<>(moraleLabel);

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

        // Bottom controls
        pauseBtn = new TextButton("Pause", skin);

        TextButton testToastBtn = new TextButton("Test Toast", skin);
        testToastBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                toast.show("Toast #" + (int)(Math.random() * 999));
            }
        });

        TextButton saleBtn = new TextButton("Sim Sale (+£50)", skin);
        TextButton chaosUpBtn = new TextButton("Chaos (+20)", skin);
        TextButton chaosDownBtn = new TextButton("Chaos (-50)", skin);
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

        // Drawer buttons
        leftDrawerBtn = new TextButton("Feed", skin);
        rightDrawerBtn = new TextButton("Manage", skin);

        leftDrawerBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { toggleLeftDrawer(); }
        });
        rightDrawerBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { toggleRightDrawer(); }
        });

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

        Label worldHint = new Label("TMX WORLD (WASD/Arrows to pan, mousewheel to zoom)", skin);
        worldHint.getColor().a = 0.55f;
        worldArea.add(worldHint).left().top().pad(8);

        root.top().left();
        root.add(topHud).expandX().fillX().row();
        root.add(worldArea).expand().fill().row();
        root.add(bottomControls).expandX().fillX();

        buildDrawers();

        toast.show("TMX loaded. If it’s black, it’s usually a tileset path issue.");
    }

    private void buildDrawers() {
        leftDrawer = new Table(skin);
        leftDrawer.setBackground("default-round");
        leftDrawer.defaults().pad(6).left();

        Label leftTitle = new Label("Activity Feed", skin);
        leftTitle.setAlignment(Align.left);

        leftDrawer.add(leftTitle).expandX().fillX().row();
        leftDrawer.add(new Label("- Booted sim", skin)).row();
        leftDrawer.add(new Label("- TMX renderer online", skin)).row();

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

    private void popAndFlash(Actor actor) {
        actor.clearActions();
        actor.setOrigin(Align.center);
        actor.setScale(1f);

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
        worldCam.update();
        tiledRenderer.setView(worldCam);
        tiledRenderer.render();
    }

    private void update(float delta) {
        sim.update(delta);

        handleWorldCamera(delta);

        timeLabel.setText(String.format("Day %d  %02d:%02d  |  %s  x%.0f",
                sim.day, sim.getHour(), sim.getMinute(),
                sim.paused ? "PAUSED" : "RUNNING",
                sim.speedMultiplier));

        cashLabel.setText("Cash: £" + sim.cash);
        repLabel.setText("Rep: " + sim.reputation);
        chaosLabel.setText("Chaos: " + sim.chaos);
        moraleLabel.setText("Morale: " + sim.morale);

        // Pop containers (not labels) so it’s visible
        if (sim.cash != lastCash) { popAndFlash(cashBox); lastCash = sim.cash; }
        if (sim.reputation != lastRep) { popAndFlash(repBox); lastRep = sim.reputation; }
        if (sim.chaos != lastChaos) { popAndFlash(chaosBox); lastChaos = sim.chaos; }
        if (sim.morale != lastMorale) { popAndFlash(moraleBox); lastMorale = sim.morale; }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            sim.paused = !sim.paused;
            toast.show(sim.paused ? "Paused" : "Resumed");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) sim.speedMultiplier = 1f;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) sim.speedMultiplier = 2f;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) sim.speedMultiplier = 4f;
    }

    private void handleWorldCamera(float delta) {
        float dt = Math.min(delta, 1f / 30f);
        float move = camSpeed * dt * (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 2f : 1f);

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))  worldCam.position.x -= move;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) worldCam.position.x += move;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))    worldCam.position.y += move;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))  worldCam.position.y -= move;

        // Mouse wheel zoom (Lwjgl3 uses getScrollY on InputProcessor, but this works okay for many setups)
        float scroll = -Gdx.input.getRoll();
        if (scroll != 0f) {
            worldCam.zoom = clamp(worldCam.zoom - scroll * zoomSpeed, 0.4f, 3.0f);
        }
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        // Keep world cam viewport consistent with screen
        worldCam.setToOrtho(false, width, height);

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
        if (tiledRenderer != null) tiledRenderer.dispose();
        if (tiledMap != null) tiledMap.dispose();
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
