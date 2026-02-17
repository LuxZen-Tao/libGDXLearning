package com.libgdxlearning;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class UISkinTestScreen implements Screen {

    private Stage stage;
    private Skin skin;

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        
        // Load the UI skin
        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));
        
        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        // Create UI elements using the skin
        Label titleLabel = new Label("UI Skin Assets Loaded Successfully!", skin);
        TextButton button1 = new TextButton("Test Button 1", skin);
        TextButton button2 = new TextButton("Test Button 2", skin);
        Label infoLabel = new Label("The uiskin.json, uiskin.atlas, uiskin.png, and default.fnt are working!", skin);
        
        // Add elements to the table
        table.add(titleLabel).padBottom(20);
        table.row();
        table.add(button1).width(200).padBottom(10);
        table.row();
        table.add(button2).width(200).padBottom(20);
        table.row();
        table.add(infoLabel);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
