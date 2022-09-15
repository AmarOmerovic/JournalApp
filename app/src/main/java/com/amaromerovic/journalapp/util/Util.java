package com.amaromerovic.journalapp.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

public class Util {
    public static final String USER_ID_KEY = "userID";
    public static final String USERNAME_KEY = "username";
    public static final String EMAIL_KEY = "email";
    public static final String PASSWORD_KEY = "password";

    public static final String SNACKBAR_CREATION_FAILURE_MESSAGE = "Something went wrong!";
    public static final String SNACKBAR_CREATION_EMPTY_FIELDS_MESSAGE = "Please fill all the required fields!";

    public static final int SNACKBAR_LENGTH = 5000;


    public static void hideSoftKeyboard(@NonNull View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
