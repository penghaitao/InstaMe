package com.wartechwick.instasave.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.wartechwick.instasave.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by penghaitao on 2015/12/21.
 */
public class Utils {

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
            return Environment.getExternalStorageDirectory() + "/InstaMe/";
        } else {
            return context.getCacheDir() + "/InstaMe/";
        }
    }

    public static void saveImage(Bitmap bitmap, String filename, Activity context) {
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
        Uri contentUri = Uri.fromFile(file);
        IntentUtils.savetoAlbum(contentUri, R.string.image_saved, context);
    }

}
