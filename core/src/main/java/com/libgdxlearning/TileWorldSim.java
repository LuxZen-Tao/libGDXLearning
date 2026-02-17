package com.libgdxlearning;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileWorldSim {

    private final Random rng = new Random(12345);
    private final List<Agent> agents = new ArrayList<>();

    private byte[][] tiles;
    private int gridW, gridH;
    private int tileSize;

    // Tile IDs: must match AlivePackScreen values
    private static final byte T_FLOOR = 1;

    public void setMap(byte[][] tiles, int gridW, int gridH, int tileSize) {
        this.tiles = tiles;
        this.gridW = gridW;
        this.gridH = gridH;
        this.tileSize = tileSize;
    }

    public List<Agent> getAgents() { return agents; }

    public void spawn(int count) {
        agents.clear();

        for (int i = 0; i < count; i++) {
            Agent a = new Agent();

            int[] start = randomWalkableTile();
            a.tx = start[0];
            a.ty = start[1];

            // world pixel position at tile center
            a.x = a.tx * tileSize + tileSize * 0.5f;
            a.y = a.ty * tileSize + tileSize * 0.5f;

            // start by standing still on current tile center
            a.targetX = a.x;
            a.targetY = a.y;

            int[] goal = randomWalkableTile();
            a.gx = goal[0];
            a.gy = goal[1];

            a.speedTilesPerSec = rand(2.0f, 4.0f);
            a.repathTimer = rand(1.5f, 4.5f);

            a.r = rand(0.8f, 1f);
            a.g = rand(0.6f, 0.9f);
            a.b = rand(0.2f, 0.5f);

            agents.add(a);
        }
    }

    public void update(float dt) {
        if (tiles == null) return;

        for (Agent a : agents) {
            a.repathTimer -= dt;
            if (a.repathTimer <= 0f) {
                int[] goal = randomWalkableTile();
                a.gx = goal[0];
                a.gy = goal[1];
                a.repathTimer = rand(1.5f, 4.5f);
            }

            // Smooth move towards target tile center
            float dx = a.targetX - a.x;
            float dy = a.targetY - a.y;
            float dist2 = dx*dx + dy*dy;

            float speedPx = a.speedTilesPerSec * tileSize;

            if (dist2 > 1f) {
                float dist = (float)Math.sqrt(dist2);
                float vx = dx / dist;
                float vy = dy / dist;

                a.x += vx * speedPx * dt;
                a.y += vy * speedPx * dt;

                // clamp overshoot
                if ((a.targetX - a.x) * dx < 0) a.x = a.targetX;
                if ((a.targetY - a.y) * dy < 0) a.y = a.targetY;
            } else {
                // Snap to center and choose next step
                a.x = a.targetX;
                a.y = a.targetY;

                a.tx = (int)(a.x / tileSize);
                a.ty = (int)(a.y / tileSize);

                stepTowardGoal(a);
            }
        }
    }

    private void stepTowardGoal(Agent a) {
        int cx = a.tx;
        int cy = a.ty;

        if (cx == a.gx && cy == a.gy) {
            int[] goal = randomWalkableTile();
            a.gx = goal[0];
            a.gy = goal[1];
            return;
        }

        int bestNx = cx, bestNy = cy;
        int bestScore = Integer.MAX_VALUE;

        int[][] dirs = { {1,0}, {-1,0}, {0,1}, {0,-1} };
        shuffleDirs(dirs);

        for (int[] d : dirs) {
            int nx = cx + d[0];
            int ny = cy + d[1];
            if (!inBounds(nx, ny)) continue;
            if (!isWalkable(tiles[nx][ny])) continue;

            int score = manhattan(nx, ny, a.gx, a.gy);
            if (score < bestScore) {
                bestScore = score;
                bestNx = nx;
                bestNy = ny;
            }
        }

        if (bestNx == cx && bestNy == cy) {
            int[] goal = randomWalkableTile();
            a.gx = goal[0];
            a.gy = goal[1];
            return;
        }

        a.targetX = bestNx * tileSize + tileSize * 0.5f;
        a.targetY = bestNy * tileSize + tileSize * 0.5f;
    }

    private boolean isWalkable(byte t) {
        return t == T_FLOOR;
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < gridW && y < gridH;
    }

    private int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private int[] randomWalkableTile() {
        for (int tries = 0; tries < 5000; tries++) {
            int x = rng.nextInt(gridW);
            int y = rng.nextInt(gridH);
            if (isWalkable(tiles[x][y])) return new int[]{x, y};
        }
        return new int[]{0, 0};
    }

    private void shuffleDirs(int[][] dirs) {
        for (int i = dirs.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int[] tmp = dirs[i];
            dirs[i] = dirs[j];
            dirs[j] = tmp;
        }
    }

    private float rand(float min, float max) {
        return min + rng.nextFloat() * (max - min);
    }
}
