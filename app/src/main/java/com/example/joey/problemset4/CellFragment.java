package com.example.joey.problemset4;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import android.os.Handler;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.UUID;

import static android.widget.GridLayout.VERTICAL;

/**
 * Created by Joey on 11/8/2016.
 */

public class CellFragment extends android.support.v4.app.Fragment
{
    //Everything for the RecyclerView fragment
    private RecyclerView mCellRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private CellAdapter mAdapter;

    //The buttons in the settings menu
    private Button mStartButton, mClearButton, mResetButton;

    //Animating the cell & playing the game
    private SeekBar mRedBar, mGreenBar, mBlueBar;
    private EditText mUpdateTiming;
    private int mRedVal, mGreenVal, mBlueVal; // Values of the red, green and blue slider bars.
    private int mTickNum, mTickMax;
    private boolean mRunning;

    //Saving & loading
    private LinearLayout mSaveLayout, mLoadLayout;
    private Button mCancelSaveButton, mCancelLoadButton, mSaveButton, mLoadButton;  //The buttons in the save & load menus
    private EditText mSaveText, mLoadText;

    private Cell[][] mCells;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.grid_fragment, container, false);

        //Set up the recycler view and its manager
        mCellRecyclerView = (RecyclerView) view.findViewById(R.id.cell_recycler_view);
        mGridLayoutManager = new GridLayoutManager(getActivity(), Grid.get().getMaxRows(), VERTICAL, false);

        mCellRecyclerView.setLayoutManager(mGridLayoutManager); //Apply the manager to the recyclerview

        //There were so many views I put them all together in a method to save space
        initAllViews();

        //Animation stuff
        mTickNum = 1;
        mTickMax = 3;

        //There were so many listeners I put them all together in a method to save space
        setAllOnClickListeners();

        mRunning = false; //Start the app with the game paused

        updateUI(); //Set up the adapter

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        final Handler mHandler = new Handler(); //This runs the thread
        final Thread mHandlerTask; //This does the actual game part
        mHandlerTask = new Thread()
        {
            @Override
            public void run()
            {
                if (mRunning)
                {
                    Grid.get().harshReality();
                    update();
                    if (mTickNum < mTickMax)
                    {
                        mTickNum++;
                    }
                    else
                    {
                        mTickNum = 1;
                    }
                }
                mHandler.postDelayed(this, Grid.get().getTiming());
            }
        };
        getActivity().runOnUiThread(mHandlerTask);

        updateUI();
    }

    //Basically just sets up the adapter
    private void updateUI()
    {
       Cell[][] cells = Grid.get().getGrid();
       mAdapter = new CellAdapter(cells);
       mCellRecyclerView.setAdapter(mAdapter);
    }

    //Makes the changes visible on the screen by updating the cell image
    //Also animates the cells
    private void update()
    {
        for (int r = 0; r < Grid.get().getMaxRows(); r++)
        {
            for (int c = 0; c < Grid.get().getMaxRows(); c++)
            {
                RecyclerView.ViewHolder vh = mCellRecyclerView.findViewHolderForAdapterPosition(mCells[r][c].getPosition());
                View v = vh.itemView;
                v.setBackgroundResource(mCells[r][c].getImageID());

                if (mCells[r][c].isAlive())
                {
                    updateCellColor(v);
                }
            }
        }
    }

    //When someone moves a color slider
    private SeekBar.OnSeekBarChangeListener changeColor = new
            SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            if (seekBar == mRedBar)
            {
                mRedVal = progress;
            }
            else if (seekBar == mGreenBar)
            {
                mGreenVal = progress;
            }
            else
            {
                mBlueVal = progress;
            }
            update();
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar){}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    //Changes the colors of the drawables
    private void updateCellColor(View v)
    {
        //With help from http://stackoverflow.com/questions/10114420/applying-colorfilter-to-imageview-with-shapeddrawable
        //          and  http://android-er.blogspot.com/2009/08/change-background-color-by-seekbar.html
        v.getBackground().setColorFilter(new PorterDuffColorFilter(0xff000000 +
                                                                    mRedVal * 0x10000 +
                                                                    mGreenVal * 0x100 +
                                                                    mBlueVal, PorterDuff.Mode.SRC_IN));
        v.getBackground().setAlpha(255/mTickNum);
    }

    //RecyclerView ViewHolder
    private class CellHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
    {
        private Cell mCell;

        public CellHolder(View itemView)
        {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        public void bindCell(Cell cell)
        {
            mCell = cell;
        }

        @Override
        public void onClick(View v)
        {
            if (!mRunning)
            {
                mCell.toggleLife();
                v.setBackgroundResource(mCell.getImageID());

                if (mCell.isAlive())
                {
                    updateCellColor(v);
                }
            }
        }
    }

    //RecyclerView ViewAdapter
    private class CellAdapter extends RecyclerView.Adapter<CellHolder>
    {
        public CellAdapter(Cell[][] cells)
        {
            mCells = cells;
        }

        @Override
        public CellHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.cell, parent, false);
            return new CellHolder(view);
        }

        @Override
        public void onBindViewHolder(CellHolder holder, int position)
        {
            //Set the proper row and column from the recyclerview position
            int row = position / Grid.get().getMaxRows();
            int col = position % Grid.get().getMaxRows();

            Cell cell = mCells[row][col];
            cell.setPosition(position); //store the recyclerview position of the cell so we can use it later

            holder.bindCell(cell);
        }

        @Override
        public int getItemCount()
        {
            return mCells.length * mCells[0].length;
        }
    }

    //Initialize all the views in the fragment
    private void initAllViews()
    {
        //All the beautiful buttons
        mStartButton = (Button) getActivity().findViewById(R.id.start_game);
        mClearButton = (Button) getActivity().findViewById(R.id.clear_grid);
        mResetButton = (Button) getActivity().findViewById(R.id.reset_grid);

        //The edittext box for the animation timing
        mUpdateTiming = (EditText) getActivity().findViewById(R.id.animation_timing);

        //The sliders for changing the cell color
        mRedBar = (SeekBar) getActivity().findViewById(R.id.red_bar);
        mGreenBar = (SeekBar) getActivity().findViewById(R.id.green_bar);
        mBlueBar = (SeekBar) getActivity().findViewById(R.id.blue_bar);

        //Set all of the bar listeners to the same method to save space
        mRedBar.setOnSeekBarChangeListener(changeColor);
        mGreenBar.setOnSeekBarChangeListener(changeColor);
        mBlueBar.setOnSeekBarChangeListener(changeColor);

        //The hidden layouts and buttons for saving & loading
        mSaveLayout = (LinearLayout) getActivity().findViewById(R.id.save_grid_layout);
        mLoadLayout = (LinearLayout) getActivity().findViewById(R.id.load_grid_layout);
        mSaveButton = (Button) getActivity().findViewById(R.id.save_button);
        mLoadButton = (Button) getActivity().findViewById(R.id.load_button);
        mCancelSaveButton = (Button) getActivity().findViewById(R.id.cancel_save);
        mCancelLoadButton = (Button) getActivity().findViewById(R.id.cancel_load);
        mSaveText = (EditText) getActivity().findViewById(R.id.save_grid_edit_text);
        mLoadText = (EditText) getActivity().findViewById(R.id.load_grid_edit_text);
    }

    //Set up OnClickListers for all the views that need them
    private void setAllOnClickListeners()
    {
        //Clicking the start button
        mStartButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Make sure the game is paused or simply hasn't started yet
                if (!mRunning)
                {
                    if (!Grid.get().hasRunBefore())
                    {
                        Grid.get().setHasRunBefore(true);
                        Grid.get().setOriginalGrid();
                        update();
                    }
                    mRunning = true;
                }
                else
                {
                    mRunning = false;
                }
            }
        });

        //Clicking the clear button
        mClearButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Make sure the game is paused or hasn't started yet
                if (!mRunning)
                {
                    Grid.get().clearGrid();
                    update();
                }
            }
        });

        //Clicking the reset button
        mResetButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Make sure the game is paused or hasn't started yet
                //If the grid hasn't been changed there's really no reason to reset it
                if (!mRunning)
                {
                    if (Grid.get().hasRunBefore())
                    {
                        Grid.get().resetGrid();
                        mCells = Grid.get().getGrid();
                        update();
                    }
                }
            }
        });

        //Typing something into the update edittext box
        mUpdateTiming.addTextChangedListener(new TextWatcher()
        {
            //Don't need the first two, but they're necessary because it's an abstract class
            public void onTextChanged(CharSequence c, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {}

            public void afterTextChanged(Editable c)
            {
                //Make sure the game is paused or hasn't started yet
                if (!mRunning)
                {
                    //We don't want an empty value, a speed of 0, or 0.nothing
                    if (!c.toString().equals("") && !c.toString().equals("0") && !c.toString().equals("0."))
                    {
                        Grid.get().setTiming(c.toString());
                    }
                }
            }
        });

        //When you click to save a grid
        mSaveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Hide the layout and save the grid to a file entered by the user
                mSaveLayout.setVisibility(View.GONE);
                Grid.get().saveGrid(mSaveText.getText().toString(), getContext());
            }
        });

        //When you click to load a grid
        mLoadButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Hide the layout and load the grid from the user entered grid name
                mLoadLayout.setVisibility(View.GONE);
                Grid.get().loadGrid(mLoadText.getText().toString(), getContext());
                mCells = Grid.get().getGrid();
                update();
            }
        });

        //When you click the cancel button in the save menu
        mCancelSaveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mSaveLayout.setVisibility(View.GONE);
            }
        });

        //When you click the cancel button in the load menu
        mCancelLoadButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mLoadLayout.setVisibility(View.GONE);
            }
        });
    }

    //Set up the options menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.options, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch(menuItem.getItemId())
        {
            case R.id.quit: getActivity().finishAffinity();
                return true;
            case R.id.save_grid: mSaveLayout.setVisibility(View.VISIBLE);
                return true;
            case R.id.load_grid: mLoadLayout.setVisibility(View.VISIBLE);
                return true;
            case R.id.switch_activity:
                if (!Grid.get().getActivitySwitched())
                {
                    Grid.get().setActivitySwitched(true);
                    startActivity(new Intent(getActivity(), CellActivity2.class));
                }
                else
                {
                    Grid.get().setActivitySwitched(false);
                    startActivity(new Intent(getActivity(), CellActivity.class));
                }
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
