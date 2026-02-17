package com.libgdxlearning;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChaosFX {

    private final Random rng = new Random(777);
    private final List<Spill> spills = new ArrayList<>();
    private final List<AlertBubble> bubbles = new ArrayList<>();
    private float bubbleSpawnTimer = 0f;

    public List<AlertBubble> getBubbles() { return bubbles; }


    // Spawn tuning
    private float spillSpawnTimer = 0f;

    public List<Spill> getSpills() { return spills; }

    public void update(float dt, int chaos, float worldX, float worldY, float worldW, float worldH) {
        // Decay spills
        for (int i = spills.size() - 1; i >= 0; i--) {
            Spill s = spills.get(i);
            s.life -= dt;
            if (s.life <= 0f) spills.remove(i);
        }
        // Decay bubbles
        for (int i = bubbles.size() - 1; i >= 0; i--) {
            AlertBubble b = bubbles.get(i);
            b.life -= dt;
            if (b.life <= 0f) bubbles.remove(i);
        }

        if (chaos >= 10) {
            float bubbleEvery = chaos >= 18 ? 0.6f : 1.4f;
            bubbleSpawnTimer -= dt;
            if (bubbleSpawnTimer <= 0f) {
                bubbleSpawnTimer = bubbleEvery;

                AlertBubble b = new AlertBubble();
                b.agentIndex = -1; // choose later in screen when we know agent count
                b.maxLife = 1.2f;
                b.life = b.maxLife;
                bubbles.add(b);

                // cap bubbles
                while (bubbles.size() > 10) bubbles.remove(0);
            }
        }


        // Spawn rate scales with chaos
        // chaos 0-5: none
        // chaos 6-15: occasional
        // chaos 16+: frequent
        if (chaos < 6) return;

        float spawnEvery = chaos >= 16 ? 0.8f : 1.8f; // seconds
        spillSpawnTimer -= dt;
        if (spillSpawnTimer > 0f) return;
        spillSpawnTimer = spawnEvery;

        // Spawn a spill at a random position in the world
        Spill s = new Spill();
        s.x = rand(worldX + 20, worldX + worldW - 20);
        s.y = rand(worldY + 20, worldY + worldH - 20);
        s.radius = rand(10f, 28f);
        s.maxLife = rand(8f, 18f);
        s.life = s.maxLife;

        spills.add(s);

        // Cap total spills so it doesn’t accumulate forever
        int cap = 8 + chaos; // grows with chaos
        while (spills.size() > cap) spills.remove(0);
    }

    private float rand(float min, float max) {
        return min + rng.nextFloat() * (max - min);
    }
}
