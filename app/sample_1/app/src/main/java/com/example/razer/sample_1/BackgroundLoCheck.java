package com.example.razer.sample_1;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;

public class BackgroundLoCheck extends Service {
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private Double myLat = 0.0;
    private Double myLng = 0.0;
    private Boolean count = false;

    public BackgroundLoCheck() {
        checkBle();
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                testLo();
                mHandler.sendEmptyMessageDelayed(3000, 8000);
            }
        };
        mHandler.sendEmptyMessage(1);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void checkBle() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //장치가 블루투스를 지원하지 않는 경우.

        } else {
            //블루투스가 비활성인 경우 블루투스 켜기
            if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON ||
                    mBluetoothAdapter.getState() == mBluetoothAdapter.STATE_ON) {
                //mBluetoothAdapter.disable();
            } else {
                mBluetoothAdapter.enable();
            }
        }
    }

    public void testLo() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.d("main", "provider : " + lm.NETWORK_PROVIDER);
        Log.d("main", "provider : " + LocationManager.NETWORK_PROVIDER);
        //Location을 가져오는데 주목해야 할 점은 getLastKnownLocation메서드의 인자인 Provider를 GPS가 아닌 NETWORK로 해야한다는것.
        try {
            Double companylat = 35.192481;
            Double compannyLoing = 129.078180;
            Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Double latitude = 0.0;
            Double longitude = 0.0;;
            try {
                latitude = (location.getLatitude());
                longitude = (location.getLongitude());
            }
            catch (NullPointerException e){
                Log.d("NullPointer Error!!!","로케이션 모듈 불러옥리 에러");
            }
            Log.d("main", "위도 : " + latitude + "\t 경도 : " + longitude);
            Toast.makeText(getApplicationContext(),"거리: "+ calcDistance(latitude,longitude,companylat,compannyLoing) + "m 떨어져있습니다.",Toast.LENGTH_SHORT).show();
            if (calcDistance(latitude, longitude, companylat, compannyLoing) < 100 && count == false) {
                Toast.makeText(getApplicationContext(),"회사근처네요! 앱을 실행합니다",Toast.LENGTH_SHORT).show();
                checkBle();
                count = true;
                Intent cbc = new Intent(getApplicationContext(),WebViewPage.class);
                cbc.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(cbc);
            }
        } catch (SecurityException e) {
            Log.d("권한에러!!!", "");
        }
    }

//    원본 계산함수 ( String 반환)
//    public static String calcDistance(double lat1, double lon1, double lat2, double lon2){
//        double EARTH_R, Rad, radLat1, radLat2, radDist;
//        double distance, ret;
//
//        EARTH_R = 6371000.0;
//        Rad = Math.PI/180;
//        radLat1 = Rad * lat1;
//        radLat2 = Rad * lat2;
//        radDist = Rad * (lon1 - lon2);
//
//        distance = Math.sin(radLat1) * Math.sin(radLat2);
//        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist);
//        ret = EARTH_R * Math.acos(distance);
//
//        double rslt = Math.round(Math.round(ret) / 1000);
//        String result = rslt + " km";
//        if(rslt == 0) result = Math.round(ret) +" m";
//
//        return result;
//    }

//    원본 계산함수 ( 숫자(m단위) 반환)

    public static Double calcDistance(double lat1, double lon1, double lat2, double lon2) {
        double EARTH_R, Rad, radLat1, radLat2, radDist;
        double distance, ret;

        EARTH_R = 6371000.0;
        Rad = Math.PI / 180;
        radLat1 = Rad * lat1;
        radLat2 = Rad * lat2;
        radDist = Rad * (lon1 - lon2);

        distance = Math.sin(radLat1) * Math.sin(radLat2);
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist);
        ret = EARTH_R * Math.acos(distance);

        double result = Math.round(Math.round(ret) / 1000);
        result = Math.round(ret);

        return result;

    }

}
