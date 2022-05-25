package com.madmax.acamobile.fragments.mygroup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.DocumentsContract;
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

import com.madmax.acamobile.R;
import com.madmax.acamobile.adapters.OrderAdapter;
import com.madmax.acamobile.app.AuthChecker;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.VoucherModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class FragmentOrder extends Fragment {
    View v;

    RecyclerView recyclerView;
    ProgressBar pb;
    TextView tv_msg;

    ArrayList<VoucherModel> vouchers=new ArrayList<>();
    OrderAdapter adapter;

    int page=1;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;

    SharedPreferences sharedPreferences;
    Executor postExecutor;
    String userId;

    String groupId;
    int isSoldOut;

    public FragmentOrder(String groupId,int isSoldOut){
        this.groupId=groupId;
        this.isSoldOut=isSoldOut;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v= inflater.inflate(R.layout.fragment_my_group_order, container, false);

        sharedPreferences=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        postExecutor= ContextCompat.getMainExecutor(getActivity());

        setUpView();


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        page=1;
        loading=true;
        vouchers.clear();
        fetchOrders(page);
    }

    private void setUpView(){
        recyclerView=v.findViewById(R.id.recyclerView);
        pb=v.findViewById(R.id.pb);
        tv_msg=v.findViewById(R.id.tv_msg);

        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter=new OrderAdapter(getActivity(),vouchers);
        recyclerView.setAdapter(adapter);

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
                            fetchOrders(page);

                        }
                    }
                }
            }
        });

    }

    private void fetchOrders(int page){
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


                                if (vouchers.size()>0){
                                    tv_msg.setVisibility(View.GONE);
                                }else{
                                    tv_msg.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("ErrOrders ",msg);
                        pb.setVisibility(View.GONE);

                    }
                }).url(Routing.GET_ORDERS+"?user_id="+userId+"&is_sold_out="+isSoldOut+"&page="+page+"&group_id="+groupId);

                myHttp.runTask();
            }).start();
        }else{
            new AuthChecker(getActivity()).logout();
        }
    }

    private void setResult(String response){
        try {

            JSONArray ja=new JSONObject(response).getJSONArray("orders");
            for (int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                long voucher_id=jo.getLong("voucher_id");
                double total_amount=jo.getDouble("total_amount");
                String name=jo.getString("name");
                String image=jo.getString("profile_image");
                boolean seen=jo.getInt("seen")==1;
                boolean received=jo.getInt("is_received")==1;
                boolean is_sold_out=jo.getInt("is_sold_out")==1;

                vouchers.add(new VoucherModel(voucher_id,total_amount,name, Routing.PROFILE_URL+image,seen,received,is_sold_out));

            }
            adapter.notifyDataSetChanged();
            loading=true;

        }catch (Exception e){}
    }

}
