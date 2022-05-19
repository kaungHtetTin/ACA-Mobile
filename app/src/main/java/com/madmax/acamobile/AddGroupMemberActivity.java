package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.MemberModel;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class AddGroupMemberActivity extends AppCompatActivity {

    EditText et_search;
    RecyclerView recyclerViewSelect;
    CardView resultLayout;
    ImageView iv_profile;
    TextView tv_name,tv_status;
    ProgressBar pb;

    ArrayList<MemberModel> selectedUsers=new ArrayList<>();

    Executor postExecutor;
    SelectAdapter adapter;
    String currentUserId,authToken;
    SharedPreferences sharedPreferences;
    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_member);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        groupId=getIntent().getExtras().getString("groupId");


        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        postExecutor= ContextCompat.getMainExecutor(this);
        setTitle("Add Member");

        setUpView();
    }


    private void setUpView(){
        et_search=findViewById(R.id.et_search_user);
        recyclerViewSelect=findViewById(R.id.recyclerSelect);
        resultLayout=findViewById(R.id.result_layout);
        tv_name=findViewById(R.id.tv_name);
        iv_profile=findViewById(R.id.iv_profile);
        tv_status=findViewById(R.id.tv_status_message);
        pb=findViewById(R.id.pb);


        adapter=new SelectAdapter();

        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewSelect.setLayoutManager(lm);
        recyclerViewSelect.setItemAnimator(new DefaultItemAnimator());
        recyclerViewSelect.setAdapter(adapter);

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUser(charSequence+"");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Add")
                .setIcon(R.drawable.ic_baseline_check_24).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }else if(item.getTitle().toString().equals("Add")){
            if(selectedUsers.size()>0)addToGroup();
            else Toast.makeText(getApplicationContext(),"Search and select your business partner",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    private void addToGroup(){
        tv_status.setText("... Adding ...");
        tv_status.setVisibility(View.VISIBLE);
        pb.setVisibility(View.VISIBLE);
        String members=convertJSON();
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    finish();
                }
                @Override
                public void onError(String msg) {

                }
            }).url(Routing.ADD_GROUP_MEMBERS)
                    .field("user_id",currentUserId)
                    .field("auth_token",authToken)
                    .field("group_id",groupId)
                    .field("members",members);

            myHttp.runTask();
        }).start();

    }

    private String convertJSON(){
        try {
            JSONArray ja=new JSONArray();
            for(int i=0;i<selectedUsers.size();i++){
                JSONObject jo=new JSONObject();
                jo.put("member_id",selectedUsers.get(i).getUserId());
                jo.put("name",selectedUsers.get(i).getName());
                ja.put(jo);
            }

            return  ja.toString();
        }catch (Exception e){
            return null;
        }
    }


    private void searchUser(String phone){
        if(!phone.equals("")){
            tv_status.setVisibility(View.VISIBLE);
            tv_status.setText("... Searching ...");
            pb.setVisibility(View.VISIBLE);
        }
        else {
            pb.setVisibility(View.GONE);
        }

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("ser res" ,response);
                            try {
                                JSONObject jsonObject=new JSONObject(response);
                                boolean success=jsonObject.getString("status").equals("success");
                                if(success){
                                    JSONObject jo=jsonObject.getJSONObject("result");
                                    String userId=jo.getString("user_id");
                                    String name=jo.getString("name");
                                    String profileImage=jo.getString("profile_image");

                                    tv_name.setText(name);
                                    AppUtils.setPhotoFromRealUrl(iv_profile,Routing.PROFILE_URL+profileImage);
                                    resultLayout.setVisibility(View.VISIBLE);
                                    tv_status.setVisibility(View.GONE);
                                    pb.setVisibility(View.GONE);

                                    resultLayout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(!currentUserId.equals(userId)){
                                                selectedUsers.add(new MemberModel(userId,name,Routing.PROFILE_URL+profileImage));
                                                adapter.notifyDataSetChanged();
                                                resultLayout.setVisibility(View.GONE);
                                                et_search.setText("");
                                            }else{
                                                Toast.makeText(getApplicationContext(),"You cannot add yourself",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }

                            }catch (Exception e){

                            }
                        }
                    });


                }
                @Override
                public void onError(String msg) {


                }
            }).url(Routing.SEARCH_USER_BY_PHONE+"?phone="+phone);

            myHttp.runTask();
        }).start();
    }

    public class SelectAdapter extends  RecyclerView.Adapter<SelectAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(AddGroupMemberActivity.this).inflate(R.layout.item_search_select_user,parent,false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            AppUtils.setPhotoFromRealUrl(holder.iv_profile,selectedUsers.get(position).getImageUrl());
            holder.tv_name.setText(selectedUsers.get(position).getName());

        }

        @Override
        public int getItemCount() {
            return selectedUsers.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class Holder extends RecyclerView.ViewHolder{

            ImageView iv_profile,iv_cancel;
            TextView tv_name;

            public Holder(View view){
                super(view);
                iv_profile=view.findViewById(R.id.iv_profile);
                iv_cancel=view.findViewById(R.id.iv_cancel);
                tv_name=view.findViewById(R.id.tv_name);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                iv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedUsers.remove(getAdapterPosition());
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }
}