package com.madmax.acamobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.madmax.acamobile.R;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.models.OrderModel;


import java.util.ArrayList;
import java.util.concurrent.Executor;


public class InvoiceProductAdapter extends RecyclerView.Adapter<InvoiceProductAdapter.Holder> {

    private final Activity c;
    private final ArrayList<OrderModel> data;
    private final LayoutInflater mInflater;

    final SharedPreferences sharedPreferences;
    String userId,authToken;
    Executor postExecutor;

    public InvoiceProductAdapter(Activity c, ArrayList<OrderModel> data){
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
    public InvoiceProductAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_invoice,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceProductAdapter.Holder holder, int position) {
        OrderModel model=data.get(position);
        holder.tv_no.setText((position+1)+"");
        holder.tv_item.setText(model.getProductName());
        holder.tv_qty.setText(model.getQuantity()+"");
        holder.tv_price.setText(model.getPrice()+"");
        holder.tv_amount.setText(AppUtils.getTwoDecimalDouble(model.getAmount()));
        holder.tv_foc.setText(model.getFoc()+"");

    }



    public class Holder extends RecyclerView.ViewHolder{

        TextView tv_no,tv_item,tv_qty,tv_price,tv_amount,tv_foc;

        public Holder(View view){
            super(view);

            tv_no=view.findViewById(R.id.tv_no);
            tv_item=view.findViewById(R.id.tv_item);
            tv_qty=view.findViewById(R.id.tv_qty);
            tv_price=view.findViewById(R.id.tv_price);
            tv_amount=view.findViewById(R.id.tv_amount);
            tv_foc=view.findViewById(R.id.tv_foc);
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
