package com.libgdxlearning;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.libgdxlearning.input.WorldInput;
import com.libgdxlearning.render.WorldView;
import com.libgdxlearning.rooms.RoomPlacementSystem;
import com.libgdxlearning.world.Grid;

public class AlivePackScreen implements Screen {

    private SimState sim;

    private Stage stage;
    private Skin skin;
    private ToastManager toast;

    // World
    private WorldView worldView;

    // HUD
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
    private final float drawerWidth = 320f;
    private final float drawerAnimTime = 0.25f;

    @Override
    public void show() {
        sim = new SimState();

        stage = new Stage(new ScreenViewport());

        // World model + placement system
        Grid grid = new Grid(80, 60);
        RoomPlacementSystem placer = new RoomPlacementSystem(grid);
        worldView = new WorldView(grid, placer);
        worldView.setTouchable(Touchable.disabled); // prevent Stage hit-testing from intercepting world-area clicks (handled by WorldInput)

        // InputMultiplexer: UI stage first (buttons take priority), then world input
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new WorldInput(worldView, placer));
        Gdx.input.setInputProcessor(multiplexer);

        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));
        toast = new ToastManager(stage.getRoot(), skin);

        root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);

        // Top HUD bar
        Table topHud = new Table(skin);
        topHud.defaults().pad(6);

        timeLabel   = new Label("Day 1  00:00", skin);
        cashLabel   = new Label("Cash: £0", skin);
        repLabel    = new Label("Rep: 0", skin);
        chaosLabel  = new Label("Chaos: 0", skin);
        moraleLabel = new Label("Morale: 0", skin);

        cashBox   = new Container<>(cashLabel);
        repBox    = new Container<>(repLabel);
        chaosBox  = new Container<>(chaosLabel);
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

        lastCash   = sim.cash;
        lastRep    = sim.reputation;
        lastChaos  = sim.chaos;
        lastMorale = sim.morale;

        // Bottom controls
        pauseBtn  = new TextButton("Pause",  skin);
        speed1Btn = new TextButton("x1",     skin);
        speed2Btn = new TextButton("x2",     skin);
        speed4Btn = new TextButton("x4",     skin);

        TextButton saleBtn        = new TextButton("+£50 Sale", skin);
        TextButton chaosUpBtn     = new TextButton("Chaos +20", skin);
        TextButton chaosDownBtn   = new TextButton("Chaos -50", skin);
        TextButton repDownBtn     = new TextButton("Rep -3",    skin);
        TextButton moraleUpBtn    = new TextButton("Morale +2", skin);
        TextButton leftDrawerBtn  = new TextButton("Feed",      skin);
        TextButton rightDrawerBtn = new TextButton("Manage",    skin);

        pauseBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                sim.paused = !sim.paused;
                toast.show(sim.paused ? "Paused" : "Resumed");
            }
        });
        speed1Btn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                sim.speedMultiplier = 1f; sim.paused = false; toast.show("Speed x1");
            }
        });
        speed2Btn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                sim.speedMultiplier = 2f; sim.paused = false; toast.show("Speed x2");
            }
        });
        speed4Btn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                sim.speedMultiplier = 4f; sim.paused = false; toast.show("Speed x4");
            }
        });
        saleBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                sim.addCash(50); toast.show("+£50 sale");
            }
        });
        chaosUpBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                sim.addChaos(20); toast.show("Chaos increased");
            }
        });
        chaosDownBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                sim.addChaos(-50); toast.show("Chaos decreased");
            }
        });
        repDownBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                sim.addRep(-3); toast.show("Rep hit");
            }
        });
        moraleUpBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                sim.addMorale(2); toast.show("Morale boosted");
            }
        });
        leftDrawerBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { toggleLeftDrawer(); }
        });
        rightDrawerBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { toggleRightDrawer(); }
        });

        Table bottomControls = new Table(skin);
        bottomControls.defaults().pad(4);
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

        root.top().left();
        root.add(topHud).expandX().fillX().row();
        root.add(worldView).expand().fill().row(); // world view fills center
        root.add(bottomControls).expandX().fillX();

        buildDrawers();

        toast.show("Welcome to libGDX!");
    }

    private void buildDrawers() {
        leftDrawer = new Table(skin);
        leftDrawer.setBackground("default-round");
        leftDrawer.defaults().pad(6).left();

        Label leftTitle = new Label("Activity Feed", skin);
        leftTitle.setAlignment(Align.left);
        leftDrawer.add(leftTitle).expandX().fillX().row();
        leftDrawer.add(new Label("- Sim started", skin)).row();

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
        rightDrawer.add(new TextButton("Do Thing", skin)).row();

        rightDrawer.setSize(drawerWidth, Gdx.graphics.getHeight());
        rightDrawer.setPosition(Gdx.graphics.getWidth(), 0);
        rightDrawer.setTouchable(Touchable.disabled);
        stage.addActor(rightDrawer);
    }

    private void toggleLeftDrawer() {
        leftOpen = !leftOpen;
        leftDrawer.clearActions();
        if (leftOpen) {
            leftDrawer.setTouchable(Touchable.enabled);
            leftDrawer.addAction(Actions.moveTo(0, 0, drawerAnimTime));
        } else {
            leftDrawer.addAction(Actions.sequence(
                    Actions.moveTo(-drawerWidth, 0, drawerAnimTime),
                    Actions.run(() -> leftDrawer.setTouchable(Touchable.disabled))
            ));
        }
    }

    private void toggleRightDrawer() {
        rightOpen = !rightOpen;
        float screenW = stage.getViewport().getWorldWidth();
        rightDrawer.clearActions();
        if (rightOpen) {
            rightDrawer.setTouchable(Touchable.enabled);
            rightDrawer.addAction(Actions.moveTo(screenW - drawerWidth, 0, drawerAnimTime));
        } else {
            rightDrawer.addAction(Actions.sequence(
                    Actions.moveTo(screenW, 0, drawerAnimTime),
                    Actions.run(() -> rightDrawer.setTouchable(Touchable.disabled))
            ));
        }
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
        sim.update(delta);

        // Keyboard shortcuts
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            sim.paused = !sim.paused;
            toast.show(sim.paused ? "Paused" : "Resumed");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) { sim.speedMultiplier = 1f; sim.paused = false; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) { sim.speedMultiplier = 2f; sim.paused = false; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) { sim.speedMultiplier = 4f; sim.paused = false; }

        // Update HUD
        timeLabel.setText(String.format("Day %d  %02d:%02d  |  %s  x%.0f",
                sim.day, sim.getHour(), sim.getMinute(),
                sim.paused ? "PAUSED" : "RUNNING",
                sim.speedMultiplier));
        cashLabel.setText("Cash: £" + sim.cash);
        repLabel.setText("Rep: " + sim.reputation);
        chaosLabel.setText("Chaos: " + sim.chaos);
        moraleLabel.setText("Morale: " + sim.morale);

        if (sim.cash != lastCash)          { popAndFlash(cashBox);   lastCash   = sim.cash; }
        if (sim.reputation != lastRep)     { popAndFlash(repBox);    lastRep    = sim.reputation; }
        if (sim.chaos != lastChaos)        { popAndFlash(chaosBox);  lastChaos  = sim.chaos; }
        if (sim.morale != lastMorale)      { popAndFlash(moraleBox); lastMorale = sim.morale; }

        Gdx.gl.glClearColor(0.12f, 0.12f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(delta, 1f / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (leftDrawer != null) {
            leftDrawer.setHeight(height);
            leftDrawer.setPosition(leftOpen ? 0 : -drawerWidth, 0);
        }
        float screenW = stage.getViewport().getWorldWidth();
        if (rightDrawer != null) {
            rightDrawer.setHeight(height);
            rightDrawer.setPosition(rightOpen ? screenW - drawerWidth : screenW, 0);
        }
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        if (stage != null)     stage.dispose();
        if (skin  != null)     skin.dispose();
        if (worldView != null) worldView.dispose();
    }
}
