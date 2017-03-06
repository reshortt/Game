package com.example.robertshortt.game;

import android.graphics.Bitmap;

/**
 * Created by Robert Shortt on 3/5/2017.
 */

public class Animation {

    private Bitmap[] frames;
    private int currentFrame;
    private long startTime;
    private long delay;
    private boolean playedOnce;

    public void setFrames(Bitmap[] frames) {
        this.frames = frames;
        currentFrame=0;
        startTime=System.nanoTime();
    }

    public void setDelay(long d) {
        delay = d;
    }

    public void setFrame(int i) {
        currentFrame = i;
    }

    public void update() {
        long elapsed = (System.nanoTime()-startTime)/Utils.MILLION;
        if (elapsed > delay) {
            currentFrame++;
            startTime = System.nanoTime();
        }
        if (currentFrame == frames.length) {
            currentFrame=0;
            playedOnce=true;
        }
    }
    public Bitmap getImage() {
        return frames[currentFrame];
    }

    public boolean isPlayedOnce() {
        return playedOnce;
    }
}
