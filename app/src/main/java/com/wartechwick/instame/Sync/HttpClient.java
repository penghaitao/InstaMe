package com.wartechwick.instame.sync;

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
import com.wartechwick.instame.db.Photo;
import com.wartechwick.instame.utils.Constant;
import com.wartechwick.instame.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.RealmObject;

/**
 * Created by penghaitao on 2015/12/17.
 */
public class HttpClient {

    private final static int TIMEOUT_CONNECTION = 5000;//5sec
    private final static int TIMEOUT_SOCKET = 30000;//30sec
    private final static String TAG = "HttpClient";

    private static synchronized boolean isNetworkConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            NetworkInfo ni = connManager.getActiveNetworkInfo();
            if (ni != null) {
                return ni.isConnected();
            }
        }
        return false;
    }

    private static String callAPI(String url) {
        String result = null;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
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
                photo.setVideoUrl(video);
                photo.setThumbnailLargeUrl(clipContent + "media/?size=l");
                photo.setUrl(clipContent);
                photo.setTime(System.currentTimeMillis());
            } catch (NullPointerException | IllegalStateException | JsonSyntaxException | IllegalArgumentException exception) {
                exception.printStackTrace();
            }
        }
        return photo;
    }

    public static Photo getPhoto2(String clipContent) {
        Photo photo = null;
//        Log.i("pp", clipContent);
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
            photo.setVideoUrl(video);
            photo.setThumbnailLargeUrl(clipContent + "media/?size=l");
            photo.setUrl(clipContent);
            photo.setTime(System.currentTimeMillis());
        } catch (NullPointerException | IllegalStateException | JsonSyntaxException | IllegalArgumentException exception) {
            exception.printStackTrace();
        }
        return photo;
    }

    public static ArrayList<Photo> getPhotos(String clipContent) {
        ArrayList<Photo> photos = new ArrayList<>();
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
            Photo photo = gson.fromJson(json, Photo.class);
            photo.setThumbnailLargeUrl(clipContent + "media/?size=l");
            photo.setUrl(clipContent);
            photo.setTime(System.currentTimeMillis());
            String html = HttpClient.callAPI(clipContent);
            Document doc1 = Jsoup.parse(html);
            Element videoMeta = doc1.select("meta[property=og:video]").first();
            String video = null;
            if (videoMeta != null) {
                video = videoMeta.attr("content");
            }
            photo.setVideoUrl(video);
            if (photo.getThumbnailUrl() == null || photo.getThumbnailUrl().contains("null") || html.contains("display_url")) {
                Elements script = doc1.getElementsByTag("script");
                for (Element element : script) {
                    if (element.toString().contains("display_url")) {
                        String re1=".*?";  // Non-greedy match on filler
                        String re2="((?:http|https)(?::\\/{2}[\\w]+)(?:[\\/|\\.]?)(?:[^\\s\"]*))"; // HTTP URL 1

                        Pattern p = Pattern.compile(re1+re2,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                        Matcher m = p.matcher(element.toString());
                        long time = System.currentTimeMillis();
                        String ownerAvatar = "";
                        while (m.find())
                        {
                            String httpurl1=m.group(0);
                            if (httpurl1.contains("owner") && httpurl1.contains("profile_pic_url")) {
                                String[] picUrls = httpurl1.split("\"");
                                ownerAvatar = picUrls[picUrls.length -1];
                            }
                            else if (httpurl1.contains("display_url")) {
                                String[] displayurls = httpurl1.split("\"");
                                String displayUrl = displayurls[displayurls.length - 1];
                                Photo photo1 = gson.fromJson(json, Photo.class);
                                photo1.setThumbnailUrl(displayUrl);
                                photo1.setThumbnailLargeUrl(displayUrl);
                                photo1.setUrl(clipContent);
                                photo1.setTime(time--);
                                photo1.setAvatar(ownerAvatar);
                                if (photos.size() == 1) {
                                    if (!displayUrl.equals(photos.get(0).getThumbnailUrl())) {
                                        photos.add(photo1);
                                    } else {
                                        photos.get(0).setAvatar(ownerAvatar);
                                    }
                                } else {
                                    photos.add(photo1);
                                }
                            } else if (httpurl1.contains("video_url")) {
                                String[] videoUrls = httpurl1.split("\"");
                                String videoUrl = videoUrls[videoUrls.length-1];
                                if (photos.size() > 0) {
                                    photos.get(photos.size()-1).setVideoUrl(videoUrl);
                                }
                            }
                        }
                    }
                }
            }
            if (photos.size() == 1 && (photos.get(0).getAvatar() == null || photos.get(0).getAvatar().equals(""))){
                String ownerAvatar = "";
                Elements script = doc1.getElementsByTag("script");
                for (Element element : script) {
                    if (element.toString().contains("profile_pic_url")) {
                        String re1 = ".*?";  // Non-greedy match on filler
                        String re2 = "((?:http|https)(?::\\/{2}[\\w]+)(?:[\\/|\\.]?)(?:[^\\s\"]*))"; // HTTP URL 1

                        Pattern p = Pattern.compile(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                        Matcher m = p.matcher(element.toString());
                        while (m.find()) {
                            String httpurl1 = m.group(0);
                            if (httpurl1.contains("owner") && httpurl1.contains("profile_pic_url")) {
                                String[] picUrls = httpurl1.split("\"");
                                ownerAvatar = picUrls[picUrls.length - 1];
                            }
                        }
                    }
                }
                photos.get(0).setAvatar(ownerAvatar);
            }
        } catch (NullPointerException | IllegalStateException | JsonSyntaxException | IllegalArgumentException exception) {
            exception.printStackTrace();
        }
//        Log.i("pp", photos.get(0).getAvatar()+"--------------------------------------------------------------------");
        return photos;
    }

    public static Uri loadVideo(String videoUrl, String filename) {
        String path = Utils.getImageDirectory();
        File file = new File(path, filename);
        try {
            URL url = new URL(videoUrl);
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
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
