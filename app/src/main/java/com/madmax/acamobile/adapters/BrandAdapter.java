package com.madmax.acamobile.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.madmax.acamobile.CreateOrderActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.VoucherActivity;
import com.madmax.acamobile.models.BrandModel;


import java.util.ArrayList;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.Holder>{

    private final Activity c;
    private final ArrayList<BrandModel> data;
    private final LayoutInflater mInflater;
    private final String key;
    private String customer;

    public BrandAdapter(Activity c, ArrayList<BrandModel> data,String key,String customer) {
        this.c = c;
        this.data = data;
        this.mInflater= LayoutInflater.from(c);
        this.key=key;
        this.customer=customer;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_brand,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        BrandModel model=data.get(position);
        holder.tv.setText(model.getBrand_name());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv;
        public Holder(@NonNull View itemView) {
            super(itemView);
            tv=itemView.findViewById(R.id.tv_brand);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BrandModel model=data.get(getAdapterPosition());
                    Intent intent;
                    if(key.equals("voucher")){
                        intent = new Intent(c, VoucherActivity.class);
                        if(customer!=null) intent.putExtra("customer",customer);
                    }else{
                        intent = new Intent(c, CreateOrderActivity.class);
                    }
                    intent.putExtra("brand_id",model.getBrand_id());
                    c.startActivity(intent);
                }
            });
        }
    }
}
