package com.example.drawandpaint;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import yuku.ambilwarna.AmbilWarnaDialog;

public class SettingsFragment extends PreferenceFragment
{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // below line is used to add preference
        // fragment from our xml folder.
        addPreferencesFromResource(R.xml.settings);
        View layout = LayoutInflater.from(getActivity()).inflate(R.layout.activity_main, null);
        PaintView paintView = layout.findViewById(R.id.paintView);
        ConstraintLayout constraintLayout = layout.findViewById(R.id.constraintLayout);

        Preference dialogPreference = getPreferenceScreen().findPreference("dialog_preference");
        dialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                final AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(getActivity(), paintView.getColor(), new AmbilWarnaDialog.OnAmbilWarnaListener()
                {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog)
                    {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color)
                    {
                        paintView.setBackgroundColor(color);
                        constraintLayout.setBackgroundColor(color);
                    }
                });
                colorPicker.show();
                return true;
            }
        });
    }
}