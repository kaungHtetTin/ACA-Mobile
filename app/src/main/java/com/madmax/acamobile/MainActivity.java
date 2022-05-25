package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.navigation.NavigationView;
import com.madmax.acamobile.adapters.MainPanelAdapter;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.AuthChecker;
import com.madmax.acamobile.app.Initializer;
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
    TextView tv_username,tv_phone;
    SharedPreferences sharedPreferences;

    RecyclerView recyclerView;
    ArrayList<PanelModel> panelModels=new ArrayList<>();
    MainPanelAdapter adapter;

    //dashboard
    LinearLayout chartLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        name=sharedPreferences.getString("username","");
        phone=sharedPreferences.getString("phone","");
        profileImage=sharedPreferences.getString("profileImage","http");
        userId=sharedPreferences.getString("userId",null);
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


        View headerView=navigationView.getHeaderView(0);
        iv_edit_profile=headerView.findViewById(R.id.iv_edit_profile);
        tv_phone=headerView.findViewById(R.id.tv_header_title);
        tv_username=headerView.findViewById(R.id.tv_header);
        iv_profile=headerView.findViewById(R.id.iv_header);

        AppUtils.setPhotoFromRealUrl(iv_profile, Routing.PROFILE_URL+profileImage);

        tv_username.setText(name);
        tv_phone.setText(phone);


        iv_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,UpdateProfileActivity.class);
                startActivity(intent);
            }
        });


        SaleAndOrderRate saleAndOrderRate=new SaleAndOrderRate(this,getSupportFragmentManager(),userId);
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
        panelModels.add(new PanelModel("Products Left","file:///android_asset/productleft.png",3));
        panelModels.add(new PanelModel("Products","file:///android_asset/products.png",4));
        panelModels.add(new PanelModel("Stocks","file:///android_asset/stock.png",5));
        panelModels.add(new PanelModel("My Groups","file:///android_asset/mygroup.png",6));
        panelModels.add(new PanelModel("Partners' Groups","file:///android_asset/partnergroup.png",7));

    }
}