package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.AuthChecker;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Executor;

public class AddTargetPlanActivity extends AppCompatActivity {

    TextView tv_startDate,tv_endDate,tv_header;
    Button bt_create;
    ProgressBar pb;
    SharedPreferences sharedPreferences;
    String userId,authToken;
    long startDate=0,endDate=0;
    Executor postExecutor;

    boolean groupUpdate;
    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_target_plan);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        postExecutor= ContextCompat.getMainExecutor(this);

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupUpdate=getIntent().getExtras().getBoolean("group",false);
        groupId=getIntent().getExtras().getString("groupId","");

        setUpView();
    }

    private void setUpView(){
        tv_header=findViewById(R.id.tv_header);
        tv_startDate=findViewById(R.id.tv_startDate);
        tv_endDate=findViewById(R.id.tv_endDate);
        bt_create=findViewById(R.id.bt_create);
        pb=findViewById(R.id.pb);

        if(groupUpdate){
            tv_header.setText("Update group target plan");
            setTitle("Update Plan");
            userId=groupId;
            bt_create.setText("Update");
        }else{
            tv_header.setText("Create new target plan");
            setTitle("Create Plan");
            bt_create.setText("Create");
        }

        tv_startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialDatePicker.Builder<Long> builder=MaterialDatePicker.Builder.datePicker();
                builder.setTitleText("Select An Initial Date");
                MaterialDatePicker<Long> materialDatePicker=builder.build();
                materialDatePicker.show(getSupportFragmentManager(),System.currentTimeMillis()+"");

                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        startDate=selection;

                        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        calendar.setTimeInMillis(selection);
                        int day=calendar.get(Calendar.DAY_OF_MONTH);
                        int month=calendar.get(Calendar.MONTH);
                        int year=calendar.get(Calendar.YEAR);

                        tv_startDate.setText(AppUtils.month[month]+" "+day+", "+year);
                    }
                });
            }
        });

        tv_endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialDatePicker.Builder<Long> builder2=MaterialDatePicker.Builder.datePicker();
                builder2.setTitleText("Select A Final Date");
                MaterialDatePicker<Long> materialDatePickerTarget=builder2.build();

                materialDatePickerTarget.show(getSupportFragmentManager(),System.currentTimeMillis()+"");
                materialDatePickerTarget.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {

                        endDate=selection;

                        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        calendar.setTimeInMillis(selection);
                        int day=calendar.get(Calendar.DAY_OF_MONTH);
                        int month=calendar.get(Calendar.MONTH);
                        int year=calendar.get(Calendar.YEAR);

                        tv_endDate.setText(AppUtils.month[month]+" "+day+", "+year);
                    }
                });
            }
        });

        bt_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startDate!=0&&endDate!=0){
                   if(groupUpdate){
                        createNewTargetPlan(Routing.UPDATE_GROUP_TARGET_PLAN);
                   }else {
                       createNewTargetPlan(Routing.ADD_TARGET_PLAN);
                   }
                }else{
                    Toast.makeText(getApplicationContext(),"Select all dates",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void createNewTargetPlan(String url){
        pb.setVisibility(View.VISIBLE);
        if(userId!=null){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                pb.setVisibility(View.GONE);
                                try {
                                    JSONObject jo = new JSONObject(response);
                                    boolean isSuccess = jo.getString("status").equals("success");
                                    if (isSuccess) {
                                        finish();
                                    } else {
                                        pb.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(), "Upload fail! Try again.", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                }
                            }
                        });
                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("ErrOrders ",msg);
                        pb.setVisibility(View.GONE);

                    }
                }).url(url)
                        .field("user_id",userId)
                        .field("start_date",startDate+"")
                        .field("end_date",endDate+"");

                myHttp.runTask();
            }).start();
        }else{
            new AuthChecker(this).logout();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}