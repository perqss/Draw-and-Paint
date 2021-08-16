package com.example.drawandpaint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity
{
    private PaintView paintView;
    private ImageButton btnUndo, btnRedo, btnPalette, btnLoad, btnSave, btnBrush, btnClear, btnChangeStrokeWidth, btnEraser;
    private RelativeLayout strokeWidthLayout, eraserWidthLayout;
    private float previousSliderValue;
    private ImageView imgCircle;

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
        strokeWidthLayout = findViewById(R.id.strokeWidthLayout);
        eraserWidthLayout = findViewById(R.id.eraserWidthLayout);
        imgCircle = findViewById(R.id.imgCircle);
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
                paintView.clearCanvas();
            }
        });


        btnBrush.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // escape erasing mode
                paintView.setEraseMode(false);
                imgCircle.setVisibility(View.GONE);
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        defaultValues();
        paintView.setEraser(imgCircle);
        btnClicks();
    }
}