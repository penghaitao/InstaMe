package com.wartechwick.instame.sync;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wartechwick.instame.utils.Constant;
import com.wartechwick.instame.utils.Utils;
import com.wartechwick.instame.db.Photo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import io.realm.RealmObject;

/**
 * Created by penghaitao on 2015/12/17.
 */
public class HttpClient {

    private final static int TIMEOUT_CONNECTION = 5000;//5sec
    private final static int TIMEOUT_SOCKET = 30000;//30sec
    private final static String TAG = "HttpClient";

    public static synchronized boolean isNetworkConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            NetworkInfo ni = connManager.getActiveNetworkInfo();
            if (ni != null) {
                return ni.isConnected();
            }
        }
        return false;
    }

    public static String callAPI(String url) {
        String result = null;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Photo getPhoto(Context context, String clipContent) {
        Photo photo = null;
        if (isNetworkConnected(context)) {
            String json = callAPI(Constant.API_BASE_URL+clipContent);
            Gson gson = new GsonBuilder()
                    .setExclusionStrategies(new ExclusionStrategy() {
                        @Override
                        public boolean shouldSkipField(FieldAttributes f) {
                            return f.getDeclaringClass().equals(RealmObject.class);
                        }

                        @Override
                        public boolean shouldSkipClass(Class<?> clazz) {
                            return false;
                        }
                    })
                    .create();
            try {
                photo = gson.fromJson(json, Photo.class);
                String html = HttpClient.callAPI(clipContent);
                Document doc1 = Jsoup.parse(html);
                Element videoMeta = doc1.select("meta[property=og:video]").first();
                String video = null;
                if (videoMeta != null) {
                    video = videoMeta.attr("content");
                }
                photo.setThumbnailLargeUrl(clipContent + "media/?size=l");
                photo.setVideoUrl(video);
                photo.setUrl(clipContent);
                photo.setTime(System.currentTimeMillis());
            } catch (NullPointerException | IllegalStateException | JsonSyntaxException | IllegalArgumentException exception) {
                exception.printStackTrace();
                //// TODO: 2016/7/7 add download failed hint 
            }
        }
        return photo;
    }

    public static Uri loadVideo(String videoUrl, String filename, Activity context) {
        URL url = null;
        String path = Utils.getImageDirectory(context);
        File file = new File(path, filename);
        try {
            url = new URL(videoUrl);
            URLConnection ucon = url.openConnection();

            ucon.setReadTimeout(TIMEOUT_CONNECTION);
            ucon.setConnectTimeout(TIMEOUT_SOCKET);

            InputStream is = ucon.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
            FileOutputStream outStream = new FileOutputStream(file);
            byte[] buff = new byte[5 * 1024];

            //Read bytes (and store them) until there is nothing more to read(-1)
            int len;
            while ((len = inStream.read(buff)) != -1) {
                outStream.write(buff, 0, len);
            }

            //clean up
            outStream.flush();
            outStream.close();
            inStream.close();
            Uri contentUri = Uri.fromFile(file);
            return contentUri;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
