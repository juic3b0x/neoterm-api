package io.neoterm.api;

import android.app.Application;
import android.content.Context;

import io.neoterm.api.util.ResultReturner;
import io.neoterm.shared.logger.Logger;
import io.neoterm.shared.neoterm.NeoTermConstants;
import io.neoterm.shared.neoterm.crash.NeoTermCrashUtils;
import io.neoterm.shared.neoterm.settings.preferences.NeoTermAPIAppSharedPreferences;


public class NeoTermAPIApplication extends Application {

    public void onCreate() {
        super.onCreate();

        // Set crash handler for the app
        NeoTermCrashUtils.setCrashHandler(this);
        ResultReturner.setContext(this);

        // Set log config for the app
        setLogConfig(getApplicationContext(), true);

        Logger.logDebug("Starting Application");

        SocketListener.createSocketListener(this);
    }

    public static void setLogConfig(Context context, boolean commitToFile) {
        Logger.setDefaultLogTag(NeoTermConstants.NEOTERM_API_APP_NAME.replaceAll(":", ""));

        // Load the log level from shared preferences and set it to the {@link Logger.CURRENT_LOG_LEVEL}
        NeoTermAPIAppSharedPreferences preferences = NeoTermAPIAppSharedPreferences.build(context);
        if (preferences == null) return;
        preferences.setLogLevel(null, preferences.getLogLevel(true), commitToFile);
    }

}
