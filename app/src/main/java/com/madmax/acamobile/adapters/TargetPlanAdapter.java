package com.madmax.acamobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.madmax.acamobile.R;
import com.madmax.acamobile.TargetPlanDetailActivity;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.TargetPlanModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Executor;


public class TargetPlanAdapter extends RecyclerView.Adapter<TargetPlanAdapter.Holder> {

    private final Activity c;
    private final ArrayList<TargetPlanModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    String userId,authToken;
    Executor postExecutor;

    public TargetPlanAdapter(Activity c, ArrayList<TargetPlanModel> data){
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
    public TargetPlanAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_target_plan,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TargetPlanAdapter.Holder holder, int position) {

        try{
            TargetPlanModel model=data.get(position);
            long startDate=model.getStartDate()*1000;
            long endDate=model.getEndDate()*1000;

            Calendar startCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            startCalendar.setTimeInMillis(startDate);
            int startDay=startCalendar.get(Calendar.DAY_OF_MONTH);
            int startMonth=startCalendar.get(Calendar.MONTH);
            int startYear=startCalendar.get(Calendar.YEAR);

            Calendar endCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            endCalendar.setTimeInMillis(endDate);
            int endDay=endCalendar.get(Calendar.DAY_OF_MONTH);
            int endMonth=endCalendar.get(Calendar.MONTH);
            int endYear=endCalendar.get(Calendar.YEAR);

            String plan="From "+ AppUtils.month[startMonth] +" "+startDay+", "+startYear+"    To "+AppUtils.month[endMonth]+" "+endDay+", "+endYear;
            holder.tv.setText(plan);

        }catch (Exception e){
            Toast.makeText(c,e.toString(),Toast.LENGTH_SHORT).show();
        }

    }

    public class Holder extends RecyclerView.ViewHolder{


        TextView tv;


        public Holder(View view){
            super(view);

            tv=view.findViewById(R.id.tv_target_plan);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TargetPlanModel model=data.get(getAdapterPosition());
                    Intent intent=new Intent(c, TargetPlanDetailActivity.class);
                    intent.putExtra("startDate",model.getStartDate());
                    intent.putExtra("endDate",model.getEndDate());
                    intent.putExtra("targetPlanID",model.getPlanId());
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
