package com.madmax.acamobile.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.madmax.acamobile.R;


public class MyDialog {

    private final Activity c;
    private final String title;
    private final String msg;
    ConfirmClick confirmClick;

    public MyDialog(Activity c, String title, String msg, ConfirmClick confirmClick) {
        this.c = c;
        this.title = title;
        this.msg = msg;
        this.confirmClick=confirmClick;
    }

    public void showMyDialog(){
        final AlertDialog ad = new AlertDialog.Builder(c).create();
        ad.setTitle(title);
        ad.setIcon(R.mipmap.ic_launcher);
        ad.setMessage(msg);
        ad.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                confirmClick.onConfirmClick();
            }
        });
        ad.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                ad.dismiss();
            }
        });
        ad.show();
    }

    public interface ConfirmClick{
        void onConfirmClick();
    }

}