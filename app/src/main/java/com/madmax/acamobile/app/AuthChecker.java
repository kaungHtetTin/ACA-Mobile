package com.madmax.acamobile.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.madmax.acamobile.LoginActivity;

import org.json.JSONObject;


public class AuthChecker {
    Activity c;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String authToken;
    String userId;
    AuthCallBack authCallBack;


    public AuthChecker(@NonNull Activity c) {
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        this.c=c;
    }

    public AuthChecker(@NonNull Activity c, AuthCallBack authCallBack){
        this.authCallBack=authCallBack;
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        authToken=sharedPreferences.getString("authToken",null);
        userId=sharedPreferences.getString("userId",null);
        editor=sharedPreferences.edit();
        this.c=c;
        isAuth();
    }


    public void isAuth(){

        if(authToken==null||userId==null){
            if(authCallBack!=null){
                authCallBack.onAuthChecker(false);
            }
            return ;
        }
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jo=new JSONObject(response);

                        if(authCallBack!=null){
                            boolean isSuccess=jo.getString("auth").equals("success");
                            authCallBack.onAuthChecker(isSuccess);
                            if(isSuccess){
                                JSONObject jo2=jo.getJSONObject("data");
                                String username=jo2.getString("name");
                                String profileImage=jo2.getString("profile_image");
                                String phone=jo2.getString("phone");
                                editor.putString("username",username);
                                editor.putString("profileImage",profileImage);
                                editor.putString("phone",phone);
                                editor.apply();
                            }
                        }

                    }catch (Exception e){

                        if(authCallBack!=null){
                            authCallBack.onAuthChecker(false);
                        }
                    }
                }
                @Override
                public void onError(String msg) {
                    if(authCallBack!=null){
                        authCallBack.onAuthChecker(false);
                    }
                }
            }).url(Routing.CHECK_AUTH+"?user_id="+userId+"&auth_token="+authToken);
            myHttp.runTask();
        }).start();

    }

    public void logout() {
        Intent intent=new Intent(c, LoginActivity.class);
        c.startActivity(intent);
        editor.clear();
        editor.apply();
        c.finish();
    }

    public interface AuthCallBack{
        void onAuthChecker(boolean auth);
    }
}
