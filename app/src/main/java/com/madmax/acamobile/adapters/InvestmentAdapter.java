package com.madmax.acamobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.madmax.acamobile.R;
import com.madmax.acamobile.models.InvestmentModel;

import java.util.ArrayList;


public class InvestmentAdapter extends RecyclerView.Adapter<InvestmentAdapter.Holder> {

    private final Activity c;
    private final ArrayList <InvestmentModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    final String currentUserName;
    public InvestmentAdapter(Activity c, ArrayList<InvestmentModel> data){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserName=sharedPreferences.getString("userName",null);

    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_investment,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        try{
            InvestmentModel model=data.get(position);
            holder.tv_name.setText(model.getProductName());
            holder.tv_quantity.setText(model.getQuantity()+"");
            holder.tv_amount.setText(model.getTotalAmount()+"");


        }catch (Exception ignored){

        }

    }

    public class Holder extends RecyclerView.ViewHolder{

       TextView tv_name,tv_quantity,tv_amount;

        public Holder(View view){
            super(view);
            tv_name=view.findViewById(R.id.tv_product_name);
            tv_quantity=view.findViewById(R.id.tv_quantity);
            tv_amount=view.findViewById(R.id.tv_amount);


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
