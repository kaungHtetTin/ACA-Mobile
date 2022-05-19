package com.madmax.acamobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.madmax.acamobile.R;
import com.madmax.acamobile.app.Price;
import com.madmax.acamobile.models.ProductModel;

import java.util.ArrayList;
import java.util.concurrent.Executor;


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.Holder> {

    private final Activity c;
    private final ArrayList<ProductModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    String userId,authToken;
    Executor postExecutor;

    public ProductAdapter(Activity c, ArrayList<ProductModel> data){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        postExecutor= ContextCompat.getMainExecutor(c);

    }


    @NonNull
    @Override
    public ProductAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_product,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.Holder holder, int position) {

        try{
           ProductModel model=data.get(position);

            holder.tv_product_name.setText(model.getProduct_name());
            holder.tv_retail.setText(model.getPrice(Price.retail_price)+"");
            holder.tv_5.setText(model.getPrice(Price.five_price)+"");
            holder.tv_10.setText(model.getPrice(Price.ten_price)+"");
            holder.tv_20.setText(model.getPrice(Price.twenty_price)+"");
            holder.tv_50.setText(model.getPrice(Price.fifty_price)+"");
            holder.tv_100.setText(model.getPrice(Price.hundred_price)+"");
            holder.tv_200.setText(model.getPrice(Price.two_hundred_price)+"");
            holder.tv_300.setText(model.getPrice(Price.three_hundred_price)+"");
            holder.tv_500.setText(model.getPrice(Price.five_hundred_price)+"");
            holder.tv_1000.setText(model.getPrice(Price.thousand_price)+"");
            holder.tv_2000.setText(model.getPrice(Price.two_thousand_price)+"");
            holder.tv_3000.setText(model.getPrice(Price.three_thousand_price)+"");
            holder.tv_5000.setText(model.getPrice(Price.five_thousand_price)+"");

            holder.tv_point.setText(model.getPoint()+"");
            holder.tv_discount.setText(model.getDiscount()+"");

            if(position%2==0){
                holder.layout.setBackgroundColor(c.getResources().getColor(R.color.noti_color));
            }

        }catch (Exception e){
            Toast.makeText(c,e.toString(),Toast.LENGTH_SHORT).show();
        }

    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv_product_name,tv_retail,tv_5,tv_10,tv_20,tv_50,tv_100,tv_200,tv_300,tv_500,
                tv_1000,tv_2000,tv_3000,tv_5000,tv_point,tv_discount;

        LinearLayout layout;


        public Holder(View view){
            super(view);
           tv_product_name=view.findViewById(R.id.tv_product_name);
           tv_retail=view.findViewById(R.id.tv_retail_price);
           tv_5=view.findViewById(R.id.tv_5_price);
           tv_10=view.findViewById(R.id.tv_10_price);
           tv_20=view.findViewById(R.id.tv_20_price);
           tv_50=view.findViewById(R.id.tv_50_price);
           tv_100=view.findViewById(R.id.tv_100_price);
           tv_200=view.findViewById(R.id.tv_200_price);
           tv_300=view.findViewById(R.id.tv_300_price);
           tv_500=view.findViewById(R.id.tv_500_price);
           tv_1000=view.findViewById(R.id.tv_1000_price);
           tv_2000=view.findViewById(R.id.tv_2000_price);
           tv_3000=view.findViewById(R.id.tv_3000_price);
           tv_5000=view.findViewById(R.id.tv_5000_price);
           tv_point=view.findViewById(R.id.tv_point);
           tv_discount=view.findViewById(R.id.tv_discount);

           layout=view.findViewById(R.id.product_layout);

           view.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   ProductModel model=data.get(getAdapterPosition());
                   Toast.makeText(c,model.getProduct_name(),Toast.LENGTH_SHORT).show();
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
