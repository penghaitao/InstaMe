package com.wartechwick.instame.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.ImageView;

import com.wartechwick.instame.App;
import com.wartechwick.instame.activity.AboutActivity;
import com.wartechwick.instame.activity.PhotoActivity;
import com.wartechwick.instame.activity.PlayActivity;
import com.wartechwick.instame.sync.HttpClient;

import java.io.File;

/**
 * Created by penghaitao on 2015/12/22.
 */
public class IntentUtils {

    public static void shareImage(ImageView itemView, String filename, Context context) {
        Uri uri;
        File file = new File(Utils.getImageDirectory()+filename);
        if (file.exists()) {
            uri = Uri.fromFile(file);
        } else {
            uri = Utils.saveImage(itemView, filename);
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "Share image"));
    }

    public static void setWallPaper(ImageView itemView, String filename, Context context) {
        File file = new File(Utils.getImageDirectory()+filename);
        Uri uri;
        if (file.exists()) {
            uri = Uri.fromFile(file);
        } else {
            uri = Utils.saveImage(itemView, filename);
        }
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(uri, "image/jpeg");
        intent.putExtra("mimeType", "image/jpeg");
        context.startActivity(Intent.createChooser(intent, "Set as:"));
    }

    public static void gotoAuthorUrl(String url, Context context) {
        String[] urlBits = url.split("/");
        String name = urlBits[urlBits.length-1];
        Uri uri = Uri.parse(Constant.INSTAGRAM_PROFILE_BASE_RUL+name);
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
        likeIng.setPackage("com.instagram.android");
        try {
            context.startActivity(likeIng);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url)));
        }
    }

    public static void playVideo(Activity context, String videoUrl, String filename) {
        Intent intent = new Intent(context, PlayActivity.class);
        intent.putExtra("videoUrl", videoUrl);
        intent.putExtra("filename", filename);
        context.startActivity(intent);
    }

    public static void viewImage(Context context, String url, String fileName) {
        Intent intent = new Intent(context, PhotoActivity.class);
        intent.putExtra("photourl", url);
        intent.putExtra("filename", fileName);
        context.startActivity(intent);
    }

    public static Uri saveVideoOrShare(Activity context, String videoUrl, String filename, Boolean needShare) {
        Uri uri = HttpClient.loadVideo(videoUrl, filename);
        IntentUtils.savetoAlbum(uri);
        if (needShare) {
            shareVideo(uri, context);
        }
        return uri;
    }

    public static void gotoInstagram(Activity activity) {
        PackageManager manager = activity.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage("com.instagram.android");
        if (i == null) {
            //throw new PackageManager.NameNotFoundException();
            i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setData(Uri.parse("market://details?id=" + "com.instagram.android"));
            activity.startActivity(i);
        } else {
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            activity.startActivity(i);
        }
    }

    public static void shareVideo(Uri uri, Activity context) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/mp4");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "Share video"));
    }

    public static void savetoAlbum(Uri contentUri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(contentUri);
        App.getContext().sendBroadcast(mediaScanIntent);
    }

//    public static void showSnackbar(int resId, Activity context, int duration) {
//        Snackbar snackbar = Snackbar.make(context.findViewById(android.R.id.content), resId, duration);
//        View snackbarView = snackbar.getView();
//        snackbarView.setBackgroundResource(R.color.colorAccent);
//        snackbar.show();
//    }
//
//    public static void showSnackbarWithWarning(int resId, Activity context, int duration) {
//        Snackbar snackbar = Snackbar.make(context.findViewById(android.R.id.content), resId, duration);
//        View snackbarView = snackbar.getView();
//        snackbarView.setBackgroundResource(R.color.warning_amber);
//        snackbar.show();
//    }

    public static void sendFeedback(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "wartechwick@gmail.com", null));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"wartechwick@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "InstantSave feedback");
        intent.putExtra(Intent.EXTRA_TEXT, "");

        activity.startActivity(Intent.createChooser(intent, "Send Email"));
    }

    public static void gotoAbout(Activity activity) {
        activity.startActivity(new Intent(activity, AboutActivity.class));
    }

    public static void rateInstaMe(Activity activity) {
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
