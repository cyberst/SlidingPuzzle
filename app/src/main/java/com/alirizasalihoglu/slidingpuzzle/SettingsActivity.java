package com.alirizasalihoglu.slidingpuzzle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;


public class SettingsActivity extends AppCompatActivity {

    SharedPreferences appSettings;
    SeekBar sizeSeekBar;
    ShareDialog shareFaceDialog;
    int size;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Button shareFaceButton = (Button) findViewById(R.id.shareAppFaceButton);
        shareFaceDialog = new ShareDialog(this);

        sizeSeekBar = (SeekBar) findViewById(R.id.sizeSeekBar);
        loadSettings();
        Twitter.initialize(this);

        final TextView  sizeText = (TextView) findViewById(R.id.sizeText);
        sizeText.setText(String.valueOf(size));

        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                size  = progress + 3;
                sizeText.setText(String.valueOf(size));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Puzzle Size Is :" + size, Toast.LENGTH_SHORT).show();

            }
        });
    }



    private void loadSettings() {
        appSettings = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        size = appSettings.getInt("Size", 3);
        sizeSeekBar.setProgress(size - 3);
    }

    @Override
    protected void onDestroy() {
        setSettings();
        super.onDestroy();
    }
    public void clickShareFace(View view) {
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse("https://web.itu.edu.tr/salihoglual")).setQuote("Hey, check this game!").build();
            shareFaceDialog.show(content);
        }
    }
    public void clickShareTwit(View view) {
        TweetComposer.Builder builder = new TweetComposer.Builder(this)
                .text("https://web.itu.edu.tr/salihoglual");
        builder.show();

    }

    public void clickShareWp(View view) {

        final String appPackageName = "com.whatsapp";

        if(checkAppInstalled(appPackageName)){
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://web.itu.edu.tr/salihoglual");
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        }
        else{
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }
    private boolean checkAppInstalled(String packageName){
        try {
            getApplicationContext().getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    private void setSettings() {
        appSettings.edit().putInt("Size", size).apply();
    }

}
