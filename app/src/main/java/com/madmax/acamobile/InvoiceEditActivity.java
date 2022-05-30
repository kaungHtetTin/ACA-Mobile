package com.madmax.acamobile;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.RealPathUtil;
import com.madmax.acamobile.app.Routing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class InvoiceEditActivity extends AppCompatActivity {

    String invoice_name,invoice_phone,invoice_address,logo_extension;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    EditText et_invoice_name,et_invoice_phone,et_invoice_address;
    Button bt_save;
    ImageView iv_logo,iv_icon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        invoice_address=sharedPreferences.getString("invoice_address","No Address");
        invoice_name=sharedPreferences.getString("invoice_name","No Name");
        invoice_phone=sharedPreferences.getString("invoice_phone","No Phone");
        logo_extension=sharedPreferences.getString("logo_extension","");
        editor=sharedPreferences.edit();
        setContentView(R.layout.activity_invoice_edit);
        setTitle("Edit");

        setUpView();

    }

    private void setUpView(){
        et_invoice_address=findViewById(R.id.et_invoice_address);
        et_invoice_name=findViewById(R.id.et_invoice_name);
        et_invoice_phone=findViewById(R.id.et_invoice_phone);
        bt_save=findViewById(R.id.bt_save);

        iv_logo=findViewById(R.id.iv_logo);
        iv_icon=findViewById(R.id.iv_icon);

        et_invoice_phone.setText(invoice_phone);
        et_invoice_name.setText(invoice_name);
        et_invoice_address.setText(invoice_address);

        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("invoice_name",et_invoice_name.getText().toString());
                editor.putString("invoice_phone",et_invoice_phone.getText().toString());
                editor.putString("invoice_address",et_invoice_address.getText().toString());
                editor.apply();
                finish();
            }
        });

        iv_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionGranted()){
                    pickImageFromGallery();
                }else {
                    takePermission();
                }
            }
        });

        iv_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionGranted()){
                    pickImageFromGallery();
                }else {
                    takePermission();
                }
            }
        });

        setLogo();

    }

    private void setLogo(){
        Bitmap bitmap;
        byte[] art=getFileByte("invoice_logo."+logo_extension,getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        if(art!=null){
            bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
            iv_logo.setImageBitmap(bitmap);
        }
    }

    private byte[] getFileByte(String title, String dir){
        byte [] buffer;

        try {
            InputStream is=new BufferedInputStream(new FileInputStream(dir+"/"+title));
            int size=is.available();
            buffer=new byte[size];
            is.read(buffer);
            is.close();

            return  buffer;

        }catch (Exception e){
            return null;
        }
    }



    private boolean isPermissionGranted(){
        int  readExternalStorage= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return  readExternalStorage== PackageManager.PERMISSION_GRANTED;
    }

    private void takePermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
    }


    private void pickImageFromGallery(){
        mGetContent.launch("image/*");
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if(uri!=null){
                        iv_logo.setImageURI(uri);
                        saveImageInInternalStorage(RealPathUtil.getRealPath(InvoiceEditActivity.this,uri));
                    }else{
                        Toast.makeText(getApplicationContext(),"No photo is selected!",Toast.LENGTH_SHORT).show();
                    }
                }
            });


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            if(requestCode==101){
                boolean readExternalStorage=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(readExternalStorage){
                    pickImageFromGallery();
                }else {
                    takePermission();
                }
            }
        }
    }

    private void saveImageInInternalStorage(String path){

        int i = path.lastIndexOf('.');
        if (i > 0) {
            logo_extension = path.substring(i+1);
        }
        editor.putString("logo_extension",logo_extension);

        try {
            InputStream inputStream=new BufferedInputStream(new FileInputStream(path));
            int size=inputStream.available();
            byte[] buffer=new byte[size];
            File image= new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath(),"invoice_logo."+logo_extension);
            OutputStream output=new FileOutputStream(image);
            while (inputStream.read(buffer)!=-1){
                output.write(buffer);
            }

            output.flush();
            output.close();
            inputStream.close();

        }catch (IOException e){
            Log.e("imageSave ",e.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}