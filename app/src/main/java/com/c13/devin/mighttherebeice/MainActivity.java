package com.c13.devin.mighttherebeice;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MainActivity extends Activity
        implements LocationSelectFragment.OnLocationSelectedListener,
        MightThereBeIceFragment.OnChangeLocationSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
            if (sharedPreferences.contains(LocationSelectFragment.LOCATION)) {
                Bundle args = new Bundle();
                args.putString(LocationSelectFragment.LOCATION, sharedPreferences.getString(LocationSelectFragment.LOCATION, ""));

                MightThereBeIceFragment mightThereBeIceFragment = new MightThereBeIceFragment();
                mightThereBeIceFragment.setArguments(args);

                getFragmentManager().beginTransaction()
                        .add(R.id.container, mightThereBeIceFragment)
                        .commit();
            } else {

                getFragmentManager().beginTransaction()
                        .add(R.id.container, new LocationSelectFragment())
                        .commit();
            }
        }
    }

    @Override
    public void onLocationSelected(String location) {
        MightThereBeIceFragment newFragment = new MightThereBeIceFragment();
        Bundle args = new Bundle();
        args.putString(LocationSelectFragment.LOCATION, location);
        newFragment.setArguments(args);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onChangeLocationSelected() {
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.remove(LocationSelectFragment.LOCATION);
        editor.apply();

        LocationSelectFragment newFragment = new LocationSelectFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
