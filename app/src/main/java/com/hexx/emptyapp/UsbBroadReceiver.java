package com.hexx.emptyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import static com.hexx.emptyapp.MainActivity.ACTION_USB_PERMISSION;
import static com.hexx.emptyapp.MainActivity.mTag;

/**
 * Created by Hexx on 2019-04-30 12:23
 * Desc：
 */
public class UsbBroadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.e(mTag, "接收到广播");
//        String action = intent.getAction();
//        switch (action) {
//            case ACTION_USB_PERMISSION:
//                synchronized (this) {
//                    Log.e(mTag, "自定义广播");
//                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                    Log.e(mTag, "获取设备信息");
//                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                        Log.e(mTag, "获取设备信息成功");
//                        if (usbDevice != null) {
//                            Log.e(mTag, "设备信息" + usbDevice.toString());
//                        }
//                    } else {
//                        Log.e(mTag, "获取设备信息被拒绝");
//                    }
//                }
//                break;
//            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
//                Log.e(mTag, "设备接入");
//                break;
//            case UsbManager.ACTION_USB_DEVICE_DETACHED:
//                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                if (usbDevice != null) {
//                    Log.e(mTag, "设备移除");
//                }
//                break;
//            default:
//                Log.e(mTag, "未知action" + action);
//                break;
//        }
    }
}
