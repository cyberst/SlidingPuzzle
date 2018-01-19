package com.alirizasalihoglu.slidingpuzzle.Model;

        import android.graphics.Bitmap;


public class PuzzleTile {
    private final int order;
    public Bitmap bitmap;

    public PuzzleTile(Bitmap bitmapInput, int orderInput) {
        bitmap = bitmapInput;
        order = orderInput;
    }

    public  int getOrder(){
        return order;
    }
}
