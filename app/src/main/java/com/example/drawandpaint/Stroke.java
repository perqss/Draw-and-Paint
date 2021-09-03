package com.example.drawandpaint;

import android.graphics.Path;

public class Stroke
{
    public int color;
    public float width;
    public Path path;

    public Stroke(int color, float width, Path path)
    {
        this.color = color;
        this.width = width;
        this.path = path;
    }
}
