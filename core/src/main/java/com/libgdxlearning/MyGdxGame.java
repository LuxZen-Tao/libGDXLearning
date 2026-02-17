package com.libgdxlearning;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class MyGdxGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont(); // uses libGDX's default Arial font
        font.setColor(Color.WHITE);
        font.getData().setScale(2); // make the font bigger
    }

    @Override
    public void render() {
        // Clear the screen with a nice blue color
        ScreenUtils.clear(0.2f, 0.3f, 0.8f, 1);

        batch.begin();
        font.draw(batch, "Welcome to libGDX!", 100, 300);
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 100, 250);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
