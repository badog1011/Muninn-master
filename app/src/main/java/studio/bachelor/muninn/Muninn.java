package studio.bachelor.muninn;

import android.app.Application;
import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;

/**
 * Created by BACHELOR on 2016/03/01.
 */
public class Muninn extends Application {
    private static Context context;
    private static SharedPreferences sharedPreferences;
    static public MediaPlayer soundPlayer;
    static public MediaPlayer sound_Punch;
    static public MediaPlayer sound_Ding;

    public void onCreate() {
        super.onCreate();
        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        soundPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
        sound_Punch = MediaPlayer.create(this,R.raw.punch);
        sound_Ding = MediaPlayer.create(this,R.raw.ding);
    }

    public static Context getContext() {
        return context;
    }

    public static float getSizeSetting(int key_id, int default_id) {
        String default_str = context.getResources().getString(default_id);
        String key_str = context.getResources().getString(key_id);
        String preference = sharedPreferences.getString(key_str, default_str);
        return Math.abs(Float.parseFloat(preference));
    }

    public static String getColorSetting(int key_id, int default_id) {
        String default_str = context.getResources().getString(default_id);
        String key_str = context.getResources().getString(key_id);
        String preference = sharedPreferences.getString(key_str, default_str);
        return preference;
    }

    public static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
