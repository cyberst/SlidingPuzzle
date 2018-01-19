
package com.alirizasalihoglu.slidingpuzzle;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.alirizasalihoglu.slidingpuzzle.Model.PuzzleTile;

import java.util.ArrayList;

public class PuzzleTileAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<PuzzleTile> Tiles;
    private int tileWidth, tileHeight;
    private int blankTileIndex;

    public PuzzleTileAdapter(Context c, ArrayList<PuzzleTile> tiles, int blankIndex){
        mContext = c;
        Tiles = tiles;
        blankTileIndex = blankIndex;
        tileWidth = Tiles.get(0).bitmap.getWidth();
        tileHeight = Tiles.get(0).bitmap.getHeight();
    }

    @Override
    public int getCount() {
        return Tiles.size();
    }

    @Override
    public Object getItem(int position) {
        return Tiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ImageView image;
        if(convertView == null) {
            image = new ImageView(mContext);


            image.setLayoutParams(new GridView.LayoutParams(tileWidth , tileHeight));
            image.setPadding(5, 5, 5, 5);
            image.setTag(Integer.toString(position));
            image.setFocusable(false);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((GridView) parent).performItemClick(v, position, 0);
                }
            });
        }
        else {
            image = (ImageView) convertView;
        }

        image.setImageBitmap(Tiles.get(position).bitmap);
        if(blankTileIndex == (position)){
            image.setVisibility(View.INVISIBLE);
        }
        else if(image.getVisibility() != View.VISIBLE){
            image.setVisibility(View.VISIBLE);
        }

        return image;
    }

    public void notifyTilesChanged(int blankIndex){
        blankTileIndex = blankIndex;
        notifyDataSetChanged();
    }
}
