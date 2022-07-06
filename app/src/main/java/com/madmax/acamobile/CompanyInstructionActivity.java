package com.madmax.acamobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;

import com.madmax.acamobile.app.Routing;

public class CompanyInstructionActivity extends AppCompatActivity {

    WebView wv;
    ProgressBar pb;
    Button bt;
    private boolean isRedirected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_instruction);

        getSupportActionBar().hide();

        setUpView();
    }

    private void setUpView(){
        wv=findViewById(R.id.wv);
        pb=findViewById(R.id.pb);
        bt=findViewById(R.id.bt_continue);


        wv.setWebViewClient(new WebViewClient());
        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAppCachePath(getCacheDir().getAbsolutePath());
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        startWebView(wv);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CompanyInstructionActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void startWebView(WebView wv){
        wv.setWebViewClient(new WebViewClient(){

            public boolean shouldOverrideUrlLoading(WebView view,String url){

                view.loadUrl(url);
                isRedirected=true;
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                isRedirected=false;
            }
            public void onLoadResource(WebView view, String url){
                if(!isRedirected){
                    pb.setVisibility(View.VISIBLE);
                }
            }

            public void onPageFinished(WebView view,String url){
                try{
                    isRedirected=true;
                     pb.setVisibility(View.GONE);

                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }

        });

        wv.loadUrl(Routing.COMPANY_INSTRUCTIONS);
        pb.setVisibility(View.GONE);
    }
}