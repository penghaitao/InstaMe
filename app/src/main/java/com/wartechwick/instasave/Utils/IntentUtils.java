package com.wartechwick.instasave.Utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ImageView;

import com.wartechwick.instasave.MainActivity;
import com.wartechwick.instasave.PlayActivity;
import com.wartechwick.instasave.R;
import com.wartechwick.instasave.Sync.HttpClient;

import java.io.File;

/**
 * Created by penghaitao on 2015/12/22.
 */
public class IntentUtils {

    public static void shareImage(ImageView itemView, String filename, Context context) {
        Uri uri = null;
        File file = new File(Utils.getImageDirectory(context)+filename);
        if (file.exists()) {
            uri = Uri.fromFile(file);
        } else {
            uri = Utils.saveImage(itemView, filename, context);
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "Share image"));
    }

    public static void setWallPaper(ImageView itemView, String filename, Context context) {
        File file = new File(Utils.getImageDirectory(context)+filename);
        Uri uri = null;
        if (file.exists()) {
            uri = Uri.fromFile(file);
        } else {
            uri = Utils.saveImage(itemView, filename, context);
        }
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(uri, "image/jpeg");
        intent.putExtra("mimeType", "image/jpeg");
        context.startActivity(Intent.createChooser(intent, "Set as:"));
    }

    public static void gotoAuthorUrl(String url, Context context) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }

    public static void playVideo(Context context, String videoUrl, String filename) {
        Intent intent = new Intent(context, PlayActivity.class);
        intent.putExtra("videoUrl", videoUrl);
        intent.putExtra("filename", filename);
        context.startActivity(intent);
    }

    public static void saveVideoOrShare(Activity context, String videoUrl, String filename, Boolean needShare) {
        Uri uri = HttpClient.loadVideo(videoUrl, filename, context);
        IntentUtils.savetoAlbum(uri, context);
        if (needShare) {
            shareVideo(uri, context);
        }
    }

    public static void shareVideo(Uri uri, Activity context) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/mp4");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "Share video"));
    }

    public static void savetoAlbum(Uri contentUri, Context context) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static void showSnackbar(int resId, Activity context, int duration) {
        Snackbar snackbar = Snackbar.make(context.findViewById(android.R.id.content), resId, duration);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundResource(R.color.colorAccent);
        snackbar.show();
    }

    public static void sendFeedback(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "wartechwick@gmail.com", null));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"wartechwick@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "InstaMe feedback");
        intent.putExtra(Intent.EXTRA_TEXT, "");

        activity.startActivity(Intent.createChooser(intent, "Send Email"));
    }

    public static void rateInstaMe(MainActivity activity) {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }
}
