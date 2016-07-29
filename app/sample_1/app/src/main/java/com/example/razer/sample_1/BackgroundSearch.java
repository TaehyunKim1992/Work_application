package com.example.razer.sample_1;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BackgroundSearch extends Service {
    private Handler mHandler;
    private Handler scanHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private boolean mScanning;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 2000;
    public static String UUID = "";
    public static int MAJOR;
    public static int MINOR;
    public static int TXPOWER;
    public static DeviceTemp deviceTemp1;

    public BackgroundSearch() {
        //최초 한번실행???
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        //scanHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scanLeDevice(true);

        return super.onStartCommand(intent, flags, startId);
    }

    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    private void scanLeDevice(final boolean enable) {
        deviceTemp1.setUUID("");
        deviceTemp1.setMajorNum(9999);
        deviceTemp1.setMinorNum(9999);
        deviceTemp1.setDeviceName("");
        deviceTemp1.setDistance(0);

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //onDestroy();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }


    protected static double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine distance, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    int startByte = 2;
                    boolean patternFound = false;
                    while (startByte <= 5) {
                        if (    ((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                                ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                            patternFound = true;
                            break;
                        }
                        startByte++;
                    }
                    if (patternFound) {
                        //Convert to hex String
                        byte[] uuidBytes = new byte[16];
                        System.arraycopy(scanRecord, startByte+4, uuidBytes, 0, 16);
                        String hexString = bytesToHex(uuidBytes);

                        UUID =  hexString.substring(0,8) + "-" +
                                hexString.substring(8,12) + "-" +
                                hexString.substring(12,16) + "-" +
                                hexString.substring(16,20) + "-" +
                                hexString.substring(20,32);

                        MAJOR = (scanRecord[startByte+20] & 0xff) * 0x100 + (scanRecord[startByte+21] & 0xff);

                        MINOR = (scanRecord[startByte+22] & 0xff) * 0x100 + (scanRecord[startByte+23] & 0xff);

                        TXPOWER = (scanRecord[startByte+24]);

//                        Log.d("=====================","===================");
//                        Log.d("디바이스 네임:  ",device.getName());
//                        Log.d("TXPOWER:  " + TXPOWER, "    ");
//                        Log.d("MAJOR:  "+MAJOR,"    MINOR:  "+MINOR);
//                        Log.d("UUID:  "+ UUID,"    신호세기:  "+rssi);
//                        Log.d("=====================","===================");


                        deviceTemp1.setUUID(UUID);
                        deviceTemp1.setMajorNum(MAJOR);
                        deviceTemp1.setMinorNum(MINOR);
                        deviceTemp1.setDeviceName(device.getName());
                        deviceTemp1.setDistance(calculateDistance(TXPOWER,rssi));
                    }
                }
            };
}
