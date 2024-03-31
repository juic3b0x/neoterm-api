package io.neoterm.api.apis;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import io.neoterm.api.NeoTermApiReceiver;
import io.neoterm.api.util.ResultReturner;
import io.neoterm.shared.logger.Logger;

import java.io.File;

public class DownloadAPI {

    private static final String LOG_TAG = "DownloadAPI";

    public static void onReceive(NeoTermApiReceiver apiReceiver, final Context context, final Intent intent) {
        Logger.logDebug(LOG_TAG, "onReceive");

        ResultReturner.returnData(apiReceiver, intent, out -> {
            final Uri downloadUri = intent.getData();
            if (downloadUri == null) {
                out.println("No download URI specified");
                return;
            }

            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            String path = intent.getStringExtra("path");

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Request req = new Request(downloadUri);
            req.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setVisibleInDownloadsUi(true);

            if (title != null)
                req.setTitle(title);

            if (description != null)
                req.setDescription(description);

            if (path != null)
                req.setDestinationUri(Uri.fromFile(new File(path)));

            manager.enqueue(req);
        });
    }
}
