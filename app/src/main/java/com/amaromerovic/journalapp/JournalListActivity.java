package com.amaromerovic.journalapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.amaromerovic.journalapp.adapter.JournalPostRecyclerViewAdapter;
import com.amaromerovic.journalapp.model.Journal;
import com.amaromerovic.journalapp.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import journalapp.R;
import journalapp.databinding.ActivityJournalListBinding;

public class JournalListActivity extends AppCompatActivity {
    private ActivityJournalListBinding binding;

    private FirebaseAuth firebaseAuth;
    @Nullable
    private FirebaseUser user;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");
    private final CollectionReference journalListReference = db.collection("Journal");

    private List<Journal> journalList;
    private JournalPostRecyclerViewAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityJournalListBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        journalList = new ArrayList<>();
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.addJournalPost) {
                showProgress();
                if (user != null && firebaseAuth != null) {
                    collectionReference.get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                        if (user.getUid().equals(documentSnapshot.getString(Util.USER_ID_KEY))) {

                                            Handler.createAsync(Looper.getMainLooper(), message -> false).postDelayed(() -> {
                                                hideProgress();
                                                Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                                                intent.putExtra(Util.USERNAME_KEY, documentSnapshot.getString(Util.USERNAME_KEY));
                                                intent.putExtra(Util.USER_ID_KEY, user.getUid());
                                                startActivity(intent);
                                            }, 1000);
                                        }
                                    }
                                } else {
                                    hideProgress();
                                }
                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                hideProgress();
                            });
                }
            } else if (id == R.id.signOut) {
                if (user != null && firebaseAuth != null) {
                    firebaseAuth.signOut();
                    Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(login);
                    finish();
                }
            }
            return true;
        });


        assert user != null;
        journalListReference
                .whereEqualTo(Util.USER_ID_KEY, user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        binding.emptyListText.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyListText.setVisibility(View.GONE);
                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                            Journal journal = snapshot.toObject(Journal.class);
                            journalList.add(journal);
                        }
                        recyclerViewAdapter = new JournalPostRecyclerViewAdapter(journalList, getApplicationContext(), this);
                        binding.recyclerView.setAdapter(recyclerViewAdapter);
                        recyclerViewAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void hideProgress() {
        if (binding.progressBarLayoutFromList.getVisibility() == View.VISIBLE) {
            binding.progressBarLayoutFromList.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void showProgress() {
        if (binding.progressBarLayoutFromList.getVisibility() == View.GONE) {
            binding.progressBarLayoutFromList.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

}