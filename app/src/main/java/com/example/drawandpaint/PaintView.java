package com.example.drawandpaint;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class PaintView extends View
{
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<Stroke> strokes = new ArrayList<>();
    private ArrayList<Stroke> undoneStrokes = new ArrayList<>();
    private int mBrushColor;
    private float mStrokeWidth;
    private int mEraserColor;
    private float mEraserWidth;
    private int mOldBrushColor;
    private float mOldStrokeWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private Context mContext;
    private boolean mClear = false;
    private int mBackgroundColor;
    private boolean mEraseMode = false;
    private ImageView imgCircle;

    public PaintView(Context context)
    {
        super(context);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        mPaint = new Paint();
        mCanvas = new Canvas();
        mPath = new Path();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(getResources().getColor(R.color.black));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(8);
        mContext = context;
        mBackgroundColor = getResources().getColor(R.color.white);
        //mPaint.setAlpha(0xff);
    }

    public void setEraser(ImageView imageView)
    {
        imgCircle = imageView;
    }

    public void init(int height, int width)
    {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBrushColor = getResources().getColor(R.color.black);
        mStrokeWidth = 20;
    }

    public void setColor(int color)
    {
        mBrushColor = color;
        mOldBrushColor = color;
    }

    public int getColor()
    {
        return mBrushColor;
    }

    public void setStrokeWidth(float width)
    {
        mStrokeWidth = width;
        mOldStrokeWidth = width;
    }

    public void undo()
    {
        if (!strokes.isEmpty())
        {
            undoneStrokes.add(strokes.remove(strokes.size() - 1));
            invalidate();
        }

        else
        {
            Toast.makeText(mContext, "Nothing to undo", Toast.LENGTH_SHORT).show();
        }
    }

    public void redo()
    {
        if (!undoneStrokes.isEmpty())
        {
            strokes.add(undoneStrokes.remove(undoneStrokes.size() - 1));
            invalidate();
        }

        else
        {
            Toast.makeText(mContext, "Nothing to redo", Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap save()
    {
        return mBitmap;
    }

    public void clearCanvas()
    {
        mClear = true;
        invalidate();
    }

    // Erasing is done by drawing in the same color as the background
    public void erase()
    {
        mEraseMode = true;
        mEraserColor = mBackgroundColor;
        mEraserWidth = 8;
        imgCircle.setVisibility(VISIBLE);
    }

    public void setEraserWidth(float eraserWidth)
    {
        mEraserWidth = eraserWidth;
    }

    public void setEraseMode(boolean eraseMode)
    {
        mEraseMode = eraseMode;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        //canvas.save();
        if (mClear)
        {
            canvas.drawColor(mBackgroundColor);
            mPath.reset();
            undoneStrokes.clear();
            strokes.clear();
            mClear = false;
            return;
        }

        for (Stroke stroke : strokes)
        {
            mPaint.setColor(stroke.colour);
            mPaint.setStrokeWidth(stroke.width);
            canvas.drawPath(stroke.path, mPaint);
        }

        if (mEraseMode) // if we want to erase, set color of the brush to background color and width to the value chosen with slider
        {
            mBrushColor = mEraserColor;
            mStrokeWidth = mEraserWidth;
        }

        else // if we exit erase mode go back to old width and color
        {
            mBrushColor = mOldBrushColor;
            mStrokeWidth = mOldStrokeWidth;
        }

        mPaint.setColor(mBrushColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawPath(mPath, mPaint);

        //canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        //canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void touchStart(float x, float y)
    {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y)
    {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
        {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp()
    {
        mPath.lineTo(mX, mY);
        //mCanvas.drawPath(mPath, mPaint);
        strokes.add(new Stroke(mBrushColor, mStrokeWidth, mPath));
        mPath = new Path();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();

        if (mEraseMode)
        {
            imgCircle.setX(x);
            imgCircle.setY(y);
        }

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }

        return true;
    }
}
