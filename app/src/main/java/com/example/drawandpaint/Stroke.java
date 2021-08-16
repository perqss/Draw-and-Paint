package com.example.drawandpaint;

import android.graphics.Path;

public class Stroke
{
    public int colour;
    public float width;
    public Path path;

    public Stroke(int colour, float width, Path path)
    {
        this.colour = colour;
        this.width = width;
        this.path = path;
    }
}
