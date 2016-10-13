package com.balinasoft.clever.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;

import com.balinasoft.clever.R;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.res.StringRes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@EBean
public class SharingHelper {

    @RootContext
    Context mContext;

    @StringRes(R.string.share)
    String mShareMessage;

    public void share(String text) {
        Intent myShareIntent = new Intent(Intent.ACTION_SEND);
//        if(imageView != null) {
//            Uri uri = getLocalBitmapUri(imageView);
//            if(uri != null) {
//                myShareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//                myShareIntent.setType("image/*");
//            }
//        }
        myShareIntent.setType("text/plain");
        myShareIntent.putExtra(Intent.EXTRA_TEXT,text);
        myShareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        mContext.startActivity(Intent.createChooser(myShareIntent, mShareMessage));
    }


    private Uri getLocalBitmapUri(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp;
        if (drawable instanceof GlideBitmapDrawable){
            bmp = ((GlideBitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }

        Uri bmpUri = null;
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            boolean isCreated = file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}
