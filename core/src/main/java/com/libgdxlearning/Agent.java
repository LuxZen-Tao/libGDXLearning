package com.libgdxlearning;

public class Agent {
    // current tile
    public int tx, ty;

    // goal tile
    public int gx, gy;

    // current position in world pixels (center of tile)
    public float x, y;

    // next target tile center in pixels
    public float targetX, targetY;

    public float speedTilesPerSec;
    public float repathTimer;

    // color tint
    public float r, g, b;
    public float needToilet; // 0..1
    public float patience;   // idle time when reaching target

}
