package com.example.razer.sample_1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * Created by Razer on 2016-05-16.
 */
public class WebViewPage extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_page);


        Intent intent = getIntent();
        String URLADD = intent.getStringExtra("URL");

        WebView WebView01 = (WebView) findViewById(R.id.webView01);
        WebView01.setWebViewClient(new WebViewClient());

        WebSettings webSettings = WebView01.getSettings();
        webSettings.setJavaScriptEnabled(true);


        //웹뷰 롱클릭시 블럭지정 안되게.
        WebView01.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });


        //Intent시 URL을 전송받아 실행
        WebView01.loadUrl("http://106.243.213.92:10/"+URLADD);
        //WebView01.loadUrl("http://106.243.213.92:10/usermonitering");
    }
}
