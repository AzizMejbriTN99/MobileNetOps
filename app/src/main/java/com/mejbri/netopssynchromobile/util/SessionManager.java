package com.mejbri.netopssynchromobile.util;

import android.content.Context;

public class SessionManager {
    private static final String PREF = "netops_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";

    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";

    public static void save(Context ctx, String token, String username, String role, String firstName, String lastName) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USERNAME, username)
                .putString(KEY_ROLE, role)
                .putString(KEY_FIRSTNAME, firstName)
                .putString(KEY_LASTNAME, lastName)
                .apply();
    }

    public static String getToken(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_TOKEN, null);
    }

    public static String getUsername(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_USERNAME, null);
    }

    public static boolean isLoggedIn(Context ctx) {
        return getToken(ctx) != null;
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
    }
}