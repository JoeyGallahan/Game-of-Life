package com.example.joey.problemset4;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by Joey on 11/8/2016.
 */

public class CellActivity extends SingleFragmentActivity
{

    public static Intent newIntent(Context packageContext)
    {
        Intent i = new Intent(packageContext, CellActivity.class);
        return i;
    }

    @Override
    protected Fragment createFragment()
    {
        setContentView(R.layout.activity_fragment);
        return new CellFragment();
    }
 
}
