package com.example.joey.problemset4;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Joey on 11/8/2016.
 */

public class Cell
{
    private final static int LIFE_SPAN = 10; //max age

    private UUID mID;
    private boolean mIsAlive;
    private int mAge;
    private int mRow, mCol;
    private int mPosition;
    private int mImageID;

    Cell()
    {
        mID = UUID.randomUUID();
        mAge = 0;
        mIsAlive = false;
        mImageID = R.drawable.divider;
    }

    Cell(int row, int col)
    {
        mID = UUID.randomUUID();
        mAge = 0;
        mIsAlive = false;

        mRow = row;
        mCol = col;
        mImageID = R.drawable.divider;
    }

    //Accessors
    public boolean isAlive() { return mIsAlive; }
    public int getImageID() {return mImageID; }
    public int getPosition(){return mPosition;}

    public void setPosition(int position) { mPosition = position; }


    //If you're alive, die. If you're dead, come to life.
    public void toggleLife()
    {
        mIsAlive = !mIsAlive;
        mAge = 0;
        if (mIsAlive)
        {
            mImageID = R.drawable.cell_alive;
        }
        else
        {
            mImageID = R.drawable.divider;
        }
    }

    public void bringToLife()
    {
        mIsAlive = true;
        mImageID = R.drawable.cell_alive;
    }

    public void kill()
    {
        mIsAlive = false;
        mAge = 0;
        mImageID = R.drawable.divider;
    }

    //Grow older
    public void age()
    {
        if (mAge < LIFE_SPAN)
        {
            mAge++;
        }
        else
        {
            kill();
        }
    }
}