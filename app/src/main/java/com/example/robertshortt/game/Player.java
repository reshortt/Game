package com.example.robertshortt.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;



public class Player extends GameObject {
    private Bitmap spritesheet;
    private int score;
    //private double dya;
    private boolean up;
    private boolean playing;
    private long startTime;
    private Animation animation = new Animation();
    private static final float DYA_SCALE = 0.5f;

    public Player (Bitmap res, int w, int h, int numFrames) {
        x=100;
        y = GamePanel.HEIGHT/2;
        dy=0;
        score=0;
        height=h;
        width=w;
        Bitmap[] image = new Bitmap[numFrames];
        spritesheet=res;

        for (int i=0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, i*width, 0, width,  height);
        }

        animation.setFrames(image);
        animation.setDelay(10);
        startTime = System.nanoTime();
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        System.out.println("Up going from from " + this.up + " to " + up);
        this.up = up;
    }

    private int printCount = 0;

    public void update() {
        long elapsedMillis = (System.nanoTime()-startTime)/Utils.MILLION;
        if (elapsedMillis > 1000) {
            score++;
            startTime = System.nanoTime();
        }
        animation.update();

        if (up) {
            dy -= 1;
        }
        else {
            dy += 1;
        }
        if (dy > 14)
            dy=14;
        if (dy < -14)
            dy=-14;

        y+= 2*dy;
        //dy=0;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(animation.getImage(), x, y, null);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean getPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void resetDY() {
        dy=0;
    }

    public void resetScore() {
        score=0;
    }


}
