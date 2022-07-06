package com.madmax.acamobile;

import static com.madmax.acamobile.app.Initializer.ranks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.madmax.acamobile.adapters.MainPanelAdapter;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.AuthChecker;
import com.madmax.acamobile.app.Initializer;
import com.madmax.acamobile.app.MyDialog;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.charts.SaleAndOrderRate;
import com.madmax.acamobile.models.PanelModel;
import com.madmax.acamobile.models.ProductModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ImageView iv_edit_profile;
    String name,phone,profileImage,userId;
    ImageView iv_profile;
    TextView tv_username,tv_phone,tv_developer_contact;
    SharedPreferences sharedPreferences;


    RecyclerView recyclerView;
    ArrayList<PanelModel> panelModels=new ArrayList<>();
    MainPanelAdapter adapter;

    //dashboard
    LinearLayout chartLayout;
    boolean verified;

    int rank_id;
    float version;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        name=sharedPreferences.getString("username","");
        phone=sharedPreferences.getString("phone","");
        profileImage=sharedPreferences.getString("profileImage","http");
        userId=sharedPreferences.getString("userId",null);
        rank_id=sharedPreferences.getInt("rank_id",1);
        verified=sharedPreferences.getBoolean("verified",false);

        version=sharedPreferences.getFloat("version",1.0f);


        if(version>AppUtils.currentVersion){
            confirmVersionUpdate();
        }

        initializePanelObject();
        setUpView();
    }

    private void setUpView(){

        chartLayout=findViewById(R.id.chartLayout);


        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        recyclerView=findViewById(R.id.recyclerView);
        GridLayoutManager gm=new GridLayoutManager(this,2);

        recyclerView.setLayoutManager(gm);
        //recycler.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(this, 2), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new MainPanelAdapter(this,panelModels);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        tv_developer_contact=navigationView.findViewById(R.id.tv_developer_contact);


        View headerView=navigationView.getHeaderView(0);
        iv_edit_profile=headerView.findViewById(R.id.iv_edit_profile);
        tv_phone=headerView.findViewById(R.id.tv_header_title);
        tv_username=headerView.findViewById(R.id.tv_header);
        iv_profile=headerView.findViewById(R.id.iv_header);

        AppUtils.setPhotoFromRealUrl(iv_profile, Routing.PROFILE_URL+profileImage);

        tv_username.setText(name);
        tv_phone.setText(ranks.get(rank_id-1).getRank());


        iv_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,UpdateProfileActivity.class);
                startActivity(intent);
            }
        });

        tv_developer_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionGranted()){
                    callPhone("09979638384");
                }else{
                    takePermission();
                }
            }
        });

        SaleAndOrderRate saleAndOrderRate=new SaleAndOrderRate(this,getSupportFragmentManager(),userId,Routing.CHART_SALE_AND_ORDER+"?",false);
        chartLayout.addView(saleAndOrderRate.getView());

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch (id){

            case R.id.nav_logOut:
                new AuthChecker(MainActivity.this).logout();
                break;
            case R.id.nav_invoice_edit:
                Intent intent3=new Intent(MainActivity.this,InvoiceEditActivity.class);
                startActivity(intent3);
                break;
            case R.id.nav_target_plan:
                Intent intent4=new Intent(MainActivity.this,TargetPlanActivity.class);
                startActivity(intent4);
                break;
            case R.id.nav_stock:
                Intent intent5=new Intent(MainActivity.this,StockListActivity.class);
                startActivity(intent5);
                break;
            case R.id.nav_product:
                if(verified){
                    Intent intent6=new Intent(MainActivity.this, ProductListActivity.class);
                    startActivity(intent6);
                }else{
                    Toast.makeText(getApplicationContext(), "Your account is needed to verifed by your leader", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_reset_password:
                Intent intent7=new Intent(MainActivity.this,ResetPasswordActivity.class);
                startActivity(intent7);
                break;

            case R.id.nav_about:
                Intent intent=new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra("link", Routing.DOMAIN+"acamobile/about.php");
                startActivity(intent);
                break;

            case R.id.nav_app_upate:
                startActivity(new Intent(MainActivity.this,AppUpdateActivity.class));
                break;
            case R.id.nav_customer:
                startActivity(new Intent(MainActivity.this,CustomersActivity.class));
                break;
            case R.id.nav_investment:
                startActivity(new Intent(MainActivity.this,InvestmentActivity.class));
        }

        return true;
    }

    @Override
    protected void onResume() {
        DrawerLayout drawerLayout=findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        super.onResume();
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) Objects.requireNonNull(this).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = Objects.requireNonNull(cm).getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void initializePanelObject(){
        // greatest id =8
        panelModels.add(new PanelModel("My Business","file:///android_asset/mybusiness.png",8));
        panelModels.add(new PanelModel("Create Order","file:///android_asset/createorder.png",1));
        panelModels.add(new PanelModel("Voucher","file:///android_asset/voucher.png",2));
        panelModels.add(new PanelModel("Products Left","file:///android_asset/products.png",3));
        panelModels.add(new PanelModel("My Groups","file:///android_asset/mygroup.png",6));
        panelModels.add(new PanelModel("My Leader's Group","file:///android_asset/partnergroup.png",7));

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
                    callPhone("09979638384");
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


    private void confirmVersionUpdate(){
        MyDialog myDialog=new MyDialog(this, "Version Update", "Please check for new verion", new MyDialog.ConfirmClick() {
            @Override
            public void onConfirmClick() {
                Intent intent=new Intent(MainActivity.this,AppUpdateActivity.class);
                startActivity(intent);
            }
        });

        myDialog.showMyDialog();
    }
}