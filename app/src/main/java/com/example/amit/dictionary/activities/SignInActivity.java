package com.example.amit.dictionary.activities;

/**
 * Created by AMIT YADAV
 * 18/10/18
 */

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.amit.dictionary.DictionaryApplication;
import com.example.amit.dictionary.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

//TODO this is also a splash activity so if needed implement that one too
public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 32132;
    boolean isFirstTime;
    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mFirebaseAuth = FirebaseAuth.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isFirstTime = sharedPreferences.getBoolean(getString(R.string.IS_FIRST_TIME_KEY), true);

        Intent intent = getIntent();
        mAuthStateListener = (@NonNull FirebaseAuth firebaseAuth) -> {
           FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                //logged in
                signInsuccessful();
            } else {
                //send to log in screen
                if (isFirstTime || intent != null) {
                    isFirstTime = false;
                    sharedPreferences.edit().putBoolean(getString(R.string.IS_FIRST_TIME_KEY), isFirstTime).apply();
                    startSignInFlow();
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuthStateListener != null)
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null)
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // no matter what happens detach authStateListener
            if (mAuthStateListener != null)
                mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
            mAuthStateListener = null;
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    signInsuccessful();
                } else {
                    //may be there is some error so ask user to try again to sign in
                    signInUnsuccessful();
                }
            } else {
                //user cancelled to sign in
                signInUnsuccessful();
            }
        }
    }


    void startSignInFlow() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(true)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }


    void signInsuccessful() {
        Toast.makeText(SignInActivity.this,
                getString(R.string.signed_in_msg) + FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                Toast.LENGTH_SHORT).show();
        DictionaryApplication.upDateWidgetsIfAny(this);
        startDrawerActivity();
    }

    void signInUnsuccessful() {
        Toast.makeText(SignInActivity.this, getString(R.string.sign_in_failed), Toast.LENGTH_SHORT).show();
        startDrawerActivity();
    }

    void startDrawerActivity() {
        startActivity(new Intent(this, DrawerListener.class));
        finish();
    }

}
