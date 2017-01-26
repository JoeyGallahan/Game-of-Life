package com.example.joey.problemset4;

/**
 * Created by Joey on 11/8/2016.
 */
import android.content.Context;
import android.icu.text.DecimalFormat;
import android.icu.text.TimeZoneFormat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class Grid
{
    private static Grid sGrid;
    private boolean mActivitySwitched;
    private int mMaxRows, mMaxCols;

    private Cell[][] mGrid;
    private boolean[][] mOriginalGrid;
    private int[][] mLivingNeighborsCount;
    private long mTiming;
    private boolean mHasRunBefore;

    public static Grid get()
    {
        if (sGrid == null)
        {
            sGrid = new Grid();
        }
        return sGrid;
    }

    private Grid()
    {
        mMaxRows = 20;
        mMaxCols = 20;

        mGrid = new Cell[mMaxRows][mMaxCols];
        mOriginalGrid = new boolean[mMaxRows][mMaxCols];

        for (int r = 0; r < mMaxRows; r++)
        {
            for (int c = 0; c < mMaxCols; c++) //c++ lol
            {
                mGrid[r][c] = new Cell(r,c);
            }
        }
        mTiming = 500;
        mHasRunBefore = false;
    }

    //Accessors
    public int getMaxRows() { return mMaxRows; }
    public Cell[][] getGrid() { return mGrid; }
    public boolean hasRunBefore() { return mHasRunBefore; }
    public boolean getActivitySwitched() { return mActivitySwitched; }
    public long getTiming()
    {
        return mTiming;
    }

    //Mutators
    public void setHasRunBefore(boolean maybe) { mHasRunBefore = maybe; }
    public void setActivitySwitched(boolean maybe) { mActivitySwitched = maybe; }
    public void setOriginalGrid()
    {
        for (int r = 0; r < mMaxRows; r++)
        {
            for (int c = 0; c < mMaxCols; c++)
            {
                if (mGrid[r][c].isAlive())
                {
                    mOriginalGrid[r][c] = true;
                }
                else
                {
                    mOriginalGrid[r][c] = false;
                }
            }
        }
    }
    //Set the speed of the animation
    public void setTiming(String time)
    {
        //Cast the string to a float in case the user types in a decimal number
        float temp = Float.parseFloat(time);
        temp *= 1000.0f;

        String longTime = Float.toString(temp); //cast to string
        longTime = longTime.substring(0, longTime.length() - 2); //remove the .0

        mTiming = Long.parseLong(longTime);
    }

    //Save the grid to file
    public void saveGrid(String file, Context context)
    {
        String fileName = file + ".txt";
        OutputStreamWriter outputStream;
        try
        {
            outputStream = new OutputStreamWriter(context.openFileOutput(fileName, context.MODE_APPEND));
            for (int r = 0; r < mMaxRows; r++)
            {
                for (int c = 0; c < mMaxCols; c++)
                {
                    if (mGrid[r][c].isAlive())
                    {
                        outputStream.append("1");
                    }
                    else
                    {
                        outputStream.append("0");
                    }
                }
            }
            setOriginalGrid();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    //Load the grid to the original grid from a text file
    public void loadGrid(String file, Context context)
    {
        String fileName = file + ".txt";
        String output = "";
        FileInputStream inputStream;
        try
        {
            inputStream = context.openFileInput(fileName);
            if (inputStream != null)
            {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();
                while ( (receiveString = bufferedReader.readLine()) != null )
                {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                output = stringBuilder.toString();
            }
            for(int i = 0; i < output.length(); i++)
            {
                //Set the proper row and column from the recyclerview position
                int row = (i+1) / mMaxRows;
                int col = (i+1) % mMaxCols;

                //Set everything to the original grid so we can just use resetGrid()
                if (output.charAt(i) == '1')
                {
                    mOriginalGrid[row][col] = true;
                }
                else
                {
                    mOriginalGrid[row][col] = false;
                }
            }
            resetGrid();
        }
        catch (FileNotFoundException e)
        {
            Log.e("Exception", "File not found: " + e.toString());
            setOriginalGrid();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File read failed: " + e.toString());
            setOriginalGrid();
        }
    }

    //Clear the grid
    public void clearGrid()
    {
        for (int i = 0; i < mMaxRows; i++)
        {
            for (int j = 0; j < mMaxCols; j++)
            {
                //Just kill everything
                mGrid[i][j].kill();
            }
        }
    }

    //Reset the grid according to what it was when you first hit start
    public void resetGrid()
    {
        for (int r = 0; r < mMaxRows; r++)
        {
            for (int c = 0; c < mMaxCols; c++)
            {
                if (mOriginalGrid[r][c])
                {
                    mGrid[r][c].bringToLife();
                }
                else
                {
                    mGrid[r][c].kill();
                }
            }
        }
    }

    //Kill lonely or overcrowded cells and bring new cells to life
    public void harshReality()
    {
        //Modified from David Kopec and Steven Wu's updateColony method in ColonyPanel.java
        updateNumberOfLivingNeighbors();
        // Changes the status of the cell based on the number of living
        // neighbors it has.
        for (int i = 0; i < mMaxRows; i++)
        {
            for (int j = 0; j < mMaxCols; j++)
            {
                // If the cell has 4 or more living neighbors, it dies
                // by overcrowding.
                if (mLivingNeighborsCount[i][j] >= 4)
                {
                    mGrid[i][j].kill();
                }

                // A cell dies by exposure if it has 0 or 1 living neighbors.
                if (mLivingNeighborsCount[i][j] < 2)
                {
                    mGrid[i][j].kill();
                }

                // A cell is born if it has 3 living neighbors.
                if (mLivingNeighborsCount[i][j] == 3)
                {
                    mGrid[i][j].bringToLife();
                }
                mGrid[i][j].age();
            }
        }
    }

    //Updates the number of living neighbors for all mGrid
    public void updateNumberOfLivingNeighbors()
    {
        //Modified from David Kopec and Steven Wu's updateColony method in ColonyPanel.java
        mLivingNeighborsCount = new int[mMaxRows][mMaxCols];

        for (int r = 0; r < mMaxRows; r++)
        {
            for (int c = 0; c < mMaxCols; c++)
            {
                // Variables to save positions left and right of row and column
                int leftOfRow = r + mMaxRows - 1;
                int rightOfRow = r + 1;
                int leftOfColumn = c + mMaxCols - 1;
                int rightOfColumn = c + 1;

                // Checks to see if the mGrid are alive or dead. If they are alive
                // it increments the count for living neighbors.
                if (mGrid[r][c].isAlive())
                {
                    mLivingNeighborsCount[leftOfRow % mMaxRows][leftOfColumn % mMaxCols]++;
                    mLivingNeighborsCount[leftOfRow % mMaxRows][c % mMaxCols]++;
                    mLivingNeighborsCount[(r + mMaxRows - 1) % mMaxRows][rightOfColumn % mMaxCols]++;
                    mLivingNeighborsCount[r % mMaxRows][leftOfColumn % mMaxCols]++;
                    mLivingNeighborsCount[r % mMaxRows][rightOfColumn % mMaxCols]++;
                    mLivingNeighborsCount[rightOfRow % mMaxRows][leftOfColumn % mMaxCols]++;
                    mLivingNeighborsCount[rightOfRow % mMaxRows][c % mMaxCols]++;
                    mLivingNeighborsCount[rightOfRow % mMaxRows][rightOfColumn % mMaxCols]++;
                }
            }
        }
    }
}
