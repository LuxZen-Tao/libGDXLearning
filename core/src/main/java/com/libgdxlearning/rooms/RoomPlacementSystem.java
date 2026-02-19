package com.libgdxlearning.rooms;

import com.libgdxlearning.world.Grid;
import com.libgdxlearning.world.tiles.TileCell;
import com.libgdxlearning.world.tiles.TileType;

public class RoomPlacementSystem {

    private final Grid grid;

    /** The template currently selected for placement. Swap to change templates. */
    public RoomTemplate activeTemplate;

    /** Current hover position in grid cells (-1 = no hover). */
    public int hoverX = -1;
    public int hoverY = -1;

    public RoomPlacementSystem(Grid grid) {
        this.grid = grid;
        activeTemplate = new RoomTemplate(RoomType.MAIN_BAR, 10, 8);
    }

    /** Returns true if the active template fits entirely within the grid and all cells are empty. */
    public boolean canPlaceAt(int x, int y) {
        int w = activeTemplate.width;
        int h = activeTemplate.height;
        // Require at least 3×3 so that interior floors and border walls both exist.
        if (w < 3 || h < 3) return false;
        if (x < 0 || y < 0 || x + w > grid.width || y + h > grid.height) return false;
        for (int gx = x; gx < x + w; gx++) {
            for (int gy = y; gy < y + h; gy++) {
                if (!grid.isFloorEmpty(gx, gy)) return false;
            }
        }
        return true;
    }

    /**
     * Stamps the active template at (x,y):
     * - fills interior cells with the floor type
     * - sets border cells to the wall type (blocked)
     * - places a door at the bottom-middle wall cell (unblocked)
     */
    public void placeAt(int x, int y) {
        if (!canPlaceAt(x, y)) return;

        int w = activeTemplate.width;
        int h = activeTemplate.height;
        TileType floor = activeTemplate.floorType();
        TileType wall  = activeTemplate.wallType();
        TileType door  = activeTemplate.doorType();

        // Fill interior floors
        for (int gx = x + 1; gx < x + w - 1; gx++) {
            for (int gy = y + 1; gy < y + h - 1; gy++) {
                TileCell c = grid.get(gx, gy);
                c.type    = floor;
                c.blocked = false;
            }
        }

        // Border walls
        for (int gx = x; gx < x + w; gx++) {
            for (int gy = y; gy < y + h; gy++) {
                boolean isBorder = (gx == x || gx == x + w - 1 || gy == y || gy == y + h - 1);
                if (isBorder) {
                    TileCell c = grid.get(gx, gy);
                    c.type    = wall;
                    c.blocked = true;
                }
            }
        }

        // Door at bottom-middle
        int doorX = x + w / 2;
        int doorY = y;
        TileCell doorCell = grid.get(doorX, doorY);
        doorCell.type    = door;
        doorCell.blocked = false;
    }
}
