package com.amaromerovic.journalapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.amaromerovic.journalapp.util.Util;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import journalapp.R;
import journalapp.databinding.ActivityCreateAccountBinding;

public class CreateAccountActivity extends AppCompatActivity {
    private ActivityCreateAccountBinding binding;
    private FirebaseAuth firebaseAuth;
    @Nullable
    private FirebaseUser currentUser;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbCollection = db.collection("Unverified Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();


        binding.createAccountButtonFromCreateAccount.setOnClickListener(view -> {
            String email = binding.emailCreateAccount.getText().toString().trim();
            String password = binding.passwordCreateAccount.getText().toString().trim();
            String username = binding.usernameCreateAccount.getText().toString().trim();
            Util.hideSoftKeyboard(view);
            createUserEmailAccount(username, email, password);
        });


    }

    private void createUserEmailAccount(String username, @NonNull String email, @NonNull String password) {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            showProgress();
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            currentUser = firebaseAuth.getCurrentUser();
                            Map<String, String> data = new HashMap<>();
                            assert currentUser != null;
                            data.put(Util.USER_ID_KEY, currentUser.getUid());
                            data.put(Util.EMAIL_KEY, email);
                            data.put(Util.PASSWORD_KEY, password);
                            data.put(Util.USERNAME_KEY, username);
                            dbCollection.add(data)
                                    .addOnSuccessListener(documentReference -> documentReference.get()
                                            .addOnCompleteListener(task1 -> {
                                                if (!task1.getResult().exists()) {
                                                    hideProgress();
                                                    createSnackbar(Util.SNACKBAR_CREATION_FAILURE_MESSAGE);
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                e.printStackTrace();
                                                hideProgress();
                                                createSnackbar(Util.SNACKBAR_CREATION_FAILURE_MESSAGE);

                                            }))
                                    .addOnFailureListener(e -> {
                                        e.printStackTrace();
                                        hideProgress();
                                        createSnackbar(Util.SNACKBAR_CREATION_FAILURE_MESSAGE);
                                    });


                            if (currentUser != null) {
                                sendVerificationEmail(username, email, password);
                            } else {
                                createSnackbar(Util.SNACKBAR_CREATION_FAILURE_MESSAGE);
                                hideProgress();
                            }
                        } else {
                            createSnackbar(Util.SNACKBAR_CREATION_FAILURE_MESSAGE);
                            hideProgress();
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        createSnackbarWithButton("Email already in use or bad formatted!", "LOG IN");
                        hideProgress();

                    });
        } else {
            createSnackbar(Util.SNACKBAR_CREATION_EMPTY_FIELDS_MESSAGE);
        }
    }

    private void createSnackbarWithButton(@NonNull String text, String buttonText) {
        Snackbar snackbar = Snackbar.make(binding.linearLayout, text, Util.SNACKBAR_LENGTH)
                .setAction(buttonText, view -> finish());
        View view = snackbar.getView();
        snackbar.setBackgroundTint(Color.BLACK);
        TextView actionTextView = view.findViewById(com.google.android.material.R.id.snackbar_action);
        TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
        if (actionTextView != null && textView != null) {
            textView.setTextColor(Color.WHITE);
            actionTextView.setTextSize(18);
            actionTextView.setTextColor(ContextCompat.getColor(this, R.color.orange));
        }
        snackbar.show();
    }

    private void createSnackbar(@NonNull String text) {
        Snackbar snackbar = Snackbar.make(binding.linearLayout, text, Util.SNACKBAR_LENGTH);
        snackbar.setBackgroundTint(Color.BLACK);
        View view = snackbar.getView();
        TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextColor(Color.WHITE);
        }
        snackbar.show();
        firebaseAuth.signOut();
    }

    private void sendVerificationEmail(String username, String email, String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().signOut();
                            binding.progressBarLayoutFromCreateAccount.setVisibility(View.GONE);
                            createSnackbar("An email verification has been sent to you!");
                            Handler.createAsync(Looper.getMainLooper(), message -> false).postDelayed(() -> {
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                Intent intent = getIntent();
                                intent.putExtra(Util.EMAIL_KEY, email);
                                intent.putExtra(Util.PASSWORD_KEY, password);
                                setResult(RESULT_OK, intent);
                                finish();
                            }, 2000);

                        }
                    });
        }
    }

    private void showProgress() {
        if (binding.progressBarLayoutFromCreateAccount.getVisibility() == View.GONE) {
            binding.progressBarLayoutFromCreateAccount.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        }
    }

    private void hideProgress() {
        if (binding.progressBarLayoutFromCreateAccount.getVisibility() == View.VISIBLE) {
            binding.progressBarLayoutFromCreateAccount.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        assert firebaseAuth != null;
        firebaseAuth.signOut();
    }
}