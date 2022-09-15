package com.amaromerovic.journalapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import journalapp.R;
import journalapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    @Nullable
    private FirebaseUser user;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbCollection = db.collection("Users");
    private final CollectionReference dbCollectionUnverified = db.collection("Unverified Users");

    private boolean firstLog;
    private int mismatchInputs;
    private boolean passReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        mismatchInputs = 0;

        binding.loginButton.setOnClickListener(view -> {
            if (firebaseAuth != null) {
                firebaseAuth.signOut();
            }
            String email = binding.emailLogin.getText().toString().trim();
            String password = binding.passwordLogin.getText().toString().trim();
            Util.hideSoftKeyboard(view);
            logInWithEmailAndPassword(email, password);
        });

        binding.createAccountButtonFromLogin.setOnClickListener(view -> {
            if (user != null && firebaseAuth != null) {
                firebaseAuth.signOut();
            }
            Intent intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
            getUsername.launch(intent);
        });
    }


    private void logInWithEmailAndPassword(@NonNull String email, @NonNull String password) {
        showProgress();
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            checkIfEmailIsVerified();
                        } else {
                            hideProgress();
                        }
                    })
                    .addOnFailureListener(e -> firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                        if (Objects.requireNonNull(task.getResult().getSignInMethods()).isEmpty()) {
                            createSnackbar("Nonexistent account!");
                        } else {
                            if (mismatchInputs == 3) {
                                resetPassword(email);
                            } else {
                                createSnackbar("Input mismatch!");
                                mismatchInputs++;
                            }
                        }
                        e.printStackTrace();
                        hideProgress();
                    }).addOnFailureListener(e1 -> {
                        e1.printStackTrace();
                        createSnackbar(Util.SNACKBAR_CREATION_FAILURE_MESSAGE);
                        hideProgress();
                    }));

        } else {
            createSnackbar(Util.SNACKBAR_CREATION_EMPTY_FIELDS_MESSAGE);
            hideProgress();
        }
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
    }

    private void showProgress() {
        if (binding.progressBarLayoutFromLogin.getVisibility() == View.GONE) {
            binding.progressBarLayoutFromLogin.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void checkIfEmailIsVerified() {
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified()) {
                passReset = true;
                dbCollection
                        .get()
                        .addOnCompleteListener(tsk -> {
                            if (tsk.isSuccessful()) {
                                if (tsk.getResult().isEmpty()) {
                                    firstLog = true;
                                }
                                for (QueryDocumentSnapshot document : tsk.getResult()) {
                                    firstLog = true;
                                    if (Objects.equals(document.getString(Util.USER_ID_KEY), user.getUid())) {
                                        firstLog = false;
                                        break;
                                    }
                                }

                                if (firstLog) {
                                    dbCollectionUnverified
                                            .get()
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        if (user.getUid().equals(document.getString(Util.USER_ID_KEY))) {
                                                            Map<String, String> data = new HashMap<>();
                                                            data.put(Util.USER_ID_KEY, document.getString(Util.USER_ID_KEY));
                                                            data.put(Util.EMAIL_KEY, document.getString(Util.EMAIL_KEY));
                                                            data.put(Util.PASSWORD_KEY, document.getString(Util.PASSWORD_KEY));
                                                            data.put(Util.USERNAME_KEY, document.getString(Util.USERNAME_KEY));
                                                            document.getReference().delete();
                                                            dbCollection.add(data)
                                                                    .addOnSuccessListener(documentReference -> documentReference.get()
                                                                            .addOnCompleteListener(task1 -> {
                                                                                if (task1.getResult().exists()) {
                                                                                    firstLog = false;
                                                                                    createSnackbar("Account created, and activated successfully!");
                                                                                    binding.progressBarLayoutFromLogin.setVisibility(View.GONE);
                                                                                } else {
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
                                                        }
                                                    }
                                                }
                                            });
                                } else if (passReset) {
                                    passReset = false;
                                    for (QueryDocumentSnapshot document : tsk.getResult()) {
                                        if (user.getUid().equals(document.getString(Util.USER_ID_KEY))) {
                                            String path = document.getReference().getPath();
                                            String[] fullPath = path.split("/");
                                            String docID = fullPath[fullPath.length - 1];

                                            dbCollection.document(docID).update(Util.PASSWORD_KEY, binding.passwordLogin.getText().toString().trim())
                                                    .addOnSuccessListener(unused -> {
                                                        hideProgress();
                                                        createSnackbar("Welcome back!");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        e.printStackTrace();
                                                        hideProgress();
                                                        createSnackbar(Util.SNACKBAR_CREATION_FAILURE_MESSAGE);
                                                    });
                                        }
                                    }
                                }
                            }
                        });
                Handler.createAsync(getMainLooper(), message -> false).postDelayed(() -> {
                    Intent intent = new Intent(getApplicationContext(), JournalListActivity.class);
                    startActivity(intent);
                    finish();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }, 2000);
            } else {
                resendVerificationEmail();
                hideProgress();
            }
        } else {
            createSnackbar("User not found!");
            hideProgress();
        }

    }

    private void resendVerificationEmail() {
        Snackbar snackbar = Snackbar.make(binding.linearLayout, "Email not verified!", Util.SNACKBAR_LENGTH)
                .setAction("RESEND", view -> {
                    if (user != null) {
                        user.sendEmailVerification()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (firebaseAuth != null) {
                                            firebaseAuth.signOut();
                                        }
                                        createSnackbar("Email verification has been resented!");
                                    } else if (!task.isSuccessful()) {
                                        createSnackbar("Please wait a few moments!");
                                    }
                                });
                    }
                });
        snackbar.setBackgroundTint(Color.BLACK);
        View view = snackbar.getView();
        TextView actionTextView = view.findViewById(com.google.android.material.R.id.snackbar_action);
        TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
        if (actionTextView != null && textView != null) {
            textView.setTextColor(Color.WHITE);
            actionTextView.setTextSize(18);
            actionTextView.setTextColor(ContextCompat.getColor(this, R.color.orange));
        }
        snackbar.show();
    }


    private void resetPassword(@NonNull String email) {
        Snackbar snackbar = Snackbar.make(binding.linearLayout, "Input mismatch!", Util.SNACKBAR_LENGTH)
                .setAction("RESET PASSWORD", view -> {
                    if (!TextUtils.isEmpty(email)) {
                        firebaseAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        createSnackbar("Reset password email has been sent!");
                                        binding.passwordLogin.setText("");
                                        passReset = true;
                                    } else {
                                        createSnackbar("Please wait a few moments!");
                                    }
                                });
                    }
                });
        snackbar.setBackgroundTint(Color.BLACK);
        View view = snackbar.getView();
        TextView actionTextView = view.findViewById(com.google.android.material.R.id.snackbar_action);
        TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
        if (actionTextView != null && textView != null) {
            textView.setTextColor(Color.WHITE);
            actionTextView.setTextSize(18);
            actionTextView.setTextColor(ContextCompat.getColor(this, R.color.orange));
        }
        snackbar.show();
    }

    private void hideProgress() {
        if (binding.progressBarLayoutFromLogin.getVisibility() == View.VISIBLE) {
            binding.progressBarLayoutFromLogin.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }


    @NonNull
    ActivityResultLauncher<Intent> getUsername = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    assert data != null;
                    binding.emailLogin.setText(data.getStringExtra(Util.EMAIL_KEY));
                    binding.passwordLogin.setText(data.getStringExtra(Util.PASSWORD_KEY));
                    createSnackbar("Please verify your email!");
                }
            });
}
