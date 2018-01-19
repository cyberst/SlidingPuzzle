package com.alirizasalihoglu.slidingpuzzle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.games.LeaderboardsClient;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

public class WinActivity extends AppCompatActivity {

    private LeaderboardsClient mLeaderboardsClient;
    int score, size, moveCount, backCount, time;
    TextView moveCountText, timeText, scoreText;
    ShareDialog shareFaceDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        moveCountText = (TextView) findViewById(R.id.moveCountText);
        timeText = (TextView) findViewById(R.id.timeText);
        scoreText = (TextView) findViewById(R.id.scoreText);
        shareFaceDialog = new ShareDialog(this);

    }
    @Override
    protected void onStart(){
        super.onStart();
        Intent intent = getIntent();
        moveCount = intent.getIntExtra("Move Count", 100);
        backCount = intent.getIntExtra("Back Count",100);
        time = intent.getIntExtra("Time", 100);
        size = intent.getIntExtra("Size",3);
        calculateScore();
        updateUI();
    }

    public void clickShareFace(View view) {
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            String quote = "Hey, check this out I got " + score + " score on Sliding Puzzle!";
            ShareLinkContent content = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse("https://web.itu.edu.tr/salihoglual")).setQuote(quote).build();
            shareFaceDialog.show(content);
        }
    }
    public void clickShareTwit(View view) {
        TweetComposer.Builder builder = new TweetComposer.Builder(this)
                .text("Hey, check this out I got " + score + " score on Sliding Puzzle! https://web.itu.edu.tr/salihoglual");
        builder.show();

    }
    private void calculateScore() {
        int multiplier;
        if(size == 5) {
            multiplier = 3;
        }
        else if(size == 4){
            multiplier = 4;
        }
        else {
            multiplier = 5;
        }
        score = multiplier*(moveCount + time + 2*backCount);
    }

    private void updateUI() {
        moveCountText.setText(String.valueOf(moveCount));
        timeText.setText(String.valueOf(time));
        scoreText.setText(String.valueOf(score));
    }

    public void clickOk(View view){
        Intent returnOpeningIntent = new Intent(getApplicationContext(), OpeningActivity.class);
        returnOpeningIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        SharedPreferences appSettings = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        appSettings.edit().putInt("Score", score).apply();
        startActivityIfNeeded(returnOpeningIntent, 0);
    }
    @Override
    public void onBackPressed() {

    }
}
