package com.example.razer.sample_1;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class BackgroundRunning extends Service {
    private BackgroundSearch bs;
    private Handler m ,h, p;
    private static Vibrator vibe;
    private Socket socket;
    private SharedPreferences pref;
    private static String name = UserInfo.getUserName();
    private static String email = UserInfo.getMailAddress();
//    private static String UUID = "";
    //private WifiManager.WifiLock wifiLock = null;

    {
        try{
            socket = IO.socket("http://106.243.213.92:10/");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }


    public BackgroundRunning() {
        socket.on("mylocation", loc);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        h.removeCallbacksAndMessages(null);
        PushWakePock.releaseCpuLock();

//        if (wifiLock != null) {
//            wifiLock.release();
//            wifiLock = null;
//        }
//
//        vibe.vibrate(1000);
//        Toast.makeText(getApplicationContext(),"서비스 종료됨!",Toast.LENGTH_SHORT).show();

        super.onDestroy();
    }

        public void find(){
            final Intent intent13 = new Intent(getApplicationContext(), BackgroundSearch.class);
            if (isServiceRunningCheck()) {
                Toast.makeText(getApplicationContext(), "이미 서비스가 실행 중 입니다.", Toast.LENGTH_SHORT).show();
            } else {
                startService(intent13);
            m = new Handler();
            m.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopService(intent13);
                }
            }, 10000);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //find();
        //check();
          startForeground(1, new Notification());


//        name = UserInfo.getUserName();
//        email = UserInfo.getMailAddress();
//        UUID = DeviceTemp.getUUID();

          PushWakePock.onlyCpuWakeLock(getApplicationContext());

//        //WIFI LOCK
//        if (wifiLock == null) {
//            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
//            wifiLock = wifiManager.createWifiLock("wifilock");
//            wifiLock.setReferenceCounted(true);
//            wifiLock.acquire();
//        }

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        UserSetting.setScreenon(pref.getBoolean("notifications_new_message_screen", false));
        UserSetting.setLocation(pref.getBoolean("notifications_new_message_vibrate", false));

        vibe = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
        h =  new Handler(){
            public void handleMessage(Message msg){
                super.handleMessage(msg);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                find();
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socket.connect();
                sendServerMessage();


                try {
                    Thread.sleep(2000);
                    socket.disconnect();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //onBtnNotification("");
                h.sendEmptyMessageDelayed(3000, 60000);

            }
        };
        h.sendEmptyMessage(1);

        return super.onStartCommand(intent, flags, startId);
    }

    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.razer.sample_1.BackgroundSearch".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //noti같은 경우 장소가 바뀌는 경우에만 다시 띄우기.
    public void onBtnNotification(String s) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        if(s.length() > 1){
            // 작은 아이콘 이미지.
            builder.setSmallIcon(R.mipmap.ic_launcher);
            // 알림이 출력될 때 상단에 나오는 문구.
            builder.setTicker("* ["+ s + "] 장소 감지됨");
            // 알림 출력 시간.
            builder.setWhen(System.currentTimeMillis());
            // 알림 제목.
            builder.setContentTitle("* ["+ s + "]" + " 장소 감지됨");
            // 알림 내용.
            builder.setContentText("TEST APP 가동중!");
            // 알림 터치시 반응.
            builder.setContentIntent(pendingIntent);
            // 알림 터치시 반응 후 알림 삭제 여부.
            builder.setAutoCancel(true);
            // 고유ID로 알림을 생성.
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(123456, builder.build());
        }
//        else{
//            // 작은 아이콘 이미지.
//            builder.setSmallIcon(R.mipmap.ic_launcher);
//            // 알림이 출력될 때 상단에 나오는 문구.
//            builder.setTicker("백그라운드 실행중.");
//            // 알림 출력 시간.
//            builder.setWhen(System.currentTimeMillis());
//            // 알림 제목.
//            builder.setContentTitle("TEST APP 가동중!");
//            // 알림 내용.
//            builder.setContentText("---");
//            // 알림 터치시 반응.
//            builder.setContentIntent(pendingIntent);
//            // 알림 터치시 반응 후 알림 삭제 여부.
//            builder.setAutoCancel(true);
//            // 고유ID로 알림을 생성.
//            NotificationManager nm2 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            nm2.notify(789123, builder.build());
//        }
    }

    private void sendServerMessage(){
        JSONObject sendMessage = new JSONObject();
        Log.d("================","================");
        Log.d("4번로그",DeviceTemp.getTEMPEMAIL() + "");
        Log.d(sendMessage+"",DeviceTemp.getUUID() + "");
        Log.d(sendMessage + "", DeviceTemp.getMajorNum()+"");
        Log.d(sendMessage+"",DeviceTemp.getMinorNum()+"");
        Log.d("================","================");
        if(DeviceTemp.getUUID().length() > 1 && UserInfo.getMailAddress().length() > 5) {
        }//  비콘(UUID)이 검색된 경우에만 서버 전송!
        else{
            Toast.makeText(getApplicationContext(),"email이 비어있습니다.",Toast.LENGTH_SHORT).show();
        }
        try {
            Log.d("보넀다고!!!!!!!!!!!", "보냈어!!!!!!!!!!!");
            //sendMessage.put("username", name);
            sendMessage.put("email", DeviceTemp.getTEMPEMAIL());
            sendMessage.put("uuid", DeviceTemp.getUUID());
            sendMessage.put("major", DeviceTemp.getMajorNum());
            sendMessage.put("minor", DeviceTemp.getMinorNum());
            socket.emit("userlocation", sendMessage);
        } catch (JSONException e) {

        }

    }

    private Emitter.Listener loc = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            p = new Handler(Looper.getMainLooper());
            p.postDelayed(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject)args[0];

                    String locname;
                    int type;
                    try{
                        locname = data.getString("loc").toString();
                        type = data.getInt("type");
                    }
                    catch (JSONException e){
                        locname = "감지안됨";
                        type = 0;
                    }

                    //Toast.makeText(getApplicationContext(),locname+" 거리:"+Math.round(deviceTemp1.getDistance()*100)/1000.0+"m",Toast.LENGTH_SHORT).show();

                    //onBtnNotification(locname);
                    if(type == 1 && UserSetting.getBeaconnoti()){
                        Intent intent = new Intent(getApplicationContext(),WebViewPage.class);
                        //스크린온
                        if(UserSetting.getScreenon() && UserSetting.getBeaconnoti()) {
                            PushWakePock.acquireCpuWakeLock(getApplicationContext());
                            PushWakePock.releaseCpuLock();
                        }
                        //진동
                        if(UserSetting.getVibrate() && UserSetting.getBeaconnoti()) {
                            vibe.vibrate(300);
                        }
                        onBtnNotification("**** 감지됨 ****");
                        //다른곳에서 Intent 하려면 필요
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        intent.putExtra("url", locname);
                        startActivity(intent);
                    }
                }
            }, 0);
        }
    };

    private Emitter.Listener event = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            p = new Handler(Looper.getMainLooper());
            p.postDelayed(new Runnable() {
                @Override
                public void run() {
                    /*Intent login = new Intent(getApplicationContext(), MainActivity.class);
                    login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(login);*/
                }
            }, 0);
        }
    };
}
