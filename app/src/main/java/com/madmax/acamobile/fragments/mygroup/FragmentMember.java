package com.madmax.acamobile.fragments.mygroup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madmax.acamobile.AddGroupMemberActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.adapters.GroupMemberAdapter;
import com.madmax.acamobile.app.AuthChecker;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.MemberModel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class FragmentMember extends Fragment {
    View v;

    AppBarLayout mAppBarLayout;
    String groupId;
    String userId;
    FloatingActionButton fab;
    TextView tv_msg;
    RecyclerView recyclerView;
    ProgressBar pb;

    SharedPreferences sharedPreferences;
    Executor postExecutor;

    ArrayList<MemberModel> members=new ArrayList<>();
    GroupMemberAdapter adapter;

    int page=1;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;

    public FragmentMember(AppBarLayout mAppBarLayout, String groupId){
        this.mAppBarLayout=mAppBarLayout;
        this.groupId=groupId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v= inflater.inflate(R.layout.fragment_my_group_menber, container, false);

        sharedPreferences=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);

        postExecutor= ContextCompat.getMainExecutor(getActivity());
        members.clear();
        setUpView();
        mAppBarLayout.setExpanded(false);

        return v;
    }

    private void setUpView(){
        fab=v.findViewById(R.id.fab_addGpMember);
        tv_msg=v.findViewById(R.id.tv_msg);
        recyclerView=v.findViewById(R.id.recyclerView);
        pb=v.findViewById(R.id.pb);

        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new GroupMemberAdapter(getActivity(),members,groupId);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), AddGroupMemberActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                pastVisibleItems=lm.findFirstVisibleItemPosition();

                if(dy>0){
                    visibleItemCount=lm.getChildCount();
                    totalItemCount=lm.getItemCount();

                    if(loading){
                        if((visibleItemCount+pastVisibleItems)>=totalItemCount-7){
                            loading=false;
                            page++;
                            fetchMembers(page);

                        }
                    }


                }
            }
        });
    }


    private void fetchMembers(int page){
        pb.setVisibility(View.VISIBLE);
        if(userId!=null){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                pb.setVisibility(View.GONE);
                                setResult(response);

                                if (members.size()>0){
                                    tv_msg.setVisibility(View.GONE);
                                }else{
                                    tv_msg.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("Err MY GP ",msg);
                        pb.setVisibility(View.GONE);

                    }
                }).url(Routing.GET_GROUP_MEMBERS+"?group_id="+groupId+"&page="+page);

                myHttp.runTask();
            }).start();
        }else{
            new AuthChecker(getActivity()).logout();
        }
    }

    private void setResult(String response){
        try {
            JSONArray ja=new JSONObject(response).getJSONArray("members");
            for (int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String name=jo.getString("name");
                String userId=jo.getString("user_id");
                String imageUrl=Routing.PROFILE_URL+jo.getString("profile_image");
                members.add(new MemberModel(userId,name,imageUrl));
            }
            adapter.notifyDataSetChanged();
            loading=true;

        }catch (Exception e){}
    }


    @Override
    public void onResume() {
        super.onResume();
        page=1;
        loading=true;
        members.clear();
        fetchMembers(1);
    }

}
