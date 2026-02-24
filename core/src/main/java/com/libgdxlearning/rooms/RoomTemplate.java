package com.libgdxlearning.rooms;

import com.libgdxlearning.world.tiles.TileType;

public class RoomTemplate {

    public final RoomType type;
    public final int width;
    public final int height;

    public RoomTemplate(RoomType type, int width, int height) {
        this.type = type;
        this.width = width;
        this.height = height;
    }

    public TileType floorType() {
        switch (type) {
            case KITCHEN: return TileType.FLOOR_KITCHEN;
            default:      return TileType.FLOOR_WOOD;
        }
    }

    public TileType wallType() {
        return TileType.WALL_WOOD;
    }

    public TileType doorType() {
        return TileType.DOOR_WOOD;
    }
}
