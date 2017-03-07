package com.example.robertshortt.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;

    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smokepuffs;
    private long smokeStartTime, missleStartTime;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topBorders;
    private ArrayList<BotBorder> botBorders;
    private Random rand = new Random();
    private int maxBorderHeight, minBorderHeight;
    private boolean topDown = true, botDown = false;
    private Bitmap borderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.brick);
    private boolean newGameCreated;
    private int nextRandomTop=1, nextRandomBot=1;


    // increase to slow down difficult progression
    private int progressDenom = 5;

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
        missiles = new ArrayList<>();
        topBorders = new ArrayList<>();
        botBorders = new ArrayList<>();

        smokeStartTime = missleStartTime = System.nanoTime();

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
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!player.getPlaying()) {
                player.setPlaying(true);
                player.setUp(true);
            }
            else {
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


            // calculate the threshold of height the border can have based on the score
            // max and min border height are updated, and teh border switches direction when either
            // max or min is set

            maxBorderHeight = 30 + player.getScore() / progressDenom;

            // cap max border eight so that borders can only take up 1/2 screen height
            if (maxBorderHeight > HEIGHT / 4)
                maxBorderHeight = HEIGHT / 4;
            minBorderHeight = 5 + player.getScore() / progressDenom;


            for (int i = 0; i < topBorders.size(); i++) {
                if (collision(topBorders.get(i), player))
                    player.setPlaying(false);
            }

            for (int i = 0; i < botBorders.size(); i++) {
                if (collision(botBorders.get(i), player))
                    player.setPlaying(false);
            }

            // create top border
            updateTopBorder();

            // create bot border
            updateBottomBorder();

            long elapsed = (System.nanoTime() - missleStartTime) / Utils.MILLION;
            if (elapsed > (2000 - player.getScore() / 4)) {

                Bitmap missile = BitmapFactory.decodeResource(getResources(), R.drawable.missile);

                // first missle goes dow middle
                if (missiles.size() == 0) {
                    missiles.add(new Missile(missile, WIDTH + 10, HEIGHT / 2, 45, 15, player.getScore(), 13));
                }
                else {
                    missiles.add(new Missile(missile, WIDTH + 10, (int) (rand.nextDouble() * HEIGHT - maxBorderHeight * 2 + maxBorderHeight),
                            45, 15, player.getScore(), 13));
                }
                missleStartTime = System.nanoTime();
            }

            for (int i = 0; i < missiles.size(); ++i) {

                Missile missile = missiles.get(i);
                missile.update();
                if (collision(missile, player)) {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }

                // remove missile if it is way off the screen.
                if (missile.getX() < -100) {
                    missiles.remove(i);
                    break;
                }

            }


            // add smoke puffs on timer
            elapsed = (System.nanoTime() - smokeStartTime) / Utils.MILLION;
            if (elapsed > 120) {
                smokepuffs.add(new Smokepuff(player.getX(), player.getY() + 10));
                smokeStartTime = System.nanoTime();
            }

            for (int i = 0; i < smokepuffs.size(); ++i) {
                Smokepuff puff = smokepuffs.get(i);
                puff.update();
                if (puff.getX() < -10) {
                    smokepuffs.remove(i);
                }
            }
        }
        else {
            newGameCreated = false;
            if (!newGameCreated) {
                newGame();
            }
        }
    }

    public boolean collision(GameObject a, GameObject b) {
        Rect aRect = a.getRectangle();
        Rect bRect = b.getRectangle();
        return aRect.intersect(bRect);
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
            for (Smokepuff puff : smokepuffs) {
                puff.draw(canvas);
            }

            for (Missile missile : missiles) {
                missile.draw(canvas);
            }

            for (TopBorder border : topBorders) {
                border.draw(canvas);
            }

            for (BotBorder border : botBorders) {
                border.draw(canvas);
            }
            canvas.restoreToCount(savedState);
        }
    }

    private void updateTopBorder() {
        // ever 50 points, insedrt randomly placed top blocoks to break the pattern

        if (player.getScore() % 50 == 0 && player.getScore() / 50 == nextRandomTop) {
            TopBorder lastBorder = topBorders.get(topBorders.size() - 1);
            TopBorder newBorder = new TopBorder(borderBitmap,
                    (lastBorder.getX() + 20),
                    0,
                    (int) ((rand.nextDouble() * maxBorderHeight) + 1));
            topBorders.add(newBorder);
            nextRandomTop++;
        }
        for (int i = 0; i < topBorders.size(); ++i) {
            TopBorder border = topBorders.get(i);
            border.update();
            if (border.getX() < -20) {
                topBorders.remove(i);

                // replace it by adding a new one

                // calculate topDown which determines the direction the border is moving
                TopBorder lastBorder = topBorders.get(topBorders.size() - 1);
                if (lastBorder.getHeight() >= maxBorderHeight) {
                    topDown = false;
                }
                if (lastBorder.getHeight() < minBorderHeight) {
                    topDown = true;
                }

                // new border added will have larger height
                TopBorder newBorder;
                if (topDown) {
                    newBorder = new TopBorder(borderBitmap,
                           lastBorder.getX() + 20,
                            0,
                           lastBorder.getHeight() + 1);
                }

                // new border added will have smaller height
                else {
                    newBorder = new TopBorder(borderBitmap,
                            lastBorder.getX() + 20,
                            0,
                            lastBorder.getHeight() - 1);
                }
                topBorders.add(newBorder);
            }
        }
    }

    private void updateBottomBorder() {

        // every 40 points bottom randomly breaks pattern
        if (player.getScore() % 40 == 0 && player.getScore() / 40 == nextRandomBot) {
            BotBorder lastBorder = botBorders.get(botBorders.size() - 1);
            BotBorder border = new BotBorder(borderBitmap,
                    lastBorder.getX() + 20,
                    (int) ((rand.nextDouble() * maxBorderHeight) + HEIGHT - maxBorderHeight));
            botBorders.add(border);
            nextRandomBot++;
        }

        //update bottom border
        for (int i = 0; i < botBorders.size(); ++i) {
            BotBorder border = botBorders.get(i);
            border.update();
            if (border.getX() < -20) {
                botBorders.remove(i);


                // replace it by adding a new one

                // calculate botDown which determines the direction the border is moving
                BotBorder lastBorder = botBorders.get(botBorders.size() - 1);
                if (lastBorder.getY() <= HEIGHT- maxBorderHeight) {
                    botDown = true;
                }
                if (lastBorder.getY() >= HEIGHT - minBorderHeight) {
                    botDown = false;
                }


                BotBorder newBorder;
                if (botDown) {
                    newBorder = new BotBorder(borderBitmap,
                            lastBorder.getX() + 20,
                            lastBorder.getY() + 1);
                }
                else {
                    newBorder = new BotBorder(borderBitmap,
                            lastBorder.getX() + 20,
                            lastBorder.getY() - 1);
                }
                botBorders.add(newBorder);
            }
        }
    }

    public void newGame() {
        botBorders.clear();
        topBorders.clear();
        missiles.clear();
        smokepuffs.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;

        player.resetDY();
        player.setY(HEIGHT / 2);
        player.resetDY();
        player.setUp(true);

        // create initial borders
        for (int i = 0; i * 20 < WIDTH + 40; ++i) {
            if (i == 0) {
                TopBorder border = new TopBorder(borderBitmap, i * 20, 0, 10);
                topBorders.add(border);
            }
            else {
                TopBorder border = new TopBorder(borderBitmap, i * 20, 0, topBorders.get(i - 1).getHeight() + 1);
                topBorders.add(border);
            }
        }

        for (int i = 0; i * 20 < WIDTH + 40; ++i) {
            if (i == 0) {
                BotBorder border = new BotBorder(borderBitmap, i * 20, HEIGHT - minBorderHeight);
                botBorders.add(border);
            }
            else {
                BotBorder border = new BotBorder(borderBitmap, i * 20, botBorders.get(i - 1).getY() - 1);
                botBorders.add(border);
            }
        }

        newGameCreated = true;
    }
}
