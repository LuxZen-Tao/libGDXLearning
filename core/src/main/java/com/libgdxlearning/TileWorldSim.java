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

    private List<int[]> barTiles;
    private List<int[]> tableTiles;
    private List<int[]> toiletTiles;
    private int[][] occ;


    // Tile IDs: must match AlivePackScreen values
    private static final byte T_FLOOR = 1;

    public void setMap(byte[][] tiles, int gridW, int gridH, int tileSize) {
        this.tiles = tiles;
        this.gridW = gridW;
        this.gridH = gridH;
        this.tileSize = tileSize;
        this.occ = new int[gridW][gridH];

    }

    public void setHotspots(List<int[]> barTiles, List<int[]> tableTiles, List<int[]> toiletTiles) {
        this.barTiles = barTiles;
        this.tableTiles = tableTiles;
        this.toiletTiles = toiletTiles;
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

            a.targetX = a.x;
            a.targetY = a.y;

            // Needs start low
            a.needToilet = rand(0f, 0.25f);
            a.patience = rand(0f, 0.6f);

            int[] goal = pickGoalFor(a);
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
        rebuildOccupancy();

        for (Agent a : agents) {

            // Needs drift upward every frame (not only on repath)
            a.needToilet = Math.min(1f, a.needToilet + dt * 0.03f);

            // Idle/patience: if waiting, do nothing this frame
            if (a.patience > 0f) {
                a.patience -= dt;
                continue;
            }

            // Repath timer: occasionally choose a new goal
            a.repathTimer -= dt;
            if (a.repathTimer <= 0f) {
                int[] goal = pickGoalFor(a);
                a.gx = goal[0];
                a.gy = goal[1];
                a.repathTimer = rand(1.5f, 4.5f);
            }

            // Smooth move towards next target tile center
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

        // Arrived at goal
        if (cx == a.gx && cy == a.gy) {
            a.patience = rand(0.2f, 1.2f);

            // If arrived at toilet tile, reset need
            if (isToiletTile(cx, cy)) {
                a.needToilet = rand(0f, 0.2f);
            }

            int[] goal = pickGoalFor(a);
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

            int distScore = manhattan(nx, ny, a.gx, a.gy);

// soft occupancy penalty:
// 0 occupants = +0
// 1 occupant  = +2
// 2 occupants = +5 etc
            int occPenalty = occ[nx][ny] == 0 ? 0 : (2 + occ[nx][ny] * 3);

// tiny randomness to prevent identical choices
            int noise = rng.nextInt(2); // 0 or 1

            int score = distScore + occPenalty + noise;
            if (score < bestScore) {
                bestScore = score;
                bestNx = nx;
                bestNy = ny;
            }
        }

        // If stuck, pick a new goal
        if (bestNx == cx && bestNy == cy) {
            int[] goal = pickGoalFor(a);
            a.gx = goal[0];
            a.gy = goal[1];
            return;
        }

        a.targetX = bestNx * tileSize + tileSize * 0.5f;
        a.targetY = bestNy * tileSize + tileSize * 0.5f;
    }

    private boolean isToiletTile(int x, int y) {
        if (toiletTiles == null || toiletTiles.isEmpty()) return false;
        for (int[] t : toiletTiles) {
            if (t[0] == x && t[1] == y) return true;
        }
        return false;
    }

    private int[] pickGoalFor(Agent a) {
        float toiletWeight = 0.15f + a.needToilet * 2.5f;
        float barWeight = 1.2f;
        float tableWeight = 1.6f;

        boolean hasBar = barTiles != null && !barTiles.isEmpty();
        boolean hasTables = tableTiles != null && !tableTiles.isEmpty();
        boolean hasToilets = toiletTiles != null && !toiletTiles.isEmpty();

        float total = 0f;
        if (hasTables) total += tableWeight;
        if (hasBar) total += barWeight;
        if (hasToilets) total += toiletWeight;

        if (total <= 0f) return randomWalkableTile();

        float roll = rng.nextFloat() * total;

        if (hasTables) {
            roll -= tableWeight;
            if (roll <= 0f) return pickRandomFrom(tableTiles);
        }
        if (hasBar) {
            roll -= barWeight;
            if (roll <= 0f) return pickRandomFrom(barTiles);
        }
        if (hasToilets) {
            return pickRandomFrom(toiletTiles);
        }

        return randomWalkableTile();
    }

    private int[] pickRandomFrom(List<int[]> list) {
        return list.get(rng.nextInt(list.size()));
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
    private void rebuildOccupancy() {
        // clear
        for (int x = 0; x < gridW; x++) {
            for (int y = 0; y < gridH; y++) {
                occ[x][y] = 0;
            }
        }

        // count agents by tile
        for (Agent a : agents) {
            int tx = a.tx;
            int ty = a.ty;
            if (inBounds(tx, ty)) occ[tx][ty]++;
        }
    }

}
