package com.wartechwick.instasave.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;

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
        Uri contentUri = Uri.fromFile(file);
        return contentUri;
    }

}
