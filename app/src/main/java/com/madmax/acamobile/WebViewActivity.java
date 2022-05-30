package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Objects;

public class WebViewActivity extends AppCompatActivity {

    WebView wv;
    SwipeRefreshLayout swipe;

    private boolean isRedirected;
    String Current_url,address,check;

    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        wv=findViewById(R.id.go_web);
        swipe=findViewById(R.id.swipe_go_web);

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        Current_url= Objects.requireNonNull(getIntent().getExtras()).getString("link");
        check=Current_url;
        startWebView(wv,Current_url);
        swipe.setOnRefreshListener(() -> startWebView(wv,Current_url));


    }



    private void startWebView(WebView wv, String url){
        wv.setWebViewClient(new WebViewClient(){

            public boolean shouldOverrideUrlLoading(WebView view,String url){
                Current_url=url;
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
                    swipe.setRefreshing(true);
                }
            }

            public void onPageFinished(WebView view,String url){
                try{
                    isRedirected=true;
                    swipe.setRefreshing(false);

                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }

        });

        wv.loadUrl(url);
        swipe.setRefreshing(false);
    }



    @Override
    public void onBackPressed() {

        if(wv.canGoBack() && !check.equals(address)){
            wv.goBack();
        }else {
           super.onBackPressed();
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            if(wv.canGoBack() && !check.equals(address)){
                wv.goBack();
            }else {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }


}