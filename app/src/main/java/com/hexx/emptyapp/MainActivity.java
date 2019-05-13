package com.hexx.emptyapp;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.mtp.MtpConstants;
import android.mtp.MtpDevice;
import android.mtp.MtpObjectInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    public static final String ACTION_USB_PERMISSION = "com.hexx.emptyapp.USB_PERMISSION";
    public static final String mTag = "otg_device";
    private UsbManager mUsbManager;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(mTag, "接收到广播");
            String action = intent.getAction();
            switch (action) {
                case ACTION_USB_PERMISSION:
                    synchronized (this) {
                        Log.e(mTag, "自定义广播");
                        UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        Log.e(mTag, "获取设备信息");
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            Log.e(mTag, "获取设备信息成功");
                            if (usbDevice != null) {
                                Log.e(mTag, "设备信息" + usbDevice.toString());
                                readFiles(usbDevice);
                            }
                        } else {
                            Log.e(mTag, "获取设备信息被拒绝");
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    Log.e(mTag, "设备接入");
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (usbDevice != null) {
                        Log.e(mTag, "设备移除");
                    }
                    break;
                default:
                    Log.e(mTag, "未知action" + action);
                    break;
            }
        }
    };
    private RecyclerView mRecycle;
    private ImageAdapter mImageAdapter;
    private List<Bitmap> mImageList = new ArrayList<>();
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mBroadcastReceiver, filter);
        Log.e(mTag, "已注册广播");
        mUsbManager = (UsbManager) getSystemService(USB_SERVICE);
        mRecycle = findViewById(R.id.rv);
        final TextView tv = findViewById(R.id.tv);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                tv.setText(msg.obj.toString());
            }
        };

        mRecycle.setLayoutManager(new GridLayoutManager(this, 4, LinearLayoutManager.VERTICAL, false));
        mImageAdapter = new ImageAdapter(this, new ArrayList<Bitmap>());
        mRecycle.setAdapter(mImageAdapter);

        findViewById(R.id.btn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        run();
                    }
                });
    }

    private void run() {
        for (UsbDevice device : mUsbManager.getDeviceList().values()) {
            if (mUsbManager.hasPermission(device)) {
                Log.e(mTag, "初始化时有权限");
                Log.e(mTag, device.toString());
                readFiles(device);
            } else {
                Log.e(mTag, "初始化时无权限,获取权限");
                PendingIntent intent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                mUsbManager.requestPermission(device, intent);
            }
        }


    }

    private void readFiles(UsbDevice usbDevice) {
        Log.e(mTag, "开始读取设备文件");
        UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);
        MtpDevice mtpDevice = new MtpDevice(usbDevice);
        try {
            if (mtpDevice.open(connection)) {
                Log.e(mTag, "连接成功");
                search(mtpDevice);
            } else {
                Log.e(mTag, "连接失败，无法读取");
            }
        } catch (Exception e) {
            Log.e(mTag, e.getMessage());
        }
    }


    @SuppressLint("CheckResult")
    private void search(final MtpDevice mtpDevice) {
        for (Bitmap bitmap : mImageList) {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        mImageList.clear();
        Observable.just(mtpDevice)
                .subscribeOn(Schedulers.computation())
                .map(new Function<MtpDevice, int[]>() {

                    @Override
                    public int[] apply(MtpDevice mtpDevice) {
                        Log.e(mTag, "获取handler");
                        int[] storageIds = mtpDevice.getStorageIds();
                        if (storageIds != null) {
                            Log.e(mTag, "ids not null size:" + storageIds.length);
                            int[] objectHandles = mtpDevice.getObjectHandles(storageIds[0], MtpConstants.FORMAT_EXIF_JPEG, 0);
                            if (objectHandles == null) {
                                Log.e(mTag, "handler is null");
                                return new int[0];
                            } else {
                                Log.e(mTag, "handler not null size:" + storageIds.length);
                                return objectHandles;
                            }
                        } else {
                            Log.e(mTag, "ids is null");
                            return new int[0];
                        }

                    }
                })
                .map(new Function<int[], List<Bitmap>>() {
                    @Override
                    public List<Bitmap> apply(int[] ints) {
                        Log.e(mTag, "map handler size:" + ints.length);
                        for (int i = 0; i < ints.length; i++) {
                            Log.e(mTag, "遍历图片 第" + i);
                            MtpObjectInfo objectInfo = mtpDevice.getObjectInfo(ints[i]);
//                            byte[] object = mtpDevice.getObject(ints[i], objectInfo.getCompressedSize());
//                            if (object != null) {
//                                Log.e(mTag, "图片复制到内存 position:" + i);
//                                BitmapFactory.Options options = new BitmapFactory.Options();
//                                options.inSampleSize = 16;
//                                options.inPreferredConfig = Bitmap.Config.RGB_565;
//                                mImageList.add(BitmapFactory.decodeByteArray(object, 0, object.length, options));
//                            }

                            byte[] thumbnail = mtpDevice.getThumbnail(ints[i]);
                            if (thumbnail != null) {
                                mImageList.add(BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length));
                            }
                            Message message = new Message();
                            message.obj = String.format(Locale.CHINA, "%d/%d", i + 1, ints.length);
                            mHandler.sendMessage(message);

//                            if (i == 10) {
//                                break;
//                            }
                        }
                        mtpDevice.close();
                        return mImageList;
                    }
                })

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Bitmap>>() {
                    @Override
                    public void accept(List<Bitmap> bitmaps) throws Exception {
                        Log.e(mTag, "结束 刷新adapter");
                        mImageAdapter.setBitmaps(bitmaps);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(mTag, throwable.getMessage());
                    }
                });


    }

    public void save2File(Bitmap bitmap) {
        File publicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File file = new File(publicDirectory, SystemClock.currentThreadTimeMillis() + ".jpeg");
        try {
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    /**
     *1.      int[] storageIds = mtpDevice.getStorageIds();
     *        int storageId = storageIds[i].for;
     *2.      int[] objectHandles = mtpDevice.getObjectHandles(storageId, 0, 0);
     *        int objectHandle = objectHandles[i].for;
     *3.      MtpObjectInfo objectInfo = mtpDevice.getObjectInfo(objectHandle);
     *        if (objectInfo.getFormat() == MtpConstants.FORMAT_EXIF_JPEG) {
     *            byte[] bytes = mtpDevice.getObject(objectHandle, objectInfo.getCompressedSize());
     *            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
     *        }
     */


}
