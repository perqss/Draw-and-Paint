package com.example.drawandpaint;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Image
{
    public Bitmap bitmap;
    public Canvas canvas;

    public Image(Bitmap bitmap)
    {
        this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        this.canvas = new Canvas(this.bitmap);
    }
}
