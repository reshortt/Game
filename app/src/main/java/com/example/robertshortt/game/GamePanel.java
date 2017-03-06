package com.example.robertshortt.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by Robert Shortt on 3/5/2017.
 */


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;

    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smokepuffs;
    private long smokeStartTime;

    public GamePanel(Context context) {
        super(context);

        // add the callback to surface holder to intercept events
        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);

        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Bitmap grassyKnoll = BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1);
        Bitmap helicoptor = BitmapFactory.decodeResource(getResources(), R.drawable.helicopter);

        bg = new Background(grassyKnoll);

        // image is 200x40
        player = new Player(helicoptor, 66, 25, 3);


        smokepuffs = new ArrayList<>();

        smokeStartTime = System.nanoTime();

        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000) {
            try {
                counter++;
                thread.setRunning(false);
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!player.getPlaying()) {
                player.setPlaying(true);
            } else {
                player.setUp(false);
            }
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(true);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void update() {
        if (player.getPlaying()) {
            bg.update();
            player.update();
            long elapsed = (System.nanoTime() - smokeStartTime) / Utils.MILLION;
            if (elapsed > 120) {
                smokepuffs.add(new Smokepuff(player.getX(), player.getY() + 10));
                smokeStartTime = System.nanoTime();
            }

            ArrayList<Smokepuff> toRemove = new ArrayList<>();
            for (Smokepuff puff : smokepuffs) {
                puff.update();
                if (puff.getX() < -10) {
                    toRemove.add(puff);
                }
            }
            smokepuffs.removeAll(toRemove);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        final float scaleFactorX = getWidth() / (float) WIDTH;
        final float scaleFactorY = getHeight() / (float) HEIGHT;

        if (canvas != null) {
            int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            player.draw(canvas);
            for (Smokepuff puff: smokepuffs)  {
                puff.draw(canvas);
            }
            canvas.restoreToCount(savedState);
        }
    }


}
