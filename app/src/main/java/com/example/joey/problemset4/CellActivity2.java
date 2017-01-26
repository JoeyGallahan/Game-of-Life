package com.example.joey.problemset4;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by Joey on 11/18/2016.
 */

public class CellActivity2 extends SingleFragmentActivity
{
    public static Intent newIntent(Context packageContext)
    {
        Intent i = new Intent(packageContext, CellActivity2.class);
        return i;
    }

    @Override
    protected Fragment createFragment()
    {
        setContentView(R.layout.activity_fragment_2);
        return new CellFragment();
    }
}
