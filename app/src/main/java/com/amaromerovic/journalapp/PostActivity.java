package com.amaromerovic.journalapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amaromerovic.journalapp.model.Journal;
import com.amaromerovic.journalapp.util.Util;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;

import journalapp.databinding.ActivityPostBinding;

public class PostActivity extends AppCompatActivity {
    private ActivityPostBinding binding;
    private String username;
    private String currentUserID;
    private Uri imageUri;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Journal");

    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storageReference = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        username = intent.getStringExtra(Util.USERNAME_KEY);
        currentUserID = intent.getStringExtra(Util.USER_ID_KEY);

        Log.d("Test1234", "onCreate: " + username + " " + currentUserID);

        binding.postUsername.setText(username);

        binding.postImagePlaceHolder.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            getImageFromGallery.launch(galleryIntent);
        });


        binding.savePostButton.setOnClickListener(view -> {
            showProgress();
            String title = binding.postTitle.getText().toString().trim();
            String thoughts = binding.postThoughts.getText().toString().trim();

            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts) && (imageUri != null)) {
                final StorageReference filePath = storageReference.child("Journal Images").child("Image_" + Timestamp.now().getSeconds());
                filePath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {

                                    Journal journal = new Journal(currentUserID, username, new Timestamp(new Date()), title, thoughts, uri.toString());
                                    collectionReference.add(journal)
                                            .addOnSuccessListener(documentReference -> {
                                                hideProgress();
                                                createSnackbar("New post created successfully!");
                                                startActivity(new Intent(getApplicationContext(), JournalListActivity.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                e.printStackTrace();
                                                createSnackbar(Util.SNACKBAR_CREATION_FAILURE_MESSAGE);
                                                hideProgress();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    createSnackbar(Util.SNACKBAR_CREATION_FAILURE_MESSAGE);
                                    hideProgress();
                                }))
                        .addOnFailureListener(e -> {
                            e.printStackTrace();
                            createSnackbar(Util.SNACKBAR_CREATION_FAILURE_MESSAGE);
                            hideProgress();
                        });
            } else {
                createSnackbar(Util.SNACKBAR_CREATION_EMPTY_FIELDS_MESSAGE);
                hideProgress();
            }
        });
    }

    private void showProgress() {
        if (binding.progressBarLayoutCardView.getVisibility() == View.GONE && binding.progressBarLayoutWholeScreen.getVisibility() == View.GONE) {
            binding.progressBarLayoutCardView.setVisibility(View.VISIBLE);
            binding.progressBarLayoutWholeScreen.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void hideProgress() {
        if (binding.progressBarLayoutCardView.getVisibility() == View.VISIBLE && binding.progressBarLayoutWholeScreen.getVisibility() == View.VISIBLE) {
            binding.progressBarLayoutCardView.setVisibility(View.GONE);
            binding.progressBarLayoutWholeScreen.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }


    @NonNull
    ActivityResultLauncher<Intent> getImageFromGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    assert data != null;
                    imageUri = data.getData();

                    binding.postImage.setImageURI(imageUri);
                }
            });


    private void createSnackbar(@NonNull String text) {
        Snackbar snackbar = Snackbar.make(binding.cardView, text, Util.SNACKBAR_LENGTH);
        snackbar.setBackgroundTint(Color.BLACK);
        View view = snackbar.getView();
        TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextColor(Color.WHITE);
        }
        snackbar.show();
    }
}