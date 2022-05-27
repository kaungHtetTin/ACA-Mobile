package com.madmax.acamobile;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.madmax.acamobile.app.AuthChecker;
import com.madmax.acamobile.app.Initializer;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.zguniconvert.MDetect;


public class SplashScreenActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        setContentView(R.layout.activity_splash_screen);
        
        getMessagingToken();
        new Initializer(this).initialize();
        setScreen();

    }

    private void setScreen(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    new AuthChecker(SplashScreenActivity.this, new AuthChecker.AuthCallBack() {
                        @Override
                        public void onAuthChecker(boolean auth) {
                            Intent intent;
                            if(auth){
                                intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                            }else{
                                intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        }
                    });

                }catch (Exception e){}

            }
        }).start();
    }

    private void getMessagingToken(){
        FirebaseInstallations.getInstance().getId();
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();
                        editor.putString("fcmToken",token);
                        editor.apply();

                    }
                });
    }

}