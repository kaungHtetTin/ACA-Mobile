package com.madmax.acamobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;

import org.json.JSONObject;

import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    TextView tv_info,tv_1,tv_2,tv_3,tv_4,tv_5,tv_error;
    LinearLayout layout;
    EditText et_info;
    Button bt_next;
    ProgressBar pb;
    Animation animOut;
    Animation animIn;
    int infoCount=0;
    Executor postExecutor;
    String name,email,phone,password,confirmPassword;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String fcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setContentView(R.layout.activity_sign_up);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        fcmToken=sharedPreferences.getString("fcmToken","abcdef");
        animOut= AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        animIn= AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        postExecutor= ContextCompat.getMainExecutor(this);

        setUpView();
    }

    private void setUpView(){
        tv_info=findViewById(R.id.tv_info);
        et_info=findViewById(R.id.et_info);
        bt_next=findViewById(R.id.bt_next);
        tv_1=findViewById(R.id.tv_1);
        tv_2=findViewById(R.id.tv_2);
        tv_3=findViewById(R.id.tv_3);
        tv_4=findViewById(R.id.tv_4);
        tv_5=findViewById(R.id.tv_5);
        tv_error=findViewById(R.id.tv_error);
        layout=findViewById(R.id.linearLayout);
        pb=findViewById(R.id.pb);

        bt_next.setVisibility(View.VISIBLE);
        bt_next.setAnimation(animIn);
        layout.setVisibility(View.VISIBLE);
        layout.setAnimation(animIn);

        setUpForm();
        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tv_error.setVisibility(View.GONE);
                tv_error.setAnimation(animOut);

                if(!TextUtils.isEmpty(et_info.getText().toString())){

                    infoCount++;
                    setUpForm();
                }else{
                    showTextViewError("Please filled the required field");
                }

            }
        });

    }

    private void setUpForm(){
        switch (infoCount){
            case 0:
                infoSetter("Please Enter Your Name");

                tv_1.setBackgroundResource(R.drawable.bg_signup_info_onprogress);
                tv_1.setTextColor(getResources().getColor(R.color.sign_up_progress));

                break;
            case 1:

                name=et_info.getText().toString();

                infoSetter("Please Enter Your Email Address");
                et_info.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

                tv_2.setBackgroundResource(R.drawable.bg_signup_info_onprogress);
                tv_2.setTextColor(getResources().getColor(R.color.sign_up_progress));

                tv_1.setBackgroundResource(R.drawable.bg_signup_info_complete);
                tv_1.setTextColor(Color.WHITE);

                break;
            case 2:

                email=et_info.getText().toString();

                if(!validateEmail(email)){
                    infoCount--;
                    showTextViewError("Please check your email address");

                }else{
                    existEmail();
                }

                break;

            case 3:

                phone=et_info.getText().toString();

                infoSetter("Please Enter Your Password");
                et_info.setTransformationMethod(PasswordTransformationMethod.getInstance());

                et_info.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                tv_4.setBackgroundResource(R.drawable.bg_signup_info_onprogress);
                tv_4.setTextColor(getResources().getColor(R.color.sign_up_progress));

                tv_3.setBackgroundResource(R.drawable.bg_signup_info_complete);
                tv_3.setTextColor(Color.WHITE);

                break;
            case 4:
                password=et_info.getText().toString();

                if(password.length()>4){
                    infoSetter("Please Confirm Your Password");

                    tv_5.setBackgroundResource(R.drawable.bg_signup_info_onprogress);
                    tv_5.setTextColor(getResources().getColor(R.color.sign_up_progress));
                    tv_4.setBackgroundResource(R.drawable.bg_signup_info_complete);
                    tv_4.setTextColor(Color.WHITE);
                }else{
                    infoCount--;
                    showTextViewError("Password must be at least 5 letters");
                }


                break;
            case 5:
                confirmPassword=et_info.getText().toString();

                if(confirmPassword.equals(password)){
                    tv_5.setBackgroundResource(R.drawable.bg_signup_info_complete);
                    tv_5.setTextColor(Color.WHITE);
                    infoSetter("Ready To Sign Up!");
                    bt_next.setText("Sign Up");
                }else{
                    infoCount--;
                    showTextViewError("Passwords did not match!");
                }
                break;

            case 6:
                pb.setVisibility(View.VISIBLE);
                pb.setAnimation(animIn);

                signUpNow();
                break;
        }
    }

    private void existEmail(){

        if(email!=null){
            makeAnimationOut();
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("email REs ",response);
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jo=new JSONObject(response);
                                    String result=jo.getString("email_exist");
                                    if(result.equals("false")){
                                        infoSetter("Please Enter Your Phone Number");
                                        et_info.setInputType(InputType.TYPE_CLASS_PHONE);
                                        tv_3.setBackgroundResource(R.drawable.bg_signup_info_onprogress);
                                        tv_3.setTextColor(getResources().getColor(R.color.sign_up_progress));
                                        tv_2.setBackgroundResource(R.drawable.bg_signup_info_complete);
                                        tv_2.setTextColor(Color.WHITE);
                                    }else{
                                        makeAnimationIn();
                                        showTextViewError("Already registered.Use another email.");
                                        infoCount--;
                                    }

                                }catch (Exception e){
                                    showTextViewError("Unexpected Error occurred!.Please connect to help center");
                                    makeAnimationIn();
                                    infoCount--;
                                }
                            }
                        });

                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("EmailErr ",msg);
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                showTextViewError("Unexpected Error occurred!.\nPlease connect to help center\n"+msg);
                                makeAnimationIn();
                                infoCount--;
                            }
                        });

                    }
                }).url(Routing.CHECK_EMAIL+"?email="+email);
                myHttp.runTask();
            }).start();
        }else{
            infoCount--;
        }

    }

    private void signUpNow() {
        bt_next.setEnabled(false);
        if(name!=null&&email!=null&&phone!=null&&password!=null){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        try{

                            JSONObject jo=new JSONObject(response);
                            String register = jo.getString("register");
                            String validation=jo.getString("validate");
                            if(validation.equals("fail")){
                                postExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        bt_next.setEnabled(true);
                                        pb.setVisibility(View.GONE);
                                        showTextViewError("Registration failure !\n Please connect to help center");
                                    }
                                });
                                return;
                            }

                            if (register.equals("success")) {
                                String userId = jo.getJSONObject("data").getString("user_id");
                                String authToken = jo.getJSONObject("data").getString("auth_token");
                                editor.putString("authToken", authToken);
                                editor.putString("userId", userId);
                                editor.apply();
                                Intent intent=new Intent(SignUpActivity.this,SplashScreenActivity.class);
                                startActivity(intent);
                                finish();

                            } else {
                                postExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        bt_next.setEnabled(true);
                                        pb.setVisibility(View.GONE);
                                        showTextViewError("Registration failure !\n Please connect to help center");
                                    }
                                });
                            }

                        }catch (Exception e){
                            Log.e("JSOn err ",e.toString());}

                    }
                    @Override
                    public void onError(String msg) {

                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                bt_next.setEnabled(true);
                                pb.setVisibility(View.GONE);
                                showTextViewError("Unexpected error occurred!\n Please connect to help center");
                            }
                        });
                    }
                }).url(Routing.SIGN_UP)
                        .field("name",name)
                        .field("email",email)
                        .field("password",password)
                        .field("phone",phone)
                        .field("fcmToken",fcmToken);

                myHttp.runTask();
            }).start();
        }
    }


    private void infoSetter(String s){
        makeAnimationOut();
        animateTransition(s);
    }

    private void makeAnimationOut(){
        tv_info.setVisibility(View.INVISIBLE);
        et_info.setVisibility(View.INVISIBLE);
        bt_next.setVisibility(View.INVISIBLE);
        tv_info.setAnimation(animOut);
        et_info.setAnimation(animOut);
        bt_next.setAnimation(animOut);
        pb.setVisibility(View.VISIBLE);
        pb.setAnimation(animIn);
    }

    private void makeAnimationIn(){
        tv_info.setVisibility(View.VISIBLE);
        et_info.setVisibility(View.VISIBLE);
        et_info.setAnimation(animIn);
        bt_next.setVisibility(View.VISIBLE);
        tv_info.setAnimation(animIn);
        bt_next.setAnimation(animIn);
        pb.setVisibility(View.GONE);
        pb.setAnimation(animOut);


    }

    private void showTextViewError(String s){
        tv_error.setText(s);
        tv_error.setVisibility(View.VISIBLE);
        tv_error.setAnimation(animIn);
    }

    private boolean validateEmail(String email){
        String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return   matcher.matches();
    }



    private void animateTransition(String s){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(700);
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            tv_info.setVisibility(View.VISIBLE);
                            if(infoCount<5){
                                et_info.setVisibility(View.VISIBLE);
                                et_info.setText("");
                                et_info.setHint(s+"");
                                et_info.setAnimation(animIn);
                            }
                            bt_next.setVisibility(View.VISIBLE);
                            tv_info.setAnimation(animIn);
                            bt_next.setAnimation(animIn);
                            pb.setVisibility(View.GONE);
                            pb.setAnimation(animOut);

                            tv_info.setText(s);

                        }
                    });
                }catch (Exception e){
                    Log.e("Err ",e.toString());
                }
            }
        }).start();

    }
}