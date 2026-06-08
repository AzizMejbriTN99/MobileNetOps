package com.mejbri.netopssynchromobile.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF         = "netops_session";
    private static final String KEY_TOKEN     = "token";
    private static final String KEY_USERNAME  = "username";
    private static final String KEY_ROLE      = "role";
    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME  = "lastname";
    private static final String KEY_AVATAR    = "avatar_b64";

    public static void save(Context ctx, String token, String username, String role,
                            String firstName, String lastName) {
        prefs(ctx).edit()
                .putString(KEY_TOKEN,     token)
                .putString(KEY_USERNAME,  username)
                .putString(KEY_ROLE,      role)
                .putString(KEY_FIRSTNAME, firstName != null ? firstName : "")
                .putString(KEY_LASTNAME,  lastName  != null ? lastName  : "")
                .apply();
    }

    public static String getToken(Context ctx)     { return prefs(ctx).getString(KEY_TOKEN,     null); }
    public static String getUsername(Context ctx)  { return prefs(ctx).getString(KEY_USERNAME,  "");   }
    public static String getFirstname(Context ctx) { return prefs(ctx).getString(KEY_FIRSTNAME, "");   }
    public static String getLastname(Context ctx)  { return prefs(ctx).getString(KEY_LASTNAME,  "");   }

    /** Returns "Firstname Lastname" or falls back to username. */
    public static String getDisplayName(Context ctx) {
        String fn = getFirstname(ctx);
        String ln = getLastname(ctx);
        if (fn.isEmpty() && ln.isEmpty()) return getUsername(ctx);
        return (fn + " " + ln).trim();
    }

    /** Initials for the avatar circle (up to 2 chars). */
    public static String getInitials(Context ctx) {
        String fn = getFirstname(ctx);
        String ln = getLastname(ctx);
        if (!fn.isEmpty() && !ln.isEmpty())
            return String.valueOf(fn.charAt(0)).toUpperCase() + String.valueOf(ln.charAt(0)).toUpperCase();
        if (!fn.isEmpty())  return String.valueOf(fn.charAt(0)).toUpperCase();
        String u = getUsername(ctx);
        return u != null && !u.isEmpty() ? String.valueOf(u.charAt(0)).toUpperCase() : "?";
    }

    public static void updateName(Context ctx, String firstname, String lastname) {
        prefs(ctx).edit()
                .putString(KEY_FIRSTNAME, firstname != null ? firstname : "")
                .putString(KEY_LASTNAME,  lastname  != null ? lastname  : "")
                .apply();
    }

    /** Save profile picture as base64 string (stored locally, no backend call). */
    public static void saveAvatar(Context ctx, String base64) {
        prefs(ctx).edit().putString(KEY_AVATAR, base64).apply();
    }

    public static String getAvatar(Context ctx) {
        return prefs(ctx).getString(KEY_AVATAR, null);
    }

    public static boolean isLoggedIn(Context ctx) { return getToken(ctx) != null; }

    public static void clear(Context ctx) { prefs(ctx).edit().clear().apply(); }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
}