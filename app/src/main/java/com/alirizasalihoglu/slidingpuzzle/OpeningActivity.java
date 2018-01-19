package com.alirizasalihoglu.slidingpuzzle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class OpeningActivity extends AppCompatActivity {

    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;

    private static final String TAG = "Sliding";
    private boolean mShowSignInButton = true;

    private GoogleSignInClient mGoogleSignInClient;
    private LeaderboardsClient mLeaderboardsClient;
    private PlayersClient mPlayersClient;

    private String mGreeting = "Hello, anonymous user (not signed in)";
    private TextView mGreetingTextView;
    private View mSignInButtonView;
    private View mSignOutButtonView;
    private View mShowLeaderboardsButton;
    private int score = Integer.MAX_VALUE;
    SharedPreferences appSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

        appSettings = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        mGreetingTextView = findViewById(R.id.text_greeting);
        mSignInButtonView = findViewById(R.id.sign_in_button);
        mSignOutButtonView = findViewById(R.id.sign_out_button);
        mShowLeaderboardsButton = findViewById(R.id.show_leaderboards_button);
        TextView gameName = (TextView)findViewById(R.id.gameNameText);
        TextView gameName2 = (TextView)findViewById(R.id.gameNameText2);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(),  "font/delius_unicase_regular.ttf");

        gameName.setTypeface(font);
        gameName2.setTypeface(font);

        updateUI();


    }


    public void clickPlay(View view){
        Intent playIntent = new Intent(getApplicationContext(), PhotoActivity.class);
        onStop();
        startActivity(playIntent);
    }

    public void clickSettings(View view){
        Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(settingsIntent);
    }

    @Override
    protected  void onResume(){
        super.onResume();
        signInSilently();
    }

    @Override
    protected void onDestroy() {
        appSettings.edit().putInt("Score",Integer.MAX_VALUE).apply();
        super.onDestroy();
    }

    private void signInSilently() {

        Log.d(TAG, "signInSilently()");
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }
                    }
                });
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");

        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);

        setShowSignInButton(false);

        mPlayersClient.getCurrentPlayer()
                .addOnCompleteListener(new OnCompleteListener<Player>() {
                    @Override
                    public void onComplete(@NonNull Task<Player> task) {
                        String displayName;
                        if (task.isSuccessful()) {
                            displayName = task.getResult().getDisplayName();
                        } else {
                            Exception e = task.getException();
                            handleException(e, getString(R.string.players_exception));
                            displayName = "???";
                        }
                        setGreeting("Hello, " + displayName);
                    }
                });

        int temp_score = appSettings.getInt("Score",Integer.MAX_VALUE);
        if(temp_score != Integer.MAX_VALUE){
            appSettings.edit().putInt("Score",Integer.MAX_VALUE).apply();
        }
        if(temp_score < score){
            score = temp_score;
            submitScoreToLeaderboard(score);
        }
    }
    private void onDisconnected() {
        Log.d(TAG, "onDisconnected()");

        mLeaderboardsClient = null;
        mPlayersClient = null;
        score = Integer.MAX_VALUE;

        setShowSignInButton(true);
        setGreeting(getString(R.string.signed_out_greeting));
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    public void clickSignIn(View view) {
        startSignInIntent();
    }

    private void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    public void clickSignOut(View view) {
        signOut();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }

                onDisconnected();

                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        }
    }
    private void handleException(Exception e, String details) {
        int status = 0;

        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            status = apiException.getStatusCode();
        }

        String message = getString(R.string.status_exception_error, details, status, e);

        new AlertDialog.Builder(OpeningActivity.this)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }


    public void setShowSignInButton(boolean showSignIn) {
        mShowSignInButton = showSignIn;
        updateUI();
    }

    private void updateUI() {

        mGreetingTextView.setText(mGreeting);
        mShowLeaderboardsButton.setEnabled(!mShowSignInButton);
        mSignInButtonView.setVisibility(mShowSignInButton ? View.VISIBLE : View.GONE);
        mSignOutButtonView.setVisibility(mShowSignInButton ? View.GONE : View.VISIBLE);

    }

    public void setGreeting(String greeting) {
        mGreeting = greeting;
        updateUI();
    }
    private void signOut() {
        Log.d(TAG, "signOut()");

        if (!isSignedIn()) {
            Log.w(TAG, "signOut() called, but was not signed in!");
            return;
        }

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        boolean successful = task.isSuccessful();
                        Log.d(TAG, "signOut(): " + (successful ? "success" : "failed"));

                        onDisconnected();
                    }
                });
    }
    public void clickLeaderboard(View view) {
        onShowLeaderboardsRequested();
    }

    public void onShowLeaderboardsRequested() {
        mLeaderboardsClient.getLeaderboardIntent(getString(R.string.leaderboard_id))
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_UNUSED);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, getString(R.string.leaderboards_exception));
                    }
                });
    }
    private void submitScoreToLeaderboard(int score) {
        mLeaderboardsClient.submitScore(getString(R.string.leaderboard_id),
                score);
    }

    @Override
    public void onBackPressed() {

    }
}
