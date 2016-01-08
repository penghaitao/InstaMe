package com.wartechwick.instasave.Sync;

import android.app.Activity;
import android.net.Uri;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wartechwick.instasave.Utils.Constant;
import com.wartechwick.instasave.Utils.Utils;
import com.wartechwick.instasave.db.Photo;

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

    public static Photo getPhoto(String clipContent) {
        String html1 = callAPI(Constant.API_BASE_URL+clipContent);
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
        Photo photo = gson.fromJson(html1, Photo.class);

        String html = HttpClient.callAPI(clipContent);
        Document doc1 = Jsoup.parse(html);
        Element vedioMeta = doc1.select("meta[property=og:video]").first();
        String vedio = null;
        if (vedioMeta != null) {
            vedio = vedioMeta.attr("content");
        }
        photo.setThumbnailLargeUrl(clipContent + "media/?size=l");
        photo.setVideoUrl(vedio);
        photo.setUrl(clipContent);
        photo.setTime(System.currentTimeMillis());
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
