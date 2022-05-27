package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.madmax.acamobile.adapters.ViewPageAdapter;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.fragments.partnergroup.FragmentOverview;
import com.madmax.acamobile.fragments.partnergroup.FragmentOrder;

import java.util.Objects;
import java.util.concurrent.Executor;

public class PartnerGroupDetailActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    String userId,authToken;
    String groupId,name,imageUrl,description;
    Executor postExecutor;
    Toolbar toolbar;
    CollapsingToolbarLayout toolbarLayout;

    ImageView iv_small,iv_collapse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_group_detail);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        postExecutor= ContextCompat.getMainExecutor(this);
        groupId=getIntent().getExtras().getString("groupId");
        name=getIntent().getExtras().getString("name");
        imageUrl= Routing.GROUP_COVER_URL+getIntent().getExtras().getString("imageUrl");
        description=getIntent().getExtras().getString("description");

        setUpView();

    }

    private void setUpView() {
        toolbar=findViewById(R.id.toolbar);
        iv_small=findViewById(R.id.iv_group_small);
        iv_collapse=findViewById(R.id.iv_collapse_group);
        toolbarLayout=findViewById(R.id.ctb);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AppBarLayout mAppBarLayout =findViewById(R.id.app_bar_layout);

        toolbarLayout.setTitle(name);
        AppUtils.setPhotoFromRealUrl(iv_small,imageUrl);
        AppUtils.setPhotoFromRealUrl(iv_collapse,imageUrl);

        ViewPager viewPager=findViewById(R.id.viewpager);
        TabLayout tabLayout=findViewById(R.id.tab_layout);
        ViewPageAdapter viewPageAdapter=new ViewPageAdapter(getSupportFragmentManager());
        viewPageAdapter.addFragments(new FragmentOverview(groupId),"Overview");
        viewPageAdapter.addFragments(new FragmentOrder(groupId,0),"My Order");
        viewPageAdapter.addFragments(new FragmentOrder(groupId,1),"Received");


        viewPager.setAdapter(viewPageAdapter);
        tabLayout.setupWithViewPager(viewPager);

        NestedScrollView scrollView = findViewById (R.id.nested_scroll_view);
        scrollView.setFillViewport (true);



        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                    iv_small.setVisibility(View.GONE);

                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    iv_small.setVisibility(View.VISIBLE);


                } else if (isShow) {
                    isShow = false;
                    iv_small.setVisibility(View.GONE);

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("About")
                .setIcon(R.drawable.ic_baseline_info_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }else if(item.getTitle().toString().equals("About")){
            Intent intent=new Intent(PartnerGroupDetailActivity.this,AboutGroupActivity.class);
            intent.putExtra("groupId",groupId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}