package io.neoterm.api.apis;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import io.neoterm.api.NeoTermApiReceiver;
import io.neoterm.api.util.ResultReturner;
import io.neoterm.shared.logger.Logger;

public class BrightnessAPI {

    private static final String LOG_TAG = "BrightnessAPI";

    public static void onReceive(final NeoTermApiReceiver receiver, final Context context, final Intent intent) {
        Logger.logDebug(LOG_TAG, "onReceive");

        final ContentResolver contentResolver = context.getContentResolver();
        if (intent.hasExtra("auto")) {
            boolean auto = intent.getBooleanExtra("auto", false);
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, auto?Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC:Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }

        int brightness = intent.getIntExtra("brightness", 0);

        if (brightness <= 0) {
            brightness = 0;
        } else if (brightness >= 255) {
            brightness = 255;
        }
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        ResultReturner.noteDone(receiver, intent);
    }
}
