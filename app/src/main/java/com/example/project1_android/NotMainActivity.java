package com.example.project1_android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class NotMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}