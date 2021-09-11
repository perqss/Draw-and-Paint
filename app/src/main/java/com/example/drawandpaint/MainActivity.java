package com.example.drawandpaint;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.slider.Slider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Stack;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity
{
    private PaintView paintView;
    private ImageButton btnUndo, btnRedo, btnPalette, btnLoad, btnSave, btnBrush, btnClear, btnEraser, btnCircle, btnLine, btnFill, btnRectangle;
    private float previousSliderValue;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private int height, width;

    public MainActivity()
    {
    }

    private void init()
    {
        paintView = findViewById(R.id.paintView);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        btnPalette = findViewById(R.id.btnPalette);
        btnLoad = findViewById(R.id.btnLoad);
        btnSave = findViewById(R.id.btnSave);
        btnBrush = findViewById(R.id.btnBrush);
        btnClear = findViewById(R.id.btnClear);
        btnEraser = findViewById(R.id.btnEraser);
        btnCircle = findViewById(R.id.btnCircle);
        btnLine = findViewById(R.id.btnLine);
        btnFill = findViewById(R.id.btnFill);
        btnRectangle = findViewById(R.id.btnRectangle);
    }

    private void defaultValues()
    {
        paintView.setColor(getResources().getColor(R.color.black));
        paintView.setStrokeWidth(8);
        previousSliderValue = 8;
    }

    private void btnClicks()
    {
        btnUndo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                paintView.undo();
            }
        });

        btnRedo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                paintView.redo();
            }
        });

        btnPalette.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(MainActivity.this, paintView.getColor(), new AmbilWarnaDialog.OnAmbilWarnaListener()
                {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog)
                    {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color)
                    {
                        paintView.setColor(color);
                    }
                });
                colorPicker.show();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getResources().getString(R.string.clear_message));

                builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        paintView.clearCanvas();
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


        btnBrush.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // escape erasing mode
                paintView.setEraseMode(false);
                // escape drawing circle mode
                paintView.setCircleMode(false);
                paintView.setColorFillMode(false);
                paintView.setLineMode(false);
                paintView.setRectangleMode(false);
                LayoutInflater layoutInflater = MainActivity.this.getLayoutInflater();
                View layout = layoutInflater.inflate(R.layout.stroke_width_alert, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getResources().getString(R.string.stroke_width));
                builder.setView(layout);
                Slider sliderStrokeWidth = layout.findViewById(R.id.sliderStrokeWidth);
                sliderStrokeWidth.setValue(previousSliderValue);

                builder.setPositiveButton(getResources().getString(R.string.ok_button), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        paintView.setStrokeWidth(sliderStrokeWidth.getValue());
                        previousSliderValue = sliderStrokeWidth.getValue();
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.cancel_button), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        btnEraser.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                paintView.erase();
                paintView.setCircleMode(false);
                paintView.setLineMode(false);
                paintView.setRectangleMode(false);
                paintView.setColorFillMode(false);
                LayoutInflater layoutInflater = MainActivity.this.getLayoutInflater();
                View layout = layoutInflater.inflate(R.layout.eraser_width_alert, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getResources().getString(R.string.eraser_width));
                builder.setView(layout);
                Slider sliderEraserWidth = layout.findViewById(R.id.sliderEraserWidth);
                sliderEraserWidth.setValue(previousSliderValue);

                builder.setPositiveButton(getResources().getString(R.string.ok_button), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        paintView.setEraserWidth(sliderEraserWidth.getValue());
                        previousSliderValue = sliderEraserWidth.getValue();
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.cancel_button), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Bitmap bmp = paintView.save();

                if (Build.VERSION.SDK_INT >= 29)
                {

                    // opening a OutputStream to write into the file
                    OutputStream imageOutStream = null;

                    ContentValues cv = new ContentValues();

                    // name of the file
                    cv.put(MediaStore.Images.Media.DISPLAY_NAME, "drawing.png");

                    // type of the file
                    cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

                    // location of the file to be saved
                    cv.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                    // get the Uri of the file which is to be created in the storage
                    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                    try
                    {
                        // open the output stream with the above uri
                        imageOutStream = getContentResolver().openOutputStream(uri);

                        // this method writes the files in storage
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream);

                        // close the output stream after use
                        imageOutStream.close();
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.img_saved), Toast.LENGTH_SHORT).show();
                    } catch (Exception e)
                    {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.img_saving_error), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
                else
                {
                    File directory = new File(Environment.getExternalStorageDirectory().toString() + '/' + getString(R.string.app_name));

                    if (!directory.exists())
                    {
                        directory.mkdirs();
                    }
                    String fileName = System.currentTimeMillis() + ".png";
                    File file = new File(directory, fileName);
                    try
                    {
                        saveImageToStream(bmp, new FileOutputStream(file));
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                        MainActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.img_saved), Toast.LENGTH_SHORT).show();
                    }
                    catch (FileNotFoundException e)
                    {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.img_saving_error), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });

        btnLoad.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });

        btnCircle.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                paintView.setLineMode(false);
                paintView.setCircleMode(true);
                paintView.setEraseMode(false);
                paintView.setColorFillMode(false);
                paintView.setRectangleMode(false);
            }
        });

        btnLine.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                paintView.setCircleMode(false);
                paintView.setLineMode(true);
                paintView.setEraseMode(false);
                paintView.setColorFillMode(false);
                paintView.setRectangleMode(false);
            }
        });

        btnFill.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                paintView.setColorFillMode(true);
                paintView.setLineMode(false);
                paintView.setEraseMode(false);
                paintView.setCircleMode(false);
                paintView.setRectangleMode(false);

                final AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(MainActivity.this, paintView.getFillColor(), new AmbilWarnaDialog.OnAmbilWarnaListener()
                {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog)
                    {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color)
                    {
                        paintView.setFillColor(color);
                    }
                });
                colorPicker.show();
            }
        });

        btnRectangle.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                paintView.setColorFillMode(false);
                paintView.setLineMode(false);
                paintView.setEraseMode(false);
                paintView.setCircleMode(false);
                paintView.setRectangleMode(true);
            }
        });
    }

    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream)
    {
        if (outputStream != null)
        {
            try
            {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
// CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
// RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

// RECREATE THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        defaultValues();
        btnClicks();

       /* else
        {
            init();
            paintView.setCircleMode(true);
        }*/

        // change color of canvas and brush based on mode of the app
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode)
        {
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                paintView.setBackgroundColor(getResources().getColor(R.color.white));
                paintView.setColor(getResources().getColor(R.color.black));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                paintView.setBackgroundColor(getResources().getColor(R.color.black));
                paintView.setColor(getResources().getColor(R.color.white));
                break;
        }

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>()
                {
                    @Override
                    public void onActivityResult(ActivityResult result)
                    {
                        if (result.getResultCode() == Activity.RESULT_OK)
                        {
                            try
                            {
                                Intent data = result.getData();
                                assert data != null;
                                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                bitmap = getResizedBitmap(bitmap, height, width);
                                paintView.setBitmap(bitmap);
                            }
                            catch (FileNotFoundException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        ViewTreeObserver vto = paintView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                paintView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                width = paintView.getMeasuredWidth();
                height = paintView.getMeasuredHeight();
                paintView.init(height, width); // initialize main bitmap with measured width and height
            }
        });
    }
}