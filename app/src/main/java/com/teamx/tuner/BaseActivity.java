package com.teamx.tuner;

import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by Thinh on 16/05/2016.
 * Project: Tuner
 */
public class BaseActivity extends AppCompatActivity {

    Toolbar toolbar;

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the padding to match the Status Bar height
        if (toolbar != null) {
            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

}
