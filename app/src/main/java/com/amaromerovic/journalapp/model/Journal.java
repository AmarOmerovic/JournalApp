package com.amaromerovic.journalapp.model;


import com.google.firebase.Timestamp;

public class Journal {
    private String userID;
    private String username;
    private Timestamp timestamp;
    private String title;
    private String thoughts;
    private String imageUri;

    public Journal(String userID, String username, Timestamp timestamp, String title, String thoughts, String imageUri) {
        this.userID = userID;
        this.username = username;
        this.timestamp = timestamp;
        this.title = title;
        this.thoughts = thoughts;
        this.imageUri = imageUri;
    }

    public Journal() {
    }

    public String getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getThoughts() {
        return thoughts;
    }

    public String getImageUri() {
        return imageUri;
    }
}
