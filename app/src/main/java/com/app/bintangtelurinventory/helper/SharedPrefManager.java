package com.app.bintangtelurinventory.helper;

import android.content.Context;
import android.content.SharedPreferences;

//session
public class SharedPrefManager {
    private static final String PREF_NAME = "INVENTORY_PREF";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static SharedPrefManager instance;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private SharedPrefManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void saveUser(String userId, String email) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
