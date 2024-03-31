package io.neoterm.api.apis;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Looper;
import android.util.JsonWriter;
import android.util.SparseArray;

import io.neoterm.api.NeoTermApiReceiver;
import io.neoterm.api.util.ResultReturner;
import io.neoterm.shared.logger.Logger;
import io.neoterm.shared.neoterm.NeoTermConstants;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.HashMap;

import androidx.annotation.NonNull;

public class UsbAPI {

    private static SparseArray<UsbDeviceConnection> openDevices = new SparseArray<>();

    private static final String LOG_TAG = "UsbAPI";

    public static void onReceive(final NeoTermApiReceiver apiReceiver, final Context context, final Intent intent) {
        Logger.logDebug(LOG_TAG, "onReceive");

        UsbDevice device;
        String action = intent.getAction();
        if (action == null) {
            ResultReturner.returnData(apiReceiver, intent, out -> out.append("Missing action\n"));
        } else {
            switch (action) {
                case "list":
                    ResultReturner.returnData(apiReceiver, intent, new ResultReturner.ResultJsonWriter() {
                        @Override
                        public void writeJson(JsonWriter out) throws Exception {
                            listDevices(context, out);
                        }
                    });
                    break;
                case "permission":
                    device = getDevice(apiReceiver, context, intent);
                    if (device == null) return;
                    ResultReturner.returnData(apiReceiver, intent, out -> {
                        boolean result = getPermission(device, context, intent);
                        out.append(result ? "yes\n" : "no\n");
                    });
                    break;
                case "open":
                    device = getDevice(apiReceiver, context, intent);
                    if (device == null) return;
                    ResultReturner.returnData(apiReceiver, intent, new ResultReturner.WithAncillaryFd() {
                                @Override
                                public void writeResult(PrintWriter out) {
                                    if (getPermission(device, context, intent)) {
                                        int result = open(device, context);
                                        if (result < 0) {
                                            out.append("Failed to open device\n");
                                        } else {
                                            this.sendFd(out, result);
                                        }
                                    } else out.append("No permission\n");
                                }
                            });

                    break;
                default:
                    ResultReturner.returnData(apiReceiver, intent, out -> out.append("Invalid action\n"));
            }
        }

    }

    private static void listDevices(final Context context, JsonWriter out) throws IOException {
        final UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<String> deviceIterator = deviceList.keySet().iterator();
        out.beginArray();
        while (deviceIterator.hasNext()) {
            out.value(deviceIterator.next());
        }
        out.endArray();
    }

    private static UsbDevice getDevice(final NeoTermApiReceiver apiReceiver, final Context context, final Intent intent) {
        String deviceName = intent.getStringExtra("device");
        final UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        UsbDevice device = deviceList.get(deviceName);
        if (device == null) {
            ResultReturner.returnData(apiReceiver, intent, out -> out.append("No such device\n"));
        }
        return device;
    }

    private static boolean hasPermission(final @NonNull UsbDevice device, final Context context) {
        final UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        return usbManager.hasPermission(device);
    }

    private static boolean requestPermission(final @NonNull UsbDevice device, final Context context) {
        Looper.prepare();
        Looper looper = Looper.myLooper();
        final boolean[] result = new boolean[1];

        final String ACTION_USB_PERMISSION = NeoTermConstants.NEOTERM_API_PACKAGE_NAME + ".USB_PERMISSION";
        final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context usbContext, final Intent usbIntent) {
                String action = usbIntent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        UsbDevice device = usbIntent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (usbIntent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (device != null) {
                                result[0] = true;
                                if (looper != null) looper.quit();
                            }
                        } else {
                            result[0] = false;
                            if (looper != null) looper.quit();
                        }
                    }

                }
            }
        };

        final UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.getApplicationContext().registerReceiver(usbReceiver, filter);
        usbManager.requestPermission(device, permissionIntent);
        Looper.loop();
        return result[0];
    }

    private static boolean getPermission(final @NonNull UsbDevice device, final Context context, final Intent intent) {
        boolean request = intent.getBooleanExtra("request", false);
        if(request) {
            return requestPermission(device, context);
        } else {
            return hasPermission(device, context);
        }
    }

    private static int open(final @NonNull UsbDevice device, final Context context) {
        final UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        UsbDeviceConnection connection = usbManager.openDevice(device);
        if (connection == null)
            return -2;
        int fd = connection.getFileDescriptor();
        if (fd == -1) {
            connection.close();
            return -1;
        }
        openDevices.put(fd, connection);
        return fd;
    }

}
