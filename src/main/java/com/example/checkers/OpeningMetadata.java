package com.example.checkers;

public class OpeningMetadata {


    private boolean wantsToStart;

    public OpeningMetadata(boolean wantsToStart) {
        this.wantsToStart = wantsToStart;
    }

    public boolean isWantsToStart() {
        return wantsToStart;
    }

    public void setWantsToStart(boolean wantsToStart) {
        this.wantsToStart = wantsToStart;
    }
}
