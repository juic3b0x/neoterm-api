package io.neoterm.api;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

import io.neoterm.api.apis.AudioAPI;
import io.neoterm.api.apis.BatteryStatusAPI;
import io.neoterm.api.apis.BrightnessAPI;
import io.neoterm.api.apis.CallLogAPI;
import io.neoterm.api.apis.CameraInfoAPI;
import io.neoterm.api.apis.CameraPhotoAPI;
import io.neoterm.api.apis.ClipboardAPI;
import io.neoterm.api.apis.ContactListAPI;
import io.neoterm.api.apis.DialogAPI;
import io.neoterm.api.apis.DownloadAPI;
import io.neoterm.api.apis.FingerprintAPI;
import io.neoterm.api.apis.InfraredAPI;
import io.neoterm.api.apis.JobSchedulerAPI;
import io.neoterm.api.apis.KeystoreAPI;
import io.neoterm.api.apis.LocationAPI;
import io.neoterm.api.apis.MediaPlayerAPI;
import io.neoterm.api.apis.MediaScannerAPI;
import io.neoterm.api.apis.MicRecorderAPI;
import io.neoterm.api.apis.NfcAPI;
import io.neoterm.api.apis.NotificationAPI;
import io.neoterm.api.apis.NotificationListAPI;
import io.neoterm.api.apis.SAFAPI;
import io.neoterm.api.apis.SensorAPI;
import io.neoterm.api.apis.ShareAPI;
import io.neoterm.api.apis.SmsInboxAPI;
import io.neoterm.api.apis.SmsSendAPI;
import io.neoterm.api.apis.SpeechToTextAPI;
import io.neoterm.api.apis.StorageGetAPI;
import io.neoterm.api.apis.TelephonyAPI;
import io.neoterm.api.apis.TextToSpeechAPI;
import io.neoterm.api.apis.ToastAPI;
import io.neoterm.api.apis.TorchAPI;
import io.neoterm.api.apis.UsbAPI;
import io.neoterm.api.apis.VibrateAPI;
import io.neoterm.api.apis.VolumeAPI;
import io.neoterm.api.apis.WallpaperAPI;
import io.neoterm.api.apis.WifiAPI;
import io.neoterm.api.activities.NeoTermApiPermissionActivity;
import io.neoterm.api.util.ResultReturner;
import io.neoterm.shared.data.IntentUtils;
import io.neoterm.shared.logger.Logger;
import io.neoterm.shared.neoterm.NeoTermConstants;
import io.neoterm.shared.neoterm.plugins.NeoTermPluginUtils;

public class NeoTermApiReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "NeoTermApiReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        NeoTermAPIApplication.setLogConfig(context, false);
        Logger.logDebug(LOG_TAG, "Intent Received:\n" + IntentUtils.getIntentString(intent));

        try {
            doWork(context, intent);
        } catch (Throwable t) {
            String message = "Error in " + LOG_TAG;
            // Make sure never to throw exception from BroadCastReceiver to avoid "process is bad"
            // behaviour from the Android system.
            Logger.logStackTraceWithMessage(LOG_TAG, message, t);

            NeoTermPluginUtils.sendPluginCommandErrorNotification(context, LOG_TAG,
                    NeoTermConstants.NEOTERM_API_APP_NAME + " Error", message, t);

            ResultReturner.noteDone(this, intent);
        }
    }

    private void doWork(Context context, Intent intent) {
        String apiMethod = intent.getStringExtra("api_method");
        if (apiMethod == null) {
            Logger.logError(LOG_TAG, "Missing 'api_method' extra");
            return;
        }

        switch (apiMethod) {
            case "AudioInfo":
                AudioAPI.onReceive(this, context, intent);
                break;
            case "BatteryStatus":
                BatteryStatusAPI.onReceive(this, context, intent);
                break;
            case "Brightness":
                if (!Settings.System.canWrite(context)) {
                    NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.WRITE_SETTINGS);
                    Toast.makeText(context, "Please enable permission for NeoTerm:API", Toast.LENGTH_LONG).show();

                    // user must enable WRITE_SETTINGS permission this special way
                    Intent settingsIntent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    context.startActivity(settingsIntent);
                    return;
                }
                BrightnessAPI.onReceive(this, context, intent);
                break;
            case "CameraInfo":
                CameraInfoAPI.onReceive(this, context, intent);
                break;
            case "CameraPhoto":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.CAMERA)) {
                    CameraPhotoAPI.onReceive(this, context, intent);
                }
                break;
            case "CallLog":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.READ_CALL_LOG)) {
                    CallLogAPI.onReceive(context, intent);
                }
                break;
            case "Clipboard":
                ClipboardAPI.onReceive(this, context, intent);
                break;
            case "ContactList":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.READ_CONTACTS)) {
                    ContactListAPI.onReceive(this, context, intent);
                }
                break;
            case "Dialog":
                DialogAPI.onReceive(context, intent);
                break;
            case "Download":
                DownloadAPI.onReceive(this, context, intent);
                break;
            case "Fingerprint":
                FingerprintAPI.onReceive(context, intent);
                break;
            case "InfraredFrequencies":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.TRANSMIT_IR)) {
                    InfraredAPI.onReceiveCarrierFrequency(this, context, intent);
                }
                break;
            case "InfraredTransmit":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.TRANSMIT_IR)) {
                    InfraredAPI.onReceiveTransmit(this, context, intent);
                }
                break;
            case "JobScheduler":
                JobSchedulerAPI.onReceive(this, context, intent);
                break;
            case "Keystore":
                KeystoreAPI.onReceive(this, intent);
                break;
            case "Location":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    LocationAPI.onReceive(this, context, intent);
                }
                break;
            case "MediaPlayer":
                MediaPlayerAPI.onReceive(context, intent);
                break;
            case "MediaScanner":
                MediaScannerAPI.onReceive(this, context, intent);
                break;
            case "MicRecorder":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.RECORD_AUDIO)) {
                    MicRecorderAPI.onReceive(context, intent);
                }
                break;
            case "Nfc":
                NfcAPI.onReceive(context, intent);
                break;
            case "NotificationList":
                ComponentName cn = new ComponentName(context, NotificationListAPI.NotificationService.class);
                String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
                final boolean NotificationServiceEnabled = flat != null && flat.contains(cn.flattenToString());
                if (!NotificationServiceEnabled) {
                    Toast.makeText(context,"Please give NeoTerm:API Notification Access", Toast.LENGTH_LONG).show();
                    context.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                    NotificationListAPI.onReceive(this, context, intent);
                }
                break;
            case "Notification":
                NotificationAPI.onReceiveShowNotification(this, context, intent);
                break;
            case "NotificationChannel":
                NotificationAPI.onReceiveChannel(this, context, intent);
                break;
            case "NotificationRemove":
                NotificationAPI.onReceiveRemoveNotification(this, context, intent);
                break;
            case "NotificationReply":
                NotificationAPI.onReceiveReplyToNotification(this, context, intent);
                break;
            case "SAF":
                SAFAPI.onReceive(this, context, intent);
                break;
            case "Sensor":
                SensorAPI.onReceive(context, intent);
                break;
            case "Share":
                ShareAPI.onReceive(this, context, intent);
                break;
            case "SmsInbox":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS)) {
                    SmsInboxAPI.onReceive(this, context, intent);
                }
                break;
            case "SmsSend":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS)) {
                    SmsSendAPI.onReceive(this, context, intent);
                }
                break;
            case "StorageGet":
                StorageGetAPI.onReceive(this, context, intent);
                break;
            case "SpeechToText":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.RECORD_AUDIO)) {
                    SpeechToTextAPI.onReceive(context, intent);
                }
                break;
            case "TelephonyCall":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.CALL_PHONE)) {
                    TelephonyAPI.onReceiveTelephonyCall(this, context, intent);
                }
                break;
            case "TelephonyCellInfo":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    TelephonyAPI.onReceiveTelephonyCellInfo(this, context, intent);
                }
                break;
            case "TelephonyDeviceInfo":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.READ_PHONE_STATE)) {
                    TelephonyAPI.onReceiveTelephonyDeviceInfo(this, context, intent);
                }
                break;
            case "TextToSpeech":
                TextToSpeechAPI.onReceive(context, intent);
                break;
            case "Toast":
                ToastAPI.onReceive(context, intent);
                break;
            case "Torch":
                TorchAPI.onReceive(this, context, intent);
                break;
            case "Usb":
                UsbAPI.onReceive(this, context, intent);
                break;
            case "Vibrate":
                VibrateAPI.onReceive(this, context, intent);
                break;
            case "Volume":
                VolumeAPI.onReceive(this, context, intent);
                break;
            case "Wallpaper":
                WallpaperAPI.onReceive(context, intent);
                break;
            case "WifiConnectionInfo":
                WifiAPI.onReceiveWifiConnectionInfo(this, context, intent);
                break;
            case "WifiScanInfo":
                if (NeoTermApiPermissionActivity.checkAndRequestPermissions(context, intent, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    WifiAPI.onReceiveWifiScanInfo(this, context, intent);
                }
                break;
            case "WifiEnable":
                WifiAPI.onReceiveWifiEnable(this, context, intent);
                break;
            default:
                Logger.logError(LOG_TAG, "Unrecognized 'api_method' extra: '" + apiMethod + "'");
        }
    }

}
