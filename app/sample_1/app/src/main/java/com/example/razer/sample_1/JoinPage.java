package com.example.razer.sample_1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Razer on 2016-04-19.
 */
public class JoinPage extends Activity{
    private Button join, back;
    private EditText name, email, pwd;
    private Handler c,d;
    private Socket socket;
    private Boolean joinflag = false;
    private Boolean serverflag = false;
    private Boolean countflag = false;
    private InputMethodManager imm;
    private LinearLayout joinMainLayout;
    private Vibrator vibe;
    {
        try{
            socket = IO.socket("http://106.243.213.92:10/");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_page);
        final ProgressDialog progDialog = new ProgressDialog( this );
        back = (Button) findViewById(R.id.back);
        join = (Button) findViewById(R.id.join);
        socket.on("joinok", joinok);
        socket.on("joinfalse", joinfalse);
        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);
        pwd = (EditText) findViewById(R.id.pwd);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        final String emailPattern = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";

        c = new Handler();
        d = new Handler();

        joinMainLayout = (LinearLayout) findViewById(R.id.joinMainLayout);
        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        joinMainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(name.getWindowToken(), 0);
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().toString().length() > 0 && pwd.getText().toString().length() > 0 && email.getText().toString().length() > 0) {
                    if (!Pattern.matches(emailPattern,email.getText().toString())) {
                        if(email.getText().toString().matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")){
                            Toast.makeText(getApplicationContext(), "이메일에 한글이 포함되어 있습니다.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
                        }
                        vibe.vibrate(500);
                    }

                    if(pwd.getText().toString().length() < 6){
                        Toast.makeText(getApplicationContext(), "비밀번호는 6자리 이상 입력하세요.", Toast.LENGTH_SHORT).show();
                        vibe.vibrate(500);
                    }

                    else{
                        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progDialog.setMessage("회원가입중....");
                        progDialog.show();
                        socket.connect();
                        final long start = System.currentTimeMillis();

                        sendJoin();
                        countflag = false;
                        serverflag = false;
                        joinflag = false;

                        c = new Handler() {
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                if(joinflag == true && serverflag == true && countflag == false) {
                                    progDialog.dismiss();
                                    socket.disconnect();
                                    c.removeCallbacksAndMessages(null);
                                    Toast.makeText(getApplicationContext(), "회원가입완료 다시 로그인해주세요!", Toast.LENGTH_SHORT).show();
                                    countflag = true;
                                    vibe.vibrate(500);
                                    finish();
                                }
                                else if(joinflag == false && serverflag == true && countflag == false){
                                    vibe.vibrate(1000);
                                    Toast.makeText(getApplicationContext(), "이미 가입된 메일주소입니다.", Toast.LENGTH_SHORT).show();
                                    progDialog.dismiss();
                                    socket.disconnect();
                                    countflag = true;
                                }
                                long end = System.currentTimeMillis();

                                //서버응답없음 설정하기
                                if((end-start) > 5000 && countflag == false ){
                                    vibe.vibrate(1000);
                                    Toast.makeText(getApplicationContext(), "서버응답이 없습니다. 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                    progDialog.dismiss();
                                    socket.disconnect();
                                    countflag = true;
                                }
                                c.sendEmptyMessageDelayed(5000, 1000);
                            }
                        };
                        c.sendEmptyMessage(1);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "칸을 모두 채워주세요!", Toast.LENGTH_SHORT).show();
                    vibe.vibrate(500);
                }
            }


        });
    }

    private void sendJoin(){
        JSONObject sendJoin = new JSONObject();
        try{
            sendJoin.put("pwd", pwd.getText().toString());
            sendJoin.put("email", email.getText().toString());
            sendJoin.put("name", name.getText().toString());
            socket.emit("joinuser", sendJoin);
        }catch(JSONException e){
        }
    }

    private Emitter.Listener joinfalse = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            joinflag = false;
            serverflag = true;
        }
    };

    private Emitter.Listener joinok = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            joinflag = true;
            serverflag = true;
        }
    };

}
