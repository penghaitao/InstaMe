package com.wartechwick.instame.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

import com.wartechwick.instame.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by penghaitao on 2015/12/21.
 */
public class Utils {

    // TODO: 2017-05-26 distinguish request sources
    final public static int REQUEST_CODE_ASK_PERMISSIONS = 123;

    public static void init(Context context) {
        mkDirs(getImageDirectory(context));
    }

    public static void mkDirs(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static String getImageDirectory(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory() + "/InstantMe/";
        } else {
            return context.getCacheDir() + "/InstantMe/";
        }
    }

    public static Uri saveImage(ImageView itemView, String filename, Context context) {
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) itemView.getDrawable());
        if (bitmapDrawable != null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Uri contentUri = getImageUri(bitmap, filename, context);
            IntentUtils.savetoAlbum(contentUri, context);
            return contentUri;
        } else {
            return null;
        }
    }

    public static Uri getImageUri(Bitmap bitmap, String filename, Context context) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        String path = Utils.getImageDirectory(context);
        File file = new File(path, filename);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(file);
    }

    public static boolean verifyStoragePermissions(Activity context) {

        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
//            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                showMessageOKCancel("You need to allow access to your storage, So we can save the picture",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                                        REQUEST_CODE_ASK_PERMISSIONS);
//                            }
//                        });
//                return false;
//            }
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return false;
        } else {
            Utils.init(context);
            return true;
        }

    }

    public static void showHelpMessage(final Activity activity, String title) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(R.string.insta_help)
                .setPositiveButton(R.string.watch_demo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/FMOW1c_6j6I")));
                    }
                })
                .setNegativeButton(R.string.got_it, null)
                .create()
                .show();
    }

    public static void showSupportMessage(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.action_rate)
                .setMessage(R.string.support_message)
                .setPositiveButton(R.string.rate_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IntentUtils.rateInstaMe(activity);
                    }
                })
                .setNegativeButton(R.string.not_now, null)
                .create()
                .show();

    }

}
