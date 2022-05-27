package com.madmax.acamobile.app;


import android.content.Context;
import android.widget.ImageView;

import com.madmax.acamobile.R;
import com.madmax.acamobile.models.ProductModel;
import com.madmax.acamobile.zguniconvert.MDetect;
import com.madmax.acamobile.zguniconvert.Rabbit;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AppUtils {

    public static final DecimalFormat df = new DecimalFormat("0.00");

    public static void setPhotoFromRealUrl(ImageView iv, String url){
        Picasso.get()
                .load(url)
                .centerInside()
                .fit()
                .error(R.drawable.ic_launcher_foreground)
                .into(iv, new Callback() {
                    @Override
                    public void onSuccess() {}
                    @Override
                    public void onError(Exception e) {
                    }
                });
    }


    public static String formatTime( long time){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        Date resultdate = new Date(time);
        long currentTime=System.currentTimeMillis();
        long timeDifference=currentTime-time;
        long s=1000;
        long min=s*60;
        long hour=min*60;
        long day=hour*24;
        if(timeDifference<min)return timeDifference/s+" s ago";
        else if(timeDifference>min&&timeDifference<hour) return timeDifference/min+" min ago";
        else if(timeDifference>hour&&timeDifference<day) return timeDifference/hour+ " h ago";
        else return sdf.format(resultdate);

    }


    public static String  getTwoDecimalDouble(Double d){
        DecimalFormat dfrmt = new DecimalFormat();
        dfrmt.setMaximumFractionDigits(2);

        return dfrmt.format(d);
    }

    public static ProductModel findProductById(int product_id){
        for(int i=0;i< Initializer.products.size();i++){
            ProductModel p=Initializer.products.get(i);
            if(p.getProduct_id()==product_id){
                return p;
            }
        }
        return null;
    }

    public static String [] month={"Jan","Feb","March","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

}
