package com.madmax.acamobile.adapters;



import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.madmax.acamobile.OrderDetailActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.VoucherModel;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.Holder>{

    private final Activity c;
    private final ArrayList<VoucherModel> data;
    private final LayoutInflater mInflater;

    public OrderAdapter(Activity c, ArrayList<VoucherModel> data) {
        this.c = c;
        this.data = data;
        this.mInflater= LayoutInflater.from(c);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_orders,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        VoucherModel model=data.get(position);
        holder.tv_group_name.setText(model.getGroup_name());
        holder.tv_amount.setText("Total amount - "+AppUtils.getTwoDecimalDouble(model.getTotal_amount()));
        holder.tv_voucher_id.setText("VoucherID - "+model.getVoucher_id());
        if(!model.getGroup_image().equals("")) AppUtils.setPhotoFromRealUrl(holder.iv_group,model.getGroup_image());
        holder.tv_date.setText(AppUtils.formatTime(1000*model.getVoucher_id()));


        if(model.isSeen()&&model.isReceived()&& model.isSoldOut()){
            holder.tv_status.setText("Received by agent");
            holder.tv_status.setTextColor(Color.GREEN);
            holder.orderLayout.setBackgroundColor(c.getResources().getColor(R.color.white));
        }else if(model.isSeen()&&model.isSoldOut()){
            holder.tv_status.setText("Sent to Agent");
            holder.tv_status.setTextColor(Color.RED);
            holder.orderLayout.setBackgroundColor(c.getResources().getColor(R.color.white));
        }else if(!model.isSeen()){
            holder.tv_status.setText("");
            holder.orderLayout.setBackgroundColor(c.getResources().getColor(R.color.noti_color));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv_voucher_id,tv_amount,tv_date,tv_status,tv_group_name;
        ImageView iv_group;
        RelativeLayout orderLayout;
        public Holder(@NonNull View itemView) {
            super(itemView);
            tv_voucher_id=itemView.findViewById(R.id.tv_voucher_id);
            tv_amount=itemView.findViewById(R.id.tv_amount);
            tv_date=itemView.findViewById(R.id.tv_date);
            tv_status=itemView.findViewById(R.id.tv_status);
            tv_group_name=itemView.findViewById(R.id.tv_group_name);
            iv_group=itemView.findViewById(R.id.iv_group);
            orderLayout=itemView.findViewById(R.id.order_layout);

            iv_group.setClipToOutline(true);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VoucherModel model=data.get(getAdapterPosition());
                    Intent intent=new Intent(c, OrderDetailActivity.class);
                    intent.putExtra("voucher_id",model.getVoucher_id()+"");
                    c.startActivity(intent);
                }
            });
        }
    }
}
