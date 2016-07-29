package com.example.razer.sample_1;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URISyntaxException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothManager mBluetoothManager = null;
    private Handler mHandler;
    private Handler setHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    private static DeviceTemp deviceTemp1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private Vibrator vibe;
    private String myPhoneNum;
    private View mainView;
    private boolean FinishFlag = false;
    private BackPressCloseHandler backPressCloseHandler;
    private LocationManager mLocationManager;
    private ImageButton stop_scan,start_scan,goback_login;
    private ImageView imageView2;

    private Socket socket;
    {
        try{
            socket = IO.socket("http://106.243.213.92:10/");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

//    @Override
//    public void finish() {
//        if ( FinishFlag == false){
//            Toast.makeText(this, "'뒤로'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
//            FinishFlag =  true;
//            mKillHandler.sendEmptyMessageDelayed(0, 2000);
//            return;
//        }
//        super.finish();
//    }

    Handler mKillHandler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0)
                FinishFlag = false;
        }
    };

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        if(event.getAction() == KeyEvent.ACTION_DOWN)
//        {
//            if (event.getAction() == KeyEvent.ACTION_UP) {
//                char keyDown = (char) event.getUnicodeChar();
//                Log.d("Android", keyDown + "");
//            }
//        }
//        else if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
//            Log.d("Android","Home키");
//        }
//
//        return true;
//    }

    @Override
    protected void onResume() {
        super.onResume();
        //환경설정 값 불러오기 예제
        //SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        //String text = pref.getString("editText", "");
        //Boolean chk1 = pref.getBoolean("check1", false);

        //환경설정 확인

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        UserSetting.setBeaconnoti(pref.getBoolean("notifications_new_message", false));
        UserSetting.setLocation(pref.getBoolean("notifications_location_info", false));


        final Intent a = new Intent(MainActivity.this, BackgroundRunning.class);
        if(isServiceRunningCheck()) {
            Log.d("서비스 종료","WWWWWWWWWWWWWWWWWWWWW");
            stopService(a);
            Toast.makeText(getApplicationContext(),"백그라운드 서비스를 중지합니다.",Toast.LENGTH_SHORT).show();
        }
        //삭제가능
//        if(UserSetting.getBeaconnoti()){Toast.makeText(getApplicationContext(),"비콘알림 체크됨",Toast.LENGTH_SHORT).show();}
//        else {Toast.makeText(getApplicationContext(),"비콘알림 체크해제됨",Toast.LENGTH_SHORT).show();}
//
//        if(UserSetting.getLocation()){Toast.makeText(getApplicationContext(),"위치정보 체크됨",Toast.LENGTH_SHORT).show();}
//        else {Toast.makeText(getApplicationContext(),"위치거리알림 체크해제됨",Toast.LENGTH_SHORT).show();}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = new Intent(MainActivity.this, BackgroundRunning.class);

        stop_scan = (ImageButton) findViewById(R.id.stop_scan);
        start_scan = (ImageButton) findViewById(R.id.scan_start);
        goback_login = (ImageButton) findViewById(R.id.login);
        imageView2 = (ImageView) findViewById(R.id.imageView2);

        backPressCloseHandler = new BackPressCloseHandler(this);
        //

        //인터넷 서비스 제공자로부터 gps 정보를 받아옵니다.

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.d("main", "provider : " + lm.NETWORK_PROVIDER);
        Log.d("main", "provider : " + LocationManager.NETWORK_PROVIDER);
        //Location을 가져오는데 주목해야 할 점은 getLastKnownLocation메서드의 인자인 Provider를 GPS가 아닌 NETWORK로 해야한다는것.
        try {
            Double companylat = 35.192481;
            Double compannyLoing = 129.078180;
            Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Double latitude = (location.getLatitude());
            Double longitude = (location.getLongitude());
            Log.d("main", "위도 : " + latitude + "\t 경도 : " + longitude);
            Toast.makeText(getApplicationContext(),"거리: "+ calcDistance(latitude,longitude,companylat,compannyLoing),Toast.LENGTH_SHORT).show();
        }
        catch (SecurityException e){
            Log.d("권한에러!!!","");
        }
        catch (NullPointerException e){
            Log.d("error!!!!!!!!!!!","");
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if(permissionCheck2== PackageManager.PERMISSION_DENIED){
            // 권한 없음
            Log.d("권한2 없어!!!!!!!!!!!!!!","권한 있음!");
        }else{
            // 권한 있음
            Log.d("권한2 있음!","권한 있음!");
        }

        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            // 권한 없음
            Log.d("권한 없어!!!!!!!!!!!!!!","권한 있음!");
        }else{
            // 권한 있음
            Log.d("권한 있음!","권한 있음!");
        }

        //위치기반 권한체크
        checkLo();

        //블루투스 권한체크
        checkBle();

        //전화번호 얻어오기
        getPhoneNum();

        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        start_scan.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent webview = new Intent(MainActivity.this, WebViewPage.class);
                startActivity(webview);
                return true;
            }
        });

        start_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunningCheck()) {
                    Toast.makeText(getApplication(), "서비스가 이미 실행중입니다", Toast.LENGTH_SHORT).show();
                } else {
                    startService(intent);
                }

                finish();
                //홈화면 진입
//                Intent home = new Intent();
//                home.setAction(Intent.ACTION_MAIN);
//                home.addCategory(Intent.CATEGORY_HOME);
//                startActivity(home);
            }
        });

        stop_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Snackbar.make(v, "종료합니다..", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                vibe.vibrate(150);
//              Toast.makeText(getApplicationContext(),"백그라운드 서비스를 종료합니다",Toast.LENGTH_SHORT).show();
                stopService(intent);
                UserInfo.setMailAddress("");
                UserInfo.setPwd("");
                UserInfo.setUserName("");

                socket.disconnect();
                finish();
            }
        });


        goback_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(150);
                stopService(intent);
                socket.disconnect();
                UserInfo.setMailAddress("");
                UserInfo.setPwd("");
                UserInfo.setUserName("");
                Intent b = new Intent(MainActivity.this, loginPage.class);
                startActivity(b);
                finish();
            }
        });

        //테스트 함수
       goback_login.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                stopService(intent);
                socket.disconnect();
                Intent Lo = new Intent(MainActivity.this, BackgroundLoCheck.class);
                startService(Lo);
                finish();

                return false;
            }
        });

    }

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = null;
            try {
                l = mLocationManager.getLastKnownLocation(provider);
            }
            catch (SecurityException e){
                Log.d("옵션에러!!!!","권한이 필요합니다!");
            }
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }
        }
    }

    //거리 구하는 공식
    public static String calcDistance(double lat1, double lon1, double lat2, double lon2){
        double EARTH_R, Rad, radLat1, radLat2, radDist;
        double distance, ret;

        EARTH_R = 6371000.0;
        Rad = Math.PI/180;
        radLat1 = Rad * lat1;
        radLat2 = Rad * lat2;
        radDist = Rad * (lon1 - lon2);

        distance = Math.sin(radLat1) * Math.sin(radLat2);
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist);
        ret = EARTH_R * Math.acos(distance);

        double rslt = Math.round(Math.round(ret) / 1000);
        String result = rslt + " km";
        if(rslt == 0) result = Math.round(ret) +" m";

        return result;
    }

    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.razer.sample_1.BackgroundRunning".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void checkLo() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                int permissionCheck = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    public void getPhoneNum(){

        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneNumber = tm.getLine1Number();
//            if(mPhoneNumber.length() < 3){
//                Toast.makeText(getApplicationContext(),"내 전화번호:" + mPhoneNumber, Toast.LENGTH_SHORT).show();
//            }
//            else{
//                Toast.makeText(getApplicationContext(),"USIM 없음", Toast.LENGTH_SHORT).show();
//            }

        }
        catch (SecurityException e){
            Log.d("권한에러입니다","");
        }
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }


    public void checkBle(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            //장치가 블루투스를 지원하지 않는 경우.
            Toast.makeText(MainActivity.this, "블루투스 지원불가 기기", Toast.LENGTH_SHORT).show();
            finish();
        }

        else {
            //블루투스가 비활성인 경우 블루투스 켜기
            if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON ||
                    mBluetoothAdapter.getState() == mBluetoothAdapter.STATE_ON){
                //mBluetoothAdapter.disable();
            }
            else {
                mBluetoothAdapter.enable();
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notice) {
            Intent noti = new Intent(MainActivity.this, noticePage.class);
            startActivity(noti);
            return true;
        }

/*        if (id == R.id.member_leave) {
            Intent leave = new Intent(MainActivity.this, MemberLeave.class);
            startActivity(leave);
            return true;
        }*/

//        if (id == R.id.view_map) {
//            Intent map = new Intent(MainActivity.this, MapTest.class);
//            startActivity(map);
//            return true;
//        }

        if (id == R.id.user_option) {
            Intent opt = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(opt);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
