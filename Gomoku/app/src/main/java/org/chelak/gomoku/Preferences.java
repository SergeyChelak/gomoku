package org.chelak.gomoku;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Sergey on 31.12.2015.
 */
public class Preferences {

    private final static String PREFERENCES_STORAGE_FILE = "gomoku.pref";

    private final static String PREFERENCE_SOUND = "pref.sound.state";

    private Context context;

    public Preferences(Context context) {
        super();
        this.context = context;
    }

    private SharedPreferences getPreferences() {
        return context.getSharedPreferences(PREFERENCES_STORAGE_FILE,
                Context.MODE_PRIVATE);
    }

    public void putLong(String key, long value) {
        SharedPreferences sharedPref = getPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public long getLong(String key, long defaultValue) {
        SharedPreferences sharedPref = getPreferences();
        return sharedPref.getLong(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences sharedPref = getPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences sharedPref = getPreferences();
        return sharedPref.getBoolean(key, defaultValue);
    }

    public void putDate(String key, Date value) {
        putLong(key, value.getTime());
    }

    public Date getDate(String key) {
        long value = getLong(key, 0);
        return new Date(value);
    }

    public boolean isSoundEnabled() {
        return getBoolean(PREFERENCE_SOUND, false);
    }

    public void setSoundEnabled(boolean isSoundEnabled) {
        putBoolean(PREFERENCE_SOUND, isSoundEnabled);
    }

}
