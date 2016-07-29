package com.example.razer.sample_1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by Razer on 2016-05-17.
 */
public class MemberLeave extends Activity{
    private Button goleave;
    private Socket socket;
    private ProgressDialog mDialog;
    private Handler mHandler;
    private EditText etext;

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
        setContentView(R.layout.member_leave);

        mHandler = new Handler();
        socket.on("memberleaveok", memberleaveok);

        goleave = (Button) findViewById(R.id.goLeave);
        etext = (EditText) findViewById(R.id.eText);

        goleave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socket.connect();
                if (UserInfo.getPwd().equals(etext.getText().toString())) {
                    deletemember();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDialog = ProgressDialog.show(MemberLeave.this, "", "잠시만 기다려 주세요", true);
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (mDialog != null && mDialog.isShowing()) {
                                            mDialog.dismiss();
                                            Toast.makeText(MemberLeave.this, "서버 응답이 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                                            if(socket.connected()){
                                                socket.disconnect();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 8000);
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(),"올바른 비밀번호를 입력하세요",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Emitter.Listener memberleaveok = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (mDialog!=null&&mDialog.isShowing()){
                mDialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder dlg = new AlertDialog.Builder(MemberLeave.this);
                        dlg.setTitle("회원탈퇴 완료");
                        dlg.setMessage("로그인페이지로..");
                        //dlg.setIcon(R.drawable.aa);
                        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getApplicationContext(), loginPage.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        dlg.show();
                        if(socket.connected()){
                            socket.disconnect();
                        }
                    }
                });
            }
        }
    };

    private void deletemember() {
        JSONObject deletemember = new JSONObject();
        try {
            deletemember.put("email", UserInfo.getMailAddress());
            deletemember.put("pwd", etext.getText().toString());
            socket.emit("memberleave", deletemember);
        } catch (JSONException e) {

        }
    }
}
