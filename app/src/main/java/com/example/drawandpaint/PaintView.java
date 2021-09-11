package com.example.drawandpaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class PaintView extends View
{
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private Stack<Image> mImages = new Stack<>();
    private Stack<Image> mUndoneImages = new Stack<>();
    private int mBrushColor;
    private float mStrokeWidth;
    private int mEraserColor;
    private float mEraserWidth;
    private int mOldBrushColor;
    private int mFillColor;
    private float mOldStrokeWidth;
    private Bitmap mBitmap;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private Context mContext;
    private Canvas mCanvas;
    private boolean mClear = false;
    private int mBackgroundColor;
    private boolean mEraseMode = false, mTxtMode = false, mCircleMode = false, mLineMode = false, mColorFillMode = false, mRectangleMode = false;
    private int mWidth, mHeight;

    public PaintView(Context context)
    {
        super(context);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        mPaint = new Paint();
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
        mFillColor = getResources().getColor(R.color.black);
    }

    public void init(int height, int width)
    {
        mHeight = height;
        mWidth = width;
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(mBackgroundColor);
        mBrushColor = getResources().getColor(R.color.black);
        mImages.push(new Image(mBitmap));
    }

    public void setColor(int color)
    {
        mBrushColor = color;
        mOldBrushColor = color;
    }

    public void setMostRecentBitmap()
    {
        if (!mImages.isEmpty())
        {
            Bitmap lastBitmap = mImages.peek().bitmap;
            setBitmap(lastBitmap);
        }
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

    public void setCircleMode(boolean mode)
    {
        mCircleMode = mode;
    }

    public void setColorFillMode(boolean mode)
    {
        mColorFillMode = mode;
    }

    public void setFillColor(int color)
    {
        mFillColor = color;
    }

    public int getFillColor()
    {
        return mFillColor;
    }

    public void setBitmap(Bitmap bitmap) // set bitmap passed as an argument as the main bitmap
    {
        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mBitmap = mutableBitmap;
        mCanvas = new Canvas(mutableBitmap);
        mPath.reset();
        mImages.push(new Image(mBitmap));
        invalidate();
    }

    public void setBackgroundColor(int color)
    {
        mBackgroundColor = color;
        invalidate();
    }

    public int getBackgroundColor()
    {
        return mBackgroundColor;
    }

    public void drawCurrentPathOnTheMostRecentBitmap()
    {
        if (!mImages.isEmpty())
        {
            mCanvas.drawBitmap(mImages.peek().bitmap, 0, 0, mBitmapPaint);
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();
        }
        else
            mCanvas.drawColor(mBackgroundColor);

        invalidate();
    }

    public void undo()
    {
        if (!mImages.isEmpty())
        {
            mPath.reset();
            if (mImages.size() > 1) // don't remove the first element, as it is the default bitmap with selected background color
                mUndoneImages.push(mImages.pop());

            Bitmap lastBitmap = mImages.peek().bitmap;
            mCanvas.drawBitmap(lastBitmap, 0, 0, mBitmapPaint);
            invalidate();
        }
    }

    public void redo()
    {
        if (!mUndoneImages.isEmpty())
        {
            Bitmap lastBitmap = mUndoneImages.peek().bitmap;
            mImages.push(mUndoneImages.pop());
            mPath.reset();
            mCanvas.drawBitmap(lastBitmap, 0, 0, mBitmapPaint);
            invalidate();
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

    public void write()
    {
        mTxtMode = true;
    }

    // Erasing is done by drawing in the same color as the background
    public void erase()
    {
        mEraseMode = true;
        mEraserColor = mBackgroundColor;
        mEraserWidth = 8;
    }

    public void setEraserWidth(float eraserWidth)
    {
        mEraserWidth = eraserWidth;
    }

    public void setEraseMode(boolean eraseMode)
    {
        mEraseMode = eraseMode;
    }

    public void setLineMode(boolean lineMode)
    {
        mLineMode = lineMode;
    }

    public void setRectangleMode(boolean rectangleMode)
    {
        mRectangleMode = rectangleMode;
    }

    public Bitmap floodFill(Bitmap  image, Point node, int targetColor, int replacementColor) // color filling algorithm, returns colored bitmap
    {
        int width = image.getWidth();
        int height = image.getHeight();
        int target = targetColor;
        int replacement = replacementColor;
        if (target != replacement)
        {
            Queue<Point> queue = new LinkedList<Point>();
            do
            {
                int x = node.x;
                int y = node.y;
                while (x > 0 && image.getPixel(x - 1, y) == target)
                {
                    x--;
                }
                boolean spanUp = false;
                boolean spanDown = false;
                while (x < width && image.getPixel(x, y) == target)
                {
                    image.setPixel(x, y, replacement);
                    if (!spanUp && y > 0 && image.getPixel(x, y - 1) == target)
                    {
                        queue.add(new Point(x, y - 1));
                        spanUp = true;
                    }
                    else if (spanUp && y > 0 && image.getPixel(x, y - 1) != target)
                    {
                        spanUp = false;
                    }
                    if (!spanDown && y < height - 1 && image.getPixel(x, y + 1) == target)
                    {
                        queue.add(new Point(x, y + 1));
                        spanDown = true;
                    }
                    else if (spanDown && y < height - 1 && image.getPixel(x, y + 1) != target)
                    {
                        spanDown = false;
                    }
                    x++;
                }
            } while ((node = queue.poll()) != null);
        }
        return image;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (mClear) // clear canvas and reset image stack
        {
            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mCanvas.drawColor(mBackgroundColor);
            mImages.clear();
            mUndoneImages.clear();
            mImages.push(new Image(mBitmap));
            mPath.reset();
            mClear = false;
            return;
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

        // draw current path
        mPaint.setColor(mBrushColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        mCanvas.drawPath(mPath, mPaint);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint); // draw the main bitmap on canvas
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void touchStart(float x, float y)
    {
        if (mColorFillMode)
        {
            // fill bitmap with color and set it as the main
            Bitmap filledBitmap = floodFill(mBitmap, new Point((int) x, (int) y), mBitmap.getPixel((int) x, (int) y), mFillColor);
            setBitmap(filledBitmap);
            return;
        }

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y)
    {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (mColorFillMode)
            return;

        if (mCircleMode)
        {
            float radius = (float) Math.sqrt(dx * dx + dy * dy); // calculate radius of the circle
            drawCurrentPathOnTheMostRecentBitmap();
            mPath.addCircle(mX, mY, radius, Path.Direction.CW);
        }

        else if (mLineMode)
        {
            drawCurrentPathOnTheMostRecentBitmap();
            mPath.moveTo(mX, mY);
            mPath.lineTo(x, y);
        }

        else if (mRectangleMode)
        {
            drawCurrentPathOnTheMostRecentBitmap();
            // draws a rectangle
            mPath.moveTo(mX, mY);
            mPath.lineTo(x, mY);
            mPath.moveTo(x, mY);
            mPath.lineTo(x, y);
            mPath.moveTo(mX, mY);
            mPath.lineTo(mX, y);
            mPath.moveTo(mX, y);
            mPath.lineTo(x, y);
            mPath.moveTo(x, y);
        }

        else
        {
            // draws path, which is not a circle, line or a rectangle
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
            {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }

    }

    private void touchUp(float x, float y)
    {
        if (mColorFillMode)
            return;

        mImages.push(new Image(mBitmap)); // add the current bitmap to the bitmap stack
        mPath = new Path();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();

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
                touchUp(x, y);
                invalidate();
                break;
        }
        return true;
    }
}
