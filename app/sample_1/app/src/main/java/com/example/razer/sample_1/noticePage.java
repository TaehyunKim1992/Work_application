package com.example.razer.sample_1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Razer on 2016-05-10.
 */

public class noticePage extends Activity {

    private String[] notilist;
    private Handler mhandler,thandler,ghandler;
    private boolean viewflag = false;
    private boolean countflag = false;


    private Socket socket;
    {
        try{
            socket = IO.socket("http://106.243.213.92:10/");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    private Emitter.Listener noticeList = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray jarray = (JSONArray) args[0];
                        JSONObject jobj = jarray.getJSONObject(0);
                        notilist = new String[jarray.length()];
                        //Log.d("wwwwwwwwwwwwwww", jarray.getJSONObject(2).getString("subject"));
                        //Log.d("wwwwwwwwwwwwwww", jobj.getString("subject") + jobj.getInt("num"));
                        //Log.d("wwwwwwwwwww", jarray.length()+"");
                        for(int i=jarray.length(); i>0;i--){
                            notilist[i-1] = jarray.getJSONObject(jarray.length()-i).getString("subject");
                            //Log.d("", notilist[i-1]);
                        }
                        viewflag = true;
                    } catch (JSONException e) {

                    }

                }
            });
        }
    };

    private Emitter.Listener sendcontent = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("wwwwwwwwwwww",args[0].toString());
                    Toast.makeText(getApplicationContext(),args[0].toString(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice);
        socket.connect();
        socket.on("noticeList", noticeList);
        socket.on("sendcontent", sendcontent);
        socket.emit("showNoticeList");
        mhandler = new Handler();
        ghandler = new Handler();


        thandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //FLAG 체크
                if(viewflag == true && countflag == false){
                    viewlist();
                    viewflag = false;
                }
                thandler.sendEmptyMessageDelayed(5000, 1000);
            }
        };
        thandler.sendEmptyMessage(1);


    }


    protected void viewlist(){
        try {
            final ListView list = (ListView) findViewById(R.id.listView1);


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, notilist);
            list.setAdapter(adapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
                    //socket.emit("sendsubject",notilist[arg2]);
                    Intent webview = new Intent(noticePage.this, WebViewPage.class);
                    webview.putExtra("URL","usermonitering");
                    startActivity(webview);

                }
            });
        } catch (NullPointerException e){
        }
    }
}
