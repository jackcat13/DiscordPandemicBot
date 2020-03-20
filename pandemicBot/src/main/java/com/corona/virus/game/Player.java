package com.corona.virus.game;

public class Player {

    private String id;
    private int score;
    private boolean isCoronned;

    public Player(String id) {
        this.id = id;
        this.score = 0;
        this.isCoronned = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isCoronned() {
        return isCoronned;
    }

    public void setCoronned(boolean coronned) {
        isCoronned = coronned;
    }

    @Override
    public String toString() {
        return "Nom : " + id + ", score=" + score + "\n";
    }
}
