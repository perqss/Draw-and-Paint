package com.example.drawandpaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class PaintView extends View
{
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath, pathToUndo, pathToRedo;
    private Paint mPaint;
    private ArrayList<Stroke> strokes = new ArrayList<>();
    private ArrayList<Stroke> undoneStrokes = new ArrayList<>();
    private int mBrushColor;
    private float mStrokeWidth;
    private int mEraserColor;
    private float mEraserWidth;
    private int mOldBrushColor;
    private float mOldStrokeWidth;
    private Bitmap mBitmap, mBitmapBackup, mLoadedBitmap;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private Context mContext;
    private Canvas mCanvas, mBitmapBackupCanvas;
    private boolean mClear = false;
    private int mBackgroundColor;
    private boolean mEraseMode = false, mTxtMode = false, mCircleMode = false, mLineMode = false, mColorFillMode = false;
    private boolean mLineFlag = false; // flag to indicate whether the move in touch move was the first move of drawing the line
    private boolean mCircleFlag = false; // same as above but with the circle
    private ImageView imgCircle;
    private int mWidth, mHeight;
    private EditText mEditText;
    private float radius;

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
        //mPaint.setAlpha(0xff);
    }

    public void setEraser(ImageView imageView)
    {
        imgCircle = imageView;
    }

    public void init(int height, int width)
    {
        mHeight = height;
        mWidth = width;
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mLoadedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mBrushColor = getResources().getColor(R.color.black);
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

    public void setCircleMode(boolean mode)
    {
        mCircleMode = mode;
    }

    public void setColorFillMode(boolean mode)
    {
        mColorFillMode = mode;
    }

    public void setBitmap(Bitmap bitmap)
    {
        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mBitmap = mutableBitmap;
        mLoadedBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mCanvas = new Canvas(mutableBitmap);
    }

    public void setBackgroundColor(int color)
    {
        mBackgroundColor = color;
        invalidate();
    }

    public void setEditText(EditText editText)
    {
        mEditText = editText;
    }

    public int getBackgroundColor()
    {
        return mBackgroundColor;
    }

    public void undo()
    {
        if (!strokes.isEmpty())
        {
            undoneStrokes.add(strokes.remove(strokes.size() - 1));
            mCanvas.drawColor(mBackgroundColor);
            mCanvas.drawBitmap(mLoadedBitmap, 0, 0, mBitmapPaint);
            mPath.reset();
            for (Stroke stroke : strokes)
            {
                mPaint.setColor(stroke.color);
                mPaint.setStrokeWidth(stroke.width);
                mCanvas.drawPath(stroke.path, mPaint);
            }
        }

        else
        {
            mCanvas.drawColor(mBackgroundColor);
        }

        invalidate();
    }

    public void redo()
    {
        if (!undoneStrokes.isEmpty())
        {
            strokes.add(undoneStrokes.remove(undoneStrokes.size() - 1));
            mCanvas.drawColor(mBackgroundColor);
            mCanvas.drawBitmap(mLoadedBitmap, 0, 0, mBitmapPaint);
            mPath.reset();
            for (Stroke stroke : strokes)
            {
                mPaint.setColor(stroke.color);
                mPaint.setStrokeWidth(stroke.width);
                mCanvas.drawPath(stroke.path, mPaint);
            }
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
        //imgCircle.setVisibility(VISIBLE);
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

    public Bitmap floodFill(Bitmap  image, Point node, int targetColor, int replacementColor)
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
        if (mClear) // clear canvas and reset arraylists
        {
            mCanvas.drawColor(mBackgroundColor);
            mPath.reset();
            undoneStrokes.clear();
            strokes.clear();
            mClear = false;
            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888); // reset bitmap
            mLoadedBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            return;
        }

        for (Stroke stroke : strokes)
        {
            mPaint.setColor(stroke.color);
            mPaint.setStrokeWidth(stroke.width);
            mCanvas.drawPath(stroke.path, mPaint);
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
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void touchStart(float x, float y)
    {
       /* if (mTxtMode)
        {
            mEditText.setVisibility(VISIBLE);
            mEditText.setX(x);
            mEditText.setY(y);
        }*/

        if (mColorFillMode)
        {
            Bitmap filledBitmap = floodFill(mBitmap, new Point((int) x, (int) y), mBitmap.getPixel((int) x, (int) y), Color.BLACK);
            setBitmap(filledBitmap);
            return;
        }

        mCircleFlag = mCircleMode;
        mLineFlag = mLineMode;
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
            radius = (float) Math.sqrt(dx * dx + dy * dy);
            // don't undo if this is the first move of drawing a new circle, because the previous circle will be removed
            if (!mCircleFlag)
                undo();
            else
                mCircleFlag = false;

            mPath.addCircle(mX, mY, radius, Path.Direction.CW);
            strokes.add(new Stroke(mBrushColor, mStrokeWidth, mPath));
        }

        else if (mLineMode)
        {
            if (!mLineFlag)
                undo();
            else
                mLineFlag = false;

            mPath.moveTo(mX, mY);
            mPath.lineTo(x, y);
            strokes.add(new Stroke(mBrushColor, mStrokeWidth, mPath));
        }

        else
        {
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
        /*if (mCircleMode)
            mPath.addCircle(mX, mY, radius, Path.Direction.CW);
        else if (mLineMode)
            mPath.lineTo(x, y);
        else*/
            /*mPath.lineTo(mX, mY);*/

        if (mColorFillMode)
            return;
        if (!mCircleMode && !mLineMode)
            strokes.add(new Stroke(mBrushColor, mStrokeWidth, mPath));

        mPath = new Path();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();

        // makes the circle follow cursor while erasing (need to improve)
       /* if (mEraseMode)
        {
            imgCircle.setX(x);
            imgCircle.setY(y);
        }*/

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
