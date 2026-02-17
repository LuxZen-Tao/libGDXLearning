package com.libgdxlearning;

public class SimState {

    public double minutes = 0;
    public int day = 1;

    public boolean paused = false;
    public float speedMultiplier = 1f; // 0 = paused, 1 = normal, 2 = fast

    public int cash = 500;
    public int reputation = 50;
    public int chaos = 0;
    public int morale = 50;

    public void update(float delta) {
        if (paused) return;

        minutes += delta * 60f * speedMultiplier; // 60 game minutes per real second

        if (minutes >= 1440) { // 1440 minutes in a day
            minutes = 0;
            day++;
        }
    }

    public int getHour() {
        return (int)(minutes / 60);
    }

    public int getMinute() {
        return (int)(minutes % 60);
    }

    public void addCash(int amount) { cash += amount; }
    public void addRep(int amount) { reputation += amount; }
    public void addChaos(int amount) { chaos = Math.max(0, chaos + amount); }
    public void addMorale(int amount) { morale = Math.max(0, morale + amount); }

}