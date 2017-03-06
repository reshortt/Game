package com.example.robertshortt.game;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by Robert Shortt on 3/5/2017.
 */

public class MainThread extends Thread {
    private int FPS = 30;
    private double averageFPS;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;
    public static Canvas canvas;

    public MainThread(SurfaceHolder holder, GamePanel panel) {
        super();
        surfaceHolder = holder;
        gamePanel = panel;
    }

    @Override
    public void run() {
        long startTimeNanos, waitTime, totalTimeNanos = 0;
        long targetTime = 1000 / FPS;
        int frameCount = 0;

        while (running) {
            startTimeNanos = System.nanoTime();
            canvas = null;

            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    gamePanel.update();
                    gamePanel.draw(canvas);
                }
            } catch (Exception e) {
                System.out.println("Exception locking canvas: " + e.getMessage());
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            long currentTime = System.nanoTime();
            long elapsedTimeMillis = (currentTime - startTimeNanos) / Utils.MILLION;
            waitTime = targetTime - elapsedTimeMillis;

                try {
                    sleep(waitTime);
                } catch (Exception e) {
                    //nuttin
                }

            long elapsedTimeNanos = System.nanoTime() - startTimeNanos;
            totalTimeNanos += elapsedTimeNanos;
            frameCount++;
            if (frameCount == FPS) {
                averageFPS = 1000 / ((totalTimeNanos / frameCount) / Utils.MILLION);
                frameCount = 0;
                totalTimeNanos=0;
                //System.out.println("Avg FPS = " + averageFPS);
            }
        }
    }

    public void setRunning(boolean b) {
        running = b;
    }
}
