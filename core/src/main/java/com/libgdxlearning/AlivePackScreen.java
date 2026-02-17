package com.libgdxlearning;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class AlivePackScreen implements Screen {

    private SpriteBatch batch;
    private BitmapFont font;

    public AlivePackScreen() {
        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    @Override
    public void show() {
        // Called when this screen becomes the current screen for a Game.

        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.06f, 0.06f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "Alive Pack Gym - Phase 0 (Game + Screen booted)", 20, Gdx.graphics.getHeight() - 20);
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, Gdx.graphics.getHeight() - 45);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Called when the application is resized.
    }

    @Override
    public void pause() {
        // Called when the application is paused.
    }

    @Override
    public void resume() {
        // Called when the application is resumed after being paused.
    }

    @Override
    public void hide() {
        // Called when this screen is no longer the current screen for a Game.
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
