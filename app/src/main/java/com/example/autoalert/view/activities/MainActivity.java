package com.example.autoalert.view.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.util.Log;

import com.example.autoalert.R;
import com.example.autoalert.view.fragments.PasosASeguirFragment;
import com.example.autoalert.view.fragments.PrincipalFragment;

public class MainActivity extends AppCompatActivity implements PasosASeguirFragment.OnCompleteListener {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String FIRST_TIME_KEY = "isFirstTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean(FIRST_TIME_KEY, true);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setReorderingAllowed(true);

            if (isFirstTime) {
                PasosASeguirFragment pasosFragment = new PasosASeguirFragment();
                pasosFragment.setOnCompleteListener(this);
                transaction.add(R.id.fcv_main_container, pasosFragment);
            } else {
                transaction.add(R.id.fcv_main_container, new PrincipalFragment());
            }

            transaction.commit();
        }
    }


    @Override
    public void onComplete() {
        markFirstTimeCompleted();
    }

    public void markFirstTimeCompleted() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(FIRST_TIME_KEY, false);
        editor.apply();
    }
}
