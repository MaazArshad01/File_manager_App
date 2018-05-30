package com.galaxy.quickfilemanager.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Umiya Mataji on 1/17/2017.
 */

public class PreferencesUtils {

    public static final String PREF_IN_APP = "PREF_IN_APP";
    public static final String PREF_RESTORE = "PREF_RESTORE";
    public static final String PREF_BANNER = "PREF_BANNER";
    public static final String PREF_INTER = "PREF_INTER";
    public static final String PREF_BACK = "PREF_BACK";

    public static boolean saveToPreference(Context context, String key, Object value) {
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constats.PREFS_NAME, 0);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else {
            // insert code here for handle errror
        }

        return editor.commit();
    }

    public static boolean removeValueFromPreference(Context context, String key) {
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constats.PREFS_NAME, 0);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        return editor.commit();
    }

    public static Object getValueFromPreference(Context context, Class<?> type, String key, Object defValue) {
        // SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constats.PREFS_NAME, 0);

        Object ret = null;

        if (String.class.isAssignableFrom(type)) {
            ret = sharedPreferences.getString(key, (String) defValue);
        } else if (Integer.class.isAssignableFrom(type)) {
            ret = sharedPreferences.getInt(key, (Integer) defValue);
        } else if (Long.class.isAssignableFrom(type)) {
            ret = sharedPreferences.getLong(key, (Long) defValue);
        } else if (Float.class.isAssignableFrom(type)) {
            ret = sharedPreferences.getFloat(key, (Float) defValue);
        } else if (Boolean.class.isAssignableFrom(type)) {
            ret = sharedPreferences.getBoolean(key, (Boolean) defValue);
        }

        return ret;
    }

}
