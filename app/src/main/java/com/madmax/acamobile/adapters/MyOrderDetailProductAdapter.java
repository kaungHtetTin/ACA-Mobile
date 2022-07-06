package com.madmax.acamobile.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.madmax.acamobile.R;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Price;
import com.madmax.acamobile.models.OrderModel;
import com.madmax.acamobile.models.ProductModel;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class MyOrderDetailProductAdapter extends RecyclerView.Adapter<MyOrderDetailProductAdapter.Holder> {

    private final Activity c;
    private final ArrayList<OrderModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    String userId,authToken;
    Executor postExecutor;


    public MyOrderDetailProductAdapter(Activity c, ArrayList<OrderModel> data){
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
    public MyOrderDetailProductAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_my_order_detail,parent,false);
        return new Holder(view);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyOrderDetailProductAdapter.Holder holder, int position) {

        OrderModel model=data.get(position);
        holder.tv_point.setText(model.getPoint()+"");
        holder.tv_price.setText(AppUtils.getTwoDecimalDouble((double)model.getPrice()));
        holder.tv_amount.setText(AppUtils.getTwoDecimalDouble(model.getAmount()));
        holder.tv_discount.setText(model.getDiscount()+"");
        holder.tv_quantity.setText(model.getQuantity()+"");
        holder.tv_foc.setText(model.getFoc()+"");

        ProductModel p=AppUtils.findProductById(model.getProduct_id());
        if(p!=null){
            holder.tv_product_name.setText(p.getProduct_name());
            holder.tv_retail.setText(AppUtils.getTwoDecimalDouble((double)p.getPrice(Price.retail_price)));
        }

    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv_point,tv_price,tv_amount,tv_retail,tv_discount,tv_product_name,tv_quantity,
                tv_foc;
        LinearLayout mLayout;

        public Holder(View view){
            super(view);
            tv_product_name=view.findViewById(R.id.tv_product_name);
            tv_point=view.findViewById(R.id.tv_point);
            tv_price=view.findViewById(R.id.tv_price);
            tv_amount=view.findViewById(R.id.tv_phone);
            tv_quantity=view.findViewById(R.id.tv_quantity);
            tv_foc=view.findViewById(R.id.tv_foc);
            tv_retail=view.findViewById(R.id.tv_retail);
            tv_discount=view.findViewById(R.id.tv_discount);

            mLayout=view.findViewById(R.id.mLayout);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

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
