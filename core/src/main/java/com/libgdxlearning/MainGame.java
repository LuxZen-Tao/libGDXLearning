package com.libgdxlearning;

import com.badlogic.gdx.Game;

public class MainGame extends Game {

    @Override
    public void create() {
        setScreen(new AlivePackScreen());
    }

    @Override
    public void dispose() {
        // Game.dispose() will dispose the current screen too
        super.dispose();
    }
}