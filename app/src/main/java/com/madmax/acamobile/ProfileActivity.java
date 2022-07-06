package com.madmax.acamobile;

import static com.madmax.acamobile.app.Initializer.ranks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Initializer;
import com.madmax.acamobile.app.MyDialog;
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

    TextView tv_name,tv_phone,tv_email,tv_address,tv_date,tv_startDate,tv_endDate,tv_official_id,
            tv_rank,tv_remove_info,tv_promote_info;
    LinearLayout order_layout,sent_layout,target_layout,remove_layout,promote_layout;
    ImageView iv_profile_small,iv_collapse;
    FloatingActionButton fab;
    Executor postExecutor;
    Toolbar toolbar;
    CollapsingToolbarLayout toolbarLayout;
    NestedScrollView nestedScrollView;
    Button bt_calculate;
    String memberId,groupId,agent_phone;

    SharedPreferences sharedPreferences;
    String userId,authToken;
    int rank_id;
    long startDate=0,finalDate=System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        rank_id=sharedPreferences.getInt("rank_id",1);

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
        tv_official_id=findViewById(R.id.tv_official_id);
        tv_rank=findViewById(R.id.tv_rank);
        tv_remove_info=findViewById(R.id.tv_remove_info);
        tv_promote_info=findViewById(R.id.tv_promote_info);
        order_layout=findViewById(R.id.order_layout);
        sent_layout=findViewById(R.id.sent_layout);
        target_layout=findViewById(R.id.target_layout);
        remove_layout=findViewById(R.id.remove_layout);
        promote_layout=findViewById(R.id.promote_layout);
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

        remove_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyDialog myDialog=new MyDialog(ProfileActivity.this, "Remove User!", "This user will be permanently removed from the group", new MyDialog.ConfirmClick() {
                    @Override
                    public void onConfirmClick() {
                        removeUser();
                    }
                });
                myDialog.showMyDialog();
            }
        });

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
                if(isPermissionGranted()){
                    callPhone(agent_phone);
                }else{
                    takePermission();
                }
            }
        });

        target_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProfileActivity.this,PlanAndOrderActivity.class);
                intent.putExtra("memberId",memberId);
                intent.putExtra("groupId",groupId);
                intent.putExtra("filtering",false);
                startActivity(intent);
            }
        });

        promote_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRankMenu(view);
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

                Intent intent=new Intent(ProfileActivity.this,PlanAndOrderActivity.class);
                intent.putExtra("memberId",memberId);
                intent.putExtra("groupId",groupId);
                intent.putExtra("start_date",startDate);
                intent.putExtra("end_date",finalDate);
                intent.putExtra("filtering",true);
                startActivity(intent);

            }
        });

    }

    private void showRankMenu(View v){

        PopupMenu popup=new PopupMenu(this,v);
        popup.getMenuInflater().inflate(R.menu.rank_menu,popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("Recycle")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                int ranking=0;
                switch (id){
                    case R.id.rank_agent:
                        ranking=1;
                        break;
                    case R.id.rank_special_agent:
                        ranking=2;
                        break;
                    case R.id.rank_vip:
                        ranking=3;

                        break;
                    case R.id.rank_dealer:
                        ranking=4;

                        break;
                    case R.id.rank_wholesaler:
                        ranking=5;

                        break;
                    case R.id.rank_silver_wholesaler:
                        ranking=6;

                        break;
                    case R.id.rank_gold_wholesaler:
                        ranking=7;

                        break;
                    case R.id.rank_platinum_wholesaler:
                        ranking=8;

                        break;
                    case R.id.rank_ceo:
                        ranking=9;

                        break;
                }

                promoteRank(ranking);
                return true;
            }
        });
        popup.show();
    }

    private void promoteRank(int ranking){
        if(ranking==0) return;
        if(rank_id>=ranking){
            tv_promote_info.setText("Promoting");
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("PromoteRes ",response);
                       postExecutor.execute(new Runnable() {
                           @Override
                           public void run() {
                               try {
                                   JSONObject jo=new JSONObject(response);
                                   boolean success=jo.getString("status").equals("success");
                                   if(success){
                                       tv_promote_info.setText("Successfully promoted");
                                       tv_rank.setText(ranks.get(ranking-1).getRank());
                                   }
                               }catch (Exception e){
                                   Log.e("PromoteJSONErr ",e.toString());
                                   tv_promote_info.setText("Please try again!");
                               }
                           }
                       });

                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("Err MY GP ",msg);
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                tv_promote_info.setText("Please try again!");
                            }
                        });
                    }
                }).url(Routing.PROMOTE_RANK)
                        .field("user_id",memberId)
                        .field("rank_id",ranking+"")
                        .field("auth_token",authToken);
                myHttp.runTask();
            }).start();
        }else{
            Toast.makeText(this, "Cannot promote!", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeUser() {
        tv_remove_info.setText("Removing ..");
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jo=new JSONObject(response);
                        boolean success=jo.getString("status").equals("success");
                        if(success){
                            finish();
                        }
                    }catch (Exception e){
                        tv_remove_info.setText("Please try again!");
                    }

                }
                @Override
                public void onError(String msg) {
                    Log.e("Err MY GP ",msg);
                    tv_remove_info.setText("Please try again!");
                }
            }).url(Routing.REMOVE_MEMBER)
                    .field("member_id",memberId)
                    .field("user_id",userId)
                    .field("group_id",MyGroupDetailActivity.groupId)
                    .field("auth_token",authToken);
            myHttp.runTask();
        }).start();
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
                                startDate=date*1000;


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
        agent_phone= jo.getString("phone");
        String address=jo.getString("address");
        String official_id=jo.getString("official_agent_id");
        int rank_id=jo.getInt("rank_id");



        toolbarLayout.setTitle(name);
        tv_name.setText(name);
        AppUtils.setPhotoFromRealUrl(iv_collapse,Routing.PROFILE_URL+profileUrl);
        AppUtils.setPhotoFromRealUrl(iv_profile_small,Routing.PROFILE_URL+profileUrl);
        tv_email.setText(email);
        tv_phone.setText(agent_phone);
        tv_address.setText(address);
        tv_official_id.setText(official_id);
        tv_rank.setText(Initializer.ranks.get(rank_id-1).getRank());

    }

    private void takePermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},101);
    }

    private boolean isPermissionGranted(){
        int  callPhone= ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        return  callPhone== PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            if(requestCode==101){
                boolean callPhone=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(callPhone){
                    callPhone(agent_phone);
                }else {
                    takePermission();
                }
            }
        }
    }

    private void callPhone(String phone){
        phone=phone.replace("#","%23");
        Intent intent=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phone));
        startActivity(intent);
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