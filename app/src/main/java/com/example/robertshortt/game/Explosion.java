package com.example.robertshortt.game;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

/**
 * Created by Robert Shortt on 3/7/2017.
 */

public class Explosion {
    private int row;
    private int x, y, width, height;
    private Animation animation = new Animation();
    private Bitmap spritesheet;

    public Explosion(Bitmap res, int x, int y, int w, int h, int numFrames) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        spritesheet = res;

        Bitmap[] image = new Bitmap[numFrames];
        for (int i = 0; i < image.length; ++i) {
            if (i % 5 == 0 && i > 0)
                ++row;
            Bitmap frame = Bitmap.createBitmap(res, (i % 5) * width, row * height, width, height);
            image[i] = frame;
        }
        animation.setFrames(image);
        animation.setDelay(10);
    }

    public void draw(Canvas canvas) {
        if (!animation.isPlayedOnce()) {
            Bitmap frame = animation.getImage();
            canvas.drawBitmap(frame, x, y, null);
        }
    }

    public void update() {
        if (!animation.isPlayedOnce())
            animation.update();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
