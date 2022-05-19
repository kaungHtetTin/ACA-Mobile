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

import com.madmax.acamobile.DataUpdateActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.ProductModel;
import java.util.ArrayList;


public class TargetPlanProductAdapter extends RecyclerView.Adapter<TargetPlanProductAdapter.Holder> {

    private final Activity c;
    private final ArrayList<ProductModel> data;
    private final LayoutInflater mInflater;
    private final int targetPlanId;

    public TargetPlanProductAdapter(Activity c, ArrayList<ProductModel> data,int targetPlanId){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);
        this.targetPlanId=targetPlanId;

    }


    @NonNull
    @Override
    public TargetPlanProductAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_target_plan_product,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TargetPlanProductAdapter.Holder holder, int position) {

        try{
            ProductModel model=data.get(position);
            holder.tv.setText(model.getProduct_name());

        }catch (Exception e){
            Toast.makeText(c,e.toString(),Toast.LENGTH_SHORT).show();
        }

    }

    public class Holder extends RecyclerView.ViewHolder{


        TextView tv;

        public Holder(View view){
            super(view);

            tv=view.findViewById(R.id.tv_product_name);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProductModel model=data.get(getAdapterPosition());

                    Intent intent=new Intent(c, DataUpdateActivity.class);
                    intent.putExtra("hint","Enter items quantity");
                    intent.putExtra("message","Update items quantity for "+model.getProduct_name()+" in target plan");
                    intent.putExtra("key","count");
                    intent.putExtra("extra1",targetPlanId+"");
                    intent.putExtra("contentId",model.getProduct_id()+"");
                    intent.putExtra("url", Routing.UPDATE_TARGET_PLAN_ITEM_COUNT);
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
