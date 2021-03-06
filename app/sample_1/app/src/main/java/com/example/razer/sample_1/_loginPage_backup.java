package com.example.razer.sample_1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by Razer on 2016-04-12.
 */

public class _loginPage_backup extends Activity {
    private Button login, join;
    private Handler a, b;
    private EditText eaddress, pwd;
    private Vibrator vibe;
    private Boolean loginflag = false;
    private Boolean serverflag = false;
    private LinearLayout loginLayout;
    private Socket socket;
    private InputMethodManager imm;
    private SharedPreferences test;
    private SharedPreferences.Editor editor;
    private String storedid;
    private BackPressCloseHandler backPressCloseHandler;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private GoogleApiClient client;

    {
        try {
            socket = IO.socket("http://106.243.213.92:10/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        login = (Button) findViewById(R.id.button);
        join = (Button) findViewById(R.id.button2);
        pwd = (EditText) findViewById(R.id.pwd);
        eaddress = (EditText) findViewById(R.id.eaddress);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        loginLayout = (LinearLayout) findViewById(R.id.loginMainLayout);
        socket.on("loginok", loginok);
        socket.on("loginfalse", loginfalse);
        final ProgressDialog progDialog = new ProgressDialog(this);
        backPressCloseHandler = new BackPressCloseHandler(this);

        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        test = getSharedPreferences("test", MODE_PRIVATE);
        editor = test.edit();

        storedid = test.getString("ID", "");
        eaddress.setText(storedid);
        editor.commit(); //완료한다.

        a = new Handler();
        b = new Handler();

        //회원정보 쿼리문.

        loginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(eaddress.getWindowToken(), 0);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //뜯어 고쳐야 할 곳. MemberLeave 클래스와 동일하게 작성 할 것.
                //UserInfo에 들어가는 내용들은 서버에서 정보가 넘어오면 그걸 토대로 넣을 것.
                if (pwd.getText().toString().length() > 0 && eaddress.getText().toString().length() > 0) {
                    progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progDialog.setMessage("로그인중....");
                    progDialog.show();
                    UserInfo.setMailAddress(eaddress.getText().toString());
                    UserInfo.setPwd(pwd.getText().toString());
                    socket.connect();


                    a.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendLogin();
                        }
                    }, 1000);

                    b.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (loginflag) {
                                Toast.makeText(getApplicationContext(), "로그인 인증완료", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                editor.putString("ID",eaddress.getText().toString());
                                editor.commit();
                                startActivity(intent);
                                vibe.vibrate(500);
                                serverflag = true;
                                finish();
                            }
                            else if(!serverflag){
                                vibe.vibrate(1000);
                                Toast.makeText(getApplicationContext(), "서버응답이 없습니다.", Toast.LENGTH_SHORT).show();
                            }

                            else {
                                vibe.vibrate(1000);
                                Toast.makeText(getApplicationContext(), "해당되는 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                            progDialog.dismiss();
                            socket.disconnect();
                        }
                    }, 2500);

                } else {
                    vibe.vibrate(500);
                    Toast.makeText(getApplicationContext(), "칸을 모두 채워주세요!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent joinpage = new Intent(getApplicationContext(), JoinPage.class);
                startActivity(joinpage);
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    private void hideKeyboard(){
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }



    private Emitter.Listener loginfalse = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            loginflag = false;
            serverflag = true;
        }
    };

    private Emitter.Listener loginok = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            loginflag = true;
            serverflag = true;
        }
    };

    private void sendLogin() {
        JSONObject sendLogin = new JSONObject();
        try {
            sendLogin.put("pwd", pwd.getText().toString());
            sendLogin.put("email", eaddress.getText().toString());
            socket.emit("finduser", sendLogin);

        } catch (JSONException e) {

        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "loginPage Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.razer.sample_1/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "loginPage Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.razer.sample_1/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
