package com.amaromerovic.journalapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import journalapp.databinding.ActivityWelcomeBinding;


public class WelcomeActivity extends AppCompatActivity {
    private ActivityWelcomeBinding binding;
    private FirebaseAuth firebaseAuth;
    @Nullable
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();


        Handler.createAsync(Looper.getMainLooper(), message -> false).postDelayed(() -> {
            Intent intent;
            if (user != null) {
                intent = new Intent(getApplicationContext(), JournalListActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), StartWritingActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2000);

    }
}