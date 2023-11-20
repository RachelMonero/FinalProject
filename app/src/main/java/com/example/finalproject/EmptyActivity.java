package com.example.finalproject;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class EmptyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        Bundle dataToPass = getIntent().getExtras();
        DetailsListViewFragment detailsListViewFragment = new DetailsListViewFragment();
        detailsListViewFragment.setArguments( dataToPass );
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.placeholder, detailsListViewFragment)
                .commit();
    }

}