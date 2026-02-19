package com.libgdxlearning.input;

import com.badlogic.gdx.InputAdapter;
import com.libgdxlearning.render.WorldView;
import com.libgdxlearning.rooms.RoomPlacementSystem;

/**
 * Handles hover preview (mouseMoved) and click-to-place (touchDown) for the world grid.
 * Registered after the Stage in the InputMultiplexer so that UI clicks take priority.
 */
public class WorldInput extends InputAdapter {

    private final WorldView worldView;
    private final RoomPlacementSystem placer;

    public WorldInput(WorldView worldView, RoomPlacementSystem placer) {
        this.worldView = worldView;
        this.placer    = placer;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        int[] cell = worldView.screenToGrid(screenX, screenY);
        if (cell != null) {
            placer.hoverX = cell[0];
            placer.hoverY = cell[1];
        } else {
            placer.hoverX = -1;
            placer.hoverY = -1;
        }
        return false; // never steal the event
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        int[] cell = worldView.screenToGrid(screenX, screenY);
        if (cell != null) {
            placer.placeAt(cell[0], cell[1]);
            return true; // consume: placed a room
        }
        return false;
    }
}
