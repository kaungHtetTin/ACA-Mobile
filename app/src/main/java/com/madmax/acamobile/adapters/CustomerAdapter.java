package com.madmax.acamobile.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.madmax.acamobile.BrandListActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.SaleListActivity;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.models.AppModel;
import com.madmax.acamobile.models.CustomerModel;
import com.madmax.acamobile.zguniconvert.MDetect;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.Holder> {

    private final Activity c;
    private final ArrayList<CustomerModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    final String currentUserName;
    public CustomerAdapter(Activity c, ArrayList<CustomerModel> data){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserName=sharedPreferences.getString("userName",null);

    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_customer,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        try{
            CustomerModel model=data.get(position);
            String name=model.getName();
            String phone= model.getPhone();
            String address=model.getAddress();
            boolean agent=model.isAgent();

            holder.tv_address.setText(address);
            holder.tv_name.setText(name);
            holder.tv_phone.setText(phone);

            if(agent){
                holder.iv.setVisibility(View.VISIBLE);
            }else{
                holder.iv.setVisibility(View.GONE);
            }


        }catch (Exception ignored){

        }

    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv_name,tv_phone,tv_address,tv_new_voucher;
        ImageView iv;

        public Holder(View view){
            super(view);
            tv_name=view.findViewById(R.id.tv_name);
            tv_phone=view.findViewById(R.id.tv_phone);
            tv_address=view.findViewById(R.id.tv_address);
            iv=view.findViewById(R.id.iv_blue_mark);
            tv_new_voucher=view.findViewById(R.id.tv_new_voucher);

            view.setOnClickListener(v -> {
                CustomerModel model=data.get(getAdapterPosition());
                Intent intent=new Intent(c, SaleListActivity.class);
                intent.putExtra("customer_phone",model.getPhone());
                c.startActivity(intent);
            });

            tv_new_voucher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomerModel model=data.get(getAdapterPosition());
                    String name=model.getName();
                    String phone=model.getPhone();
                    String address= model.getAddress();

                    JSONObject jo=new JSONObject();
                    try {
                        jo.put("name",name);
                        jo.put("phone",phone);
                        jo.put("address",address);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    Intent intent=new Intent(c, BrandListActivity.class);
                    intent.putExtra("key","voucher");
                    intent.putExtra("customer",jo.toString());
                    c.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


}
