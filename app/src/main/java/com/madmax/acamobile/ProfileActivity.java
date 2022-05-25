package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Executor;

public class ProfileActivity extends AppCompatActivity {

    TextView tv_name,tv_phone,tv_email,tv_address,tv_date,tv_startDate,tv_endDate;
    LinearLayout order_layout,sent_layout,target_layout;
    ImageView iv_profile_small,iv_collapse;
    FloatingActionButton fab;
    Executor postExecutor;
    Toolbar toolbar;
    CollapsingToolbarLayout toolbarLayout;
    NestedScrollView nestedScrollView;
    Button bt_calculate;
    String memberId,groupId;

    long startDate=0,finalDate=System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        memberId=getIntent().getExtras().getString("memberId");
        groupId=getIntent().getExtras().getString("groupId");
        postExecutor= ContextCompat.getMainExecutor(this);
        setUpView();

    }

    private void setUpView(){

        tv_name=findViewById(R.id.tv_name);
        tv_phone=findViewById(R.id.tv_phone);
        tv_email=findViewById(R.id.tv_email);
        tv_address=findViewById(R.id.tv_address);
        tv_date=findViewById(R.id.tv_join_date);
        tv_startDate=findViewById(R.id.tv_startDate);
        tv_endDate=findViewById(R.id.tv_endDate);
        order_layout=findViewById(R.id.order_layout);
        sent_layout=findViewById(R.id.sent_layout);
        target_layout=findViewById(R.id.target_layout);
        bt_calculate=findViewById(R.id.bt_calculate);



        iv_profile_small=findViewById(R.id.iv_profile_small);
        iv_collapse=findViewById(R.id.iv_collapse);
        fab=findViewById(R.id.fab);
        toolbar=findViewById(R.id.toolbar);
        nestedScrollView=findViewById(R.id.nested_layout);
        nestedScrollView.setVisibility(View.INVISIBLE);

        toolbarLayout=findViewById(R.id.ctb);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AppBarLayout mAppBarLayout =findViewById(R.id.app_bar_layout);
        toolbarLayout.setTitle("Loading ... ");

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                    iv_profile_small.setVisibility(View.GONE);

                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    iv_profile_small.setVisibility(View.VISIBLE);


                } else if (isShow) {
                    isShow = false;
                    iv_profile_small.setVisibility(View.GONE);

                }
            }
        });

        sent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProfileActivity.this,OrderActivity.class);
                intent.putExtra("isSoldOut",1);
                intent.putExtra("agent_id",memberId);
                intent.putExtra("group_id",groupId);
                startActivity(intent);
            }
        });

        order_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProfileActivity.this,OrderActivity.class);
                intent.putExtra("isSoldOut",0);
                intent.putExtra("agent_id",memberId);
                intent.putExtra("group_id",groupId);
                startActivity(intent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        target_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProfileActivity.this,PlanAndOrderActivity.class);
                intent.putExtra("memberId",memberId);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        fetchProfileData();

        tv_startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                        tv_startDate.setText(day+" - "+ AppUtils.month[month]+" - "+year);
                    }
                });
            }
        });



        tv_endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDatePicker.Builder<Long> builder2=MaterialDatePicker.Builder.datePicker();
                builder2.setTitleText("Select A Final Date");
                MaterialDatePicker<Long> materialDatePickerTarget=builder2.build();

                materialDatePickerTarget.show(getSupportFragmentManager(),System.currentTimeMillis()+"");
                materialDatePickerTarget.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {

                        finalDate=selection;

                        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        calendar.setTimeInMillis(selection);
                        int day=calendar.get(Calendar.DAY_OF_MONTH);
                        int month=calendar.get(Calendar.MONTH);
                        int year=calendar.get(Calendar.YEAR);
                        month++;
                        tv_endDate.setText(day+" - "+AppUtils.month[month]+" - "+year);
                    }
                });
            }
        });

        bt_calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Intent intent=new Intent(ProfileActivity.this,FilterActivity.class);
//                intent.putExtra("agentId",memberId);
//                intent.putExtra("startDate",startDate);
//                intent.putExtra("endDate",finalDate);
//                intent.putExtra("is_sold_out",type);
//                intent.putExtra("title",title);
//                startActivity(intent);

            }
        });

    }

    private void fetchProfileData(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("member profile ",response );
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                JSONObject jo=new JSONObject(response);
                                setProfileInfo(jo.getJSONObject("info"));

                                long date=Long.parseLong(jo.getJSONObject("joinDate").getString("time"));
                                tv_date.setText(AppUtils.formatTime(date*1000));


                            }catch (Exception e){}
                        }
                    });

                }
                @Override
                public void onError(String msg) {
                    Log.e("Err MY GP ",msg);

                }
            }).url(Routing.GET_MEMBER_PROFILE+"?group_id="+groupId+"&member_id="+memberId);

            myHttp.runTask();
        }).start();
    }

    private void setProfileInfo(JSONObject jo) throws JSONException {

        nestedScrollView.setVisibility(View.VISIBLE);

        String name=jo.getString("name");
        String profileUrl=jo.getString("profile_image");
        String email= jo.getString("email");
        String phone= jo.getString("phone");
        String address=jo.getString("address");

        toolbarLayout.setTitle(name);
        tv_name.setText(name);
        AppUtils.setPhotoFromRealUrl(iv_collapse,Routing.PROFILE_URL+profileUrl);
        AppUtils.setPhotoFromRealUrl(iv_profile_small,Routing.PROFILE_URL+profileUrl);
        tv_email.setText(email);
        tv_phone.setText(phone);
        tv_address.setText(address);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}