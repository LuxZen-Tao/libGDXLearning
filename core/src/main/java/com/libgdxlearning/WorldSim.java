package com.libgdxlearning;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldSim {

    private final Random rng = new Random(12345); // stable for gym
    private final List<Agent> agents = new ArrayList<>();

    // World bounds (set from screen layout)
    private float x, y, w, h;

    public void setBounds(float x, float y, float w, float h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
    }

    public List<Agent> getAgents() { return agents; }

    public void spawn(int count) {
        agents.clear();
        for (int i = 0; i < count; i++) {
            Agent a = new Agent();
            a.x = rand(x, x + w);
            a.y = rand(y, y + h);
            pickNewTarget(a);
            a.speed = rand(25f, 60f);
            a.changeTimer = rand(0.5f, 2.5f);
            agents.add(a);
        }
    }

    public void update(float dt) {
        if (w <= 0 || h <= 0) return;

        for (Agent a : agents) {
            a.changeTimer -= dt;
            if (a.changeTimer <= 0f) {
                pickNewTarget(a);
                a.changeTimer = rand(0.8f, 3.0f);
            }

            // Move towards target (simple seek)
            float dx = a.tx - a.x;
            float dy = a.ty - a.y;
            float dist2 = dx*dx + dy*dy;

            if (dist2 < 4f) { // close enough
                pickNewTarget(a);
            } else {
                float dist = (float)Math.sqrt(dist2);
                float vx = dx / dist;
                float vy = dy / dist;

                a.x += vx * a.speed * dt;
                a.y += vy * a.speed * dt;
            }

            // Clamp inside bounds
            a.x = clamp(a.x, x, x + w);
            a.y = clamp(a.y, y, y + h);
        }
    }

    private void pickNewTarget(Agent a) {
        a.tx = rand(x, x + w);
        a.ty = rand(y, y + h);
    }

    private float rand(float min, float max) {
        return min + rng.nextFloat() * (max - min);
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
