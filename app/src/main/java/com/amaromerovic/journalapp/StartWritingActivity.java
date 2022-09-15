package com.amaromerovic.journalapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import journalapp.databinding.ActivityStartWirtingBinding;


public class StartWritingActivity extends AppCompatActivity {
    private ActivityStartWirtingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartWirtingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.startWritingButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}