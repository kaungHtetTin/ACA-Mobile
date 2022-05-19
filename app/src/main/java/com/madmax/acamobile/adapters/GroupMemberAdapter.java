package com.madmax.acamobile.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.madmax.acamobile.ProfileActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.models.MemberModel;
import java.util.ArrayList;


public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.Holder> {

    private final Activity c;
    private final ArrayList<MemberModel> data;
    private final LayoutInflater mInflater;
    String groupId;

    public GroupMemberAdapter(Activity c, ArrayList<MemberModel> data,String groupId){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);

        this.groupId=groupId;
    }


    @NonNull
    @Override
    public GroupMemberAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_menber,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberAdapter.Holder holder, int position) {
        try{
            MemberModel model=data.get(position);
            AppUtils.setPhotoFromRealUrl(holder.iv,model.getImageUrl());
            holder.tv_name.setText(model.getName());

        }catch (Exception e){

        }


    }

    public class Holder extends RecyclerView.ViewHolder{

        ImageView iv;
        TextView tv_name;

        public Holder(View view){
            super(view);

            iv=view.findViewById(R.id.iv_profile);
            tv_name=view.findViewById(R.id.tv_name);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MemberModel model=data.get(getAdapterPosition());
                    Intent intent=new Intent(c, ProfileActivity.class);
                    intent.putExtra("groupId",groupId);
                    intent.putExtra("memberId",model.getUserId());
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
