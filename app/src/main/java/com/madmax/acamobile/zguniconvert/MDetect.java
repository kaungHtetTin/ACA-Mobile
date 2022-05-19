package com.madmax.acamobile.zguniconvert;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

public class MDetect {
    private int TYPE_DEFAULT = 0;
    private int TYPE_UNICODE = 1;
    private int TYPE_ZAWGYI = 2;

    private int isUnicode = TYPE_DEFAULT;

    public  void init(Context context) {
        if (isUnicode != TYPE_DEFAULT) {
            Log.i("MDetect", "MDetect was already initialized.");
            return;
        }
        TextView textView = new TextView(context, null);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        textView.setText("\u1000");
        textView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int length1 = textView.getMeasuredWidth();

        textView.setText("\u1000\u1039\u1000");
        textView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int length2 = textView.getMeasuredWidth();

        Log.i("MDetect", length1 + ", " + length2);

        if(length1==length2){
            isUnicode=TYPE_UNICODE;
        }else{
            isUnicode=TYPE_ZAWGYI;
        }


    }

    /**
     * method for getting user's encoding

     * @return whether the device follows myanmar unicode standard
     */


    public boolean isUnicode(){
        if(isUnicode==TYPE_DEFAULT){
            throw new UnsupportedOperationException("MDetect was not initialized.");
        }
        return isUnicode==TYPE_UNICODE;
    }


//     * method for getting display text
//     * @param unicodeString Unicode String
//     * @return appropriate String according to device's encoding



    public String getText(String s){
        if(isUnicode()){
            return s;
        }else{
            return Rabbit.uni2zg(s);
        }
    }

    //change Input String to Unicode
    public String changeUnicode(String s){
        if(isUnicode()){
            return s;
        }else {
            return Rabbit.zg2uni(s);
        }
    }


}
