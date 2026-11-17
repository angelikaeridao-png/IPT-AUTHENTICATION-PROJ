package com.example.classtag;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthHelper {
    private static final String TAG = "AuthHelper";
    private static final String PREFS_NAME = "ClassTagAuthPrefs";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";

    private static FirebaseAuth mAuth;

    public static FirebaseAuth getAuth(Context context) {
        if (mAuth == null) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context.getApplicationContext());
                }
                mAuth = FirebaseAuth.getInstance();
            } catch (Exception e) {
                Log.e(TAG, "Error initializing FirebaseAuth: " + e.getMessage());
            }
        }
        return mAuth;
    }

    public static boolean isLoggedIn(Context context) {
        FirebaseAuth auth = getAuth(context);
        if (auth != null && auth.getCurrentUser() != null) {
            return true;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public static String getUserEmail(Context context) {
        FirebaseAuth auth = getAuth(context);
        if (auth != null && auth.getCurrentUser() != null && auth.getCurrentUser().getEmail() != null) {
            return auth.getCurrentUser().getEmail();
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_EMAIL, "student@classtag.edu");
    }

    public static String getUserName(Context context) {
        FirebaseAuth auth = getAuth(context);
        if (auth != null && auth.getCurrentUser() != null) {
            FirebaseUser user = auth.getCurrentUser();
            if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
                return user.getDisplayName();
            }
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_NAME, "Angelika");
    }

    public static void setLocalUserSession(Context context, String email, String name) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, (name != null && !name.trim().isEmpty()) ? name : "Angelika")
                .apply();
    }

    public static void logout(Context context) {
        try {
            FirebaseAuth auth = getAuth(context);
            if (auth != null) {
                auth.signOut();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during logout: " + e.getMessage());
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
