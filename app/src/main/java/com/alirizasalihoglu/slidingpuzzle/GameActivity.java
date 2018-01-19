package com.alirizasalihoglu.slidingpuzzle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.alirizasalihoglu.slidingpuzzle.Model.PuzzleTile;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

import static com.alirizasalihoglu.slidingpuzzle.R.id.grid;

public class GameActivity extends AppCompatActivity {
    public ArrayList<PuzzleTile> Tiles;
    GridView gridView;
    int blankTileIndex, size, moveCount, backCount, time;
    ArrayDeque<Integer> blankTilesStack;
    PuzzleTileAdapter gridAdapter, hintAdapter;
    TextView moveText;
    final String moveTextPrefix = "Move: ";
    SharedPreferences appSettings;
    Bitmap bitmap = null;
    Handler timeHandler;
    Runnable countTime;
    TextView timeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        String imageTag;
        moveCount = 0;
        backCount = 0;
        moveText = (TextView) findViewById(R.id.moveText) ;
        moveText.setText(moveTextPrefix + String.valueOf(moveCount));
        Intent gameIntent = getIntent();
        imageTag = gameIntent.getStringExtra("Image Tag");
        blankTilesStack = new ArrayDeque<Integer>();
        timeText = (TextView) findViewById(R.id.timeText);
        if(imageTag.equals("Custom Image")) {

            Uri imageUri = gameIntent.getData();

            try {
                bitmap =  MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            int resID = getResources().getIdentifier(imageTag, "drawable", getPackageName());
            BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(getApplicationContext(), resID);
            bitmap = drawable.getBitmap();
        }

        loadSettings();
        blankTileIndex = size * size - 1;
        blankTilesStack.push(blankTileIndex);
        gridView = (GridView)findViewById(grid);

        ViewTreeObserver vto = gridView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gridView.getViewTreeObserver().removeOnGlobalLayoutListener (this);
                int width  = gridView.getMeasuredWidth();
                int height = gridView.getMeasuredHeight();
                setGrid(bitmap);

            }
        });



    }



    private void loadSettings() {
        appSettings = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        size = appSettings.getInt("Size", 3);
    }

    private void setGrid(Bitmap bitmap) {

        splitBitmapToTiles(bitmap);
        setHintButton();
        shuffleSolvableGrid();

        gridAdapter = new PuzzleTileAdapter(this, Tiles, blankTileIndex);
        gridView.setAdapter(gridAdapter);
        gridView.setNumColumns(size);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int tileIndex = Integer.parseInt((String) view.getTag());
                if((tileIndex + 1 == blankTileIndex && (tileIndex % size != size - 1))|| (tileIndex - 1 == blankTileIndex && (tileIndex % size != 0))
                        || tileIndex + size == blankTileIndex || tileIndex - size == blankTileIndex){

                    if(time == 0){
                        startTimer();
                    }
                    Collections.swap(Tiles, blankTileIndex, tileIndex);
                    blankTileIndex = tileIndex;
                    moveCount++;
                    moveText.setText(moveTextPrefix + String.valueOf(moveCount));
                    blankTilesStack.push(blankTileIndex);
                    ((PuzzleTileAdapter) gridAdapter).notifyTilesChanged(blankTileIndex);
                    checkWin();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Tile " + (tileIndex + 1) + " cannot move!", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
    @Override
    protected void onPause(){
        super.onPause();
        if(timeHandler != null){
            timeHandler.removeCallbacks(countTime);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(time != 0){
            startTimer();
        }
    }
    private void startTimer() {

        timeHandler = new Handler();
        countTime = new Runnable() {
            @Override
            public void run() {
                time++;
                timeHandler.postDelayed(this,1000);
                if(time < 3600){
                    updateUI();
                }
            }
        };
        timeHandler.post(countTime);
    }

    private void updateUI() {
        int minute = time / 60;
        int second = time % 60;
        timeText.setText(String.format("%02d", minute) + ":" + String.format("%02d", second));
    }

    private void setHintButton() {
        final Button hintButton = (Button) findViewById(R.id.hintButton);
        ArrayList<PuzzleTile> hintTiles = new ArrayList<PuzzleTile>(Tiles);

        hintAdapter = new PuzzleTileAdapter(getApplicationContext(), hintTiles, blankTileIndex);

        hintButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {

                    gridView.setAdapter(hintAdapter);
                } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    hintButton.performClick();
                    gridView.setAdapter(gridAdapter);

                }
                return true;
            }


        });
    }

    private void shuffleSolvableGrid() {
        boolean solvable = false;

        while(!solvable){
            Collections.shuffle(Tiles);
            for(int i = 0; i < Tiles.size(); i++) {

                //Find the last tile and make it blank

                if(Tiles.get(i).getOrder() == (size * size -1)){

                    Collections.swap(Tiles, i, size * size - 1);
                    break;
                }
            }
            if (checkIsSolvable()){
                solvable = true;
            }
            else {
                solvable = false;
            }
        }

    }

    private boolean checkIsSolvable() {
        int inversions = 0;

        for(int i = 0; i < Tiles.size(); i++) {

            for(int j = i + 1; j < Tiles.size(); j++){

                int tileOrder = Tiles.get(i).getOrder();
                if(tileOrder > Tiles.get(j).getOrder()){

                    inversions++;
                }
            }
        }

        if(((size % 2 == 1) && (inversions % 2 == 0)) || ((size % 2 == 0) && ((size - (blankTileIndex + 1)/size + 1) % 2 == 1 ) == (inversions % 2 == 0))){
            return true;
        }
        else{
            return false;
        }
    }


    private void checkWin() {

        for(int i = 0; i < Tiles.size(); i++){
            if(i != Tiles.get(i).getOrder()){
                return;
            }
        }
        Intent winIntent = new Intent(getApplicationContext(), WinActivity.class);
        winIntent.putExtra("Move Count",moveCount);
        winIntent.putExtra("Back Count",backCount);
        winIntent.putExtra("Size", size);
        winIntent.putExtra("Time", time);
        startActivity(winIntent);

    }

    private void splitBitmapToTiles(Bitmap bitmap) {
        Tiles = new ArrayList<PuzzleTile>(size * size);


        int gridPixelSize = gridView.getWidth();
        int rowNumber = size, colNumber = size;
        float tileHeight = (float)gridPixelSize/rowNumber;
        float tileWidth = (float)gridPixelSize/colNumber;
        int order = 0;

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, gridPixelSize, gridPixelSize, true);

        for (float y = 0; y < scaledBitmap.getHeight(); y = y + tileHeight) {
            int height = (int)y;
            for(float x = 0; x < scaledBitmap.getWidth(); x = x + tileWidth) {
                int width = (int)x;
                Tiles.add(new PuzzleTile(Bitmap.createBitmap(scaledBitmap,width, height, (int) tileWidth, (int)tileHeight), order));
                order++;
            }
        }
    }

    public void clickBack(View view) {
        if(moveCount == 0){
            Toast.makeText(getApplicationContext(), "Can't go back!", Toast.LENGTH_SHORT).show();
            return;
        }
        blankTilesStack.pop();
        int newBlankTileIndex = blankTilesStack.getFirst();
        Collections.swap(Tiles,blankTileIndex,newBlankTileIndex);
        blankTileIndex = newBlankTileIndex;
        backCount++;
        moveCount--;
        moveText.setText(moveTextPrefix + String.valueOf(moveCount));
        ((PuzzleTileAdapter)gridAdapter).notifyTilesChanged(blankTileIndex);
    }

    public void clickReset(View view) {
        blankTileIndex = size * size - 1;
        blankTilesStack.clear();
        blankTilesStack.push(blankTileIndex);
        backCount = 0;
        time = 0;

        if(timeHandler != null){
            timeHandler.removeCallbacks(countTime);
        }
        updateUI();

        if(moveCount != 0){
            moveCount = 0;
            moveText.setText(moveTextPrefix + String.valueOf(moveCount));
        }

        shuffleSolvableGrid();
        ((PuzzleTileAdapter)gridAdapter).notifyTilesChanged(blankTileIndex);

    }
}

