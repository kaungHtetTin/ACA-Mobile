package com.madmax.acamobile.adapters;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.madmax.acamobile.MyGroupDetailActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.MyGroupModel;
import java.util.ArrayList;


public class MyGroupAdapter extends RecyclerView.Adapter<MyGroupAdapter.Holder> {

    private final Activity c;
    private final ArrayList<MyGroupModel> data;
    private final LayoutInflater mInflater;

    public MyGroupAdapter(Activity c, ArrayList<MyGroupModel> data){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);

    }


    @NonNull
    @Override
    public MyGroupAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_my_group,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyGroupAdapter.Holder holder, int position) {
        try{
            MyGroupModel model=data.get(position);
            holder.tv_group_name.setText(model.getName());
            holder.tv_group_status.setText(model.getDescription());
            if(!model.getImageUrl().equals(""))
                AppUtils.setPhotoFromRealUrl(holder.iv_group_thumb, Routing.GROUP_COVER_URL+model.getImageUrl());

        }catch (Exception e){
            Log.e("Error GP Adapter ",e.toString());
        }


    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv_group_name,tv_group_status;
        ImageView iv_group_thumb;
        public Holder(View view){
            super(view);
            tv_group_name=view.findViewById(R.id.tv_address);
            tv_group_status=view.findViewById(R.id.tv_group_status);
            iv_group_thumb=view.findViewById(R.id.iv_group_thumb);
            iv_group_thumb.setClipToOutline(true);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyGroupModel model=data.get(getAdapterPosition());
                    Intent intent=new Intent(c, MyGroupDetailActivity.class);
                    intent.putExtra("groupId",model.getGroup_id());
                    intent.putExtra("imageUrl",model.getImageUrl());
                    intent.putExtra("name",model.getName());
                    intent.putExtra("description",model.getDescription());
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
