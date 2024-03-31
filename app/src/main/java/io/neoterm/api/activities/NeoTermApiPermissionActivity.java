package io.neoterm.api.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.JsonWriter;

import io.neoterm.api.util.ResultReturner;
import io.neoterm.shared.android.PermissionUtils;
import io.neoterm.shared.logger.Logger;
import io.neoterm.shared.neoterm.NeoTermConstants;

import java.util.ArrayList;

public class NeoTermApiPermissionActivity extends Activity {

    private static final String LOG_TAG = "NeoTermApiPermissionActivity";

    /**
     * Intent extra containing the permissions to request.
     */
    public static final String PERMISSIONS_EXTRA = NeoTermConstants.NEOTERM_API_PACKAGE_NAME + ".permission_extra";

    /**
     * Check for and request permissions if necessary.
     *
     * @return if all permissions were already granted
     */
    public static boolean checkAndRequestPermissions(Context context, Intent intent, String... permissions) {
        final ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (!PermissionUtils.checkPermission(context, permission)) {
                permissionsToRequest.add(permission);
            }
        }

        if (permissionsToRequest.isEmpty()) {
            return true;
        } else {
            ResultReturner.returnData(context, intent, new ResultReturner.ResultJsonWriter() {
                @Override
                public void writeJson(JsonWriter out) throws Exception {
                    String errorMessage = "Please grant the following permission"
                            + (permissionsToRequest.size() > 1 ? "s" : "")
                            + " to use this command: "
                            + TextUtils.join(" ,", permissionsToRequest);
                    out.beginObject().name("error").value(errorMessage).endObject();
                }
            });

            Intent startIntent = new Intent(context, NeoTermApiPermissionActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putStringArrayListExtra(NeoTermApiPermissionActivity.PERMISSIONS_EXTRA, permissionsToRequest);
            ResultReturner.copyIntentExtras(intent, startIntent);
            context.startActivity(startIntent);
            return false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Logger.logDebug(LOG_TAG, "onNewIntent");

        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        Logger.logVerbose(LOG_TAG, "onResume");

        super.onResume();
        ArrayList<String> permissionValues = getIntent().getStringArrayListExtra(PERMISSIONS_EXTRA);
        PermissionUtils.requestPermissions(this, permissionValues.toArray(new String[0]), 0);
        finish();
    }

}
