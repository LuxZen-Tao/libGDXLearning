package com.libgdxlearning.world;

import com.libgdxlearning.world.tiles.TileCell;
import com.libgdxlearning.world.tiles.TileType;

public class Grid {

    /** Logical tile size in world units (used by other systems). */
    public static final int TILE_SIZE = 128;

    public final int width;
    public final int height;

    private final TileCell[][] cells;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        cells = new TileCell[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new TileCell();
            }
        }
    }

    /** Returns the cell at (x,y), or null if out of bounds. */
    public TileCell get(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return null;
        return cells[x][y];
    }

    public boolean isFloorEmpty(int x, int y) {
        TileCell c = get(x, y);
        return c != null && c.type == TileType.EMPTY;
    }
}
