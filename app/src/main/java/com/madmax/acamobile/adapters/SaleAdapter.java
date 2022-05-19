package com.madmax.acamobile.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.madmax.acamobile.InvoiceActivity;
import com.madmax.acamobile.OrderDetailActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.VoucherActivity;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.models.VoucherModel;

import java.util.ArrayList;

public class SaleAdapter extends RecyclerView.Adapter<SaleAdapter.Holder>{

    private final Activity c;
    private final ArrayList<VoucherModel> data;
    private final LayoutInflater mInflater;

    public SaleAdapter(Activity c, ArrayList<VoucherModel> data) {
        this.c = c;
        this.data = data;
        this.mInflater= LayoutInflater.from(c);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_sale,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        VoucherModel model=data.get(position);
        holder.tv_amount.setText("Total amount - "+AppUtils.getTwoDecimalDouble(model.getTotal_amount()));
        holder.tv_voucher_id.setText("VoucherID - "+model.getVoucher_id());
        holder.tv_date.setText(AppUtils.formatTime(model.getVoucher_id()*1000));
        if(model.isAgent()){
            holder.tv_customer_name.setText("Agent - "+model.getCustomer_name());
        }else{
            holder.tv_customer_name.setText("Retail - "+model.getCustomer_name());
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv_voucher_id,tv_amount,tv_date,tv_customer_name;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tv_voucher_id=itemView.findViewById(R.id.tv_voucher_id);
            tv_amount=itemView.findViewById(R.id.tv_amount);
            tv_date=itemView.findViewById(R.id.tv_date);
            tv_customer_name=itemView.findViewById(R.id.tv_customer_name);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VoucherModel model=data.get(getAdapterPosition());
                    Intent intent=new Intent(c, InvoiceActivity.class);
                    intent.putExtra("total_amount",model.getTotal_amount());
                    intent.putExtra("voucher_id",model.getVoucher_id());
                    intent.putExtra("sale",true);
                    c.startActivity(intent);
                }
            });
        }
    }
}
