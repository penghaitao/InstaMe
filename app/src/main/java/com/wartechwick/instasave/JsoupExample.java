package com.wartechwick.instasave;

/**
 * Created by penghaitao on 2015/12/30.
 */
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wartechwick.instasave.Sync.HttpClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by terri on 12/30/15.
 */
public class JsoupExample {
    /**
     * *******************
     * ****** Setting ******
     * ********************
     */
    //从第x页抓起
    public static final String URLL = "https://www.douban.com/photos/album/152964468/?start=0";
    // 模仿UA
//  public static final String UA = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.47 Safari/536.11";
    public static final String UA = "Mozilla/5.0 (Windows; U; Windows NT 5.2) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13";
    // 图片节点选择器
    public static final String IMG_DIV_SELECTOR = ".image-show img";
    //帖子节点选择器
    public static final String POST_SELECTOR = ".photo_wrap a[class]";
    // 最高页数
    public static final int MAX_PAGE = 522;
    // 存储路径
    public static final String BASE_PATH = "/Users/terri/Downloads/haha";


    /**
     * @Description 主函数
     */
    public static void main(String[] args) {

        for (int i = 0; i <= MAX_PAGE; i = i + 18) {
            String page_url = URLL + i;
            // 图片按页面分文件夹
            String pagePath = BASE_PATH + "/" + i;

//            System.out.println("\n" + "**************解析URL(第" + i + "页):" + page_url + "**************\n");
            String pageResult = getResultByUrl(page_url);
            Iterator iterator = getPostUrl(pageResult).iterator();
            while (iterator.hasNext()) {
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String postUrl = (String) iterator.next();
//                System.out.println("解析图片帖子URL:" + postUrl);
                String postResult = getResultByUrl(postUrl);
                List<String> urls = getImgUrl(postResult);
                for (String str : urls) {
                    try {
                        Thread.currentThread().sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    System.out.println("解析图片url：" + str);
                    File imgFile = getStoreFile(str, pagePath);
                    if (saveImg(str, imgFile))
                        System.out.println("存入图片" + imgFile.getName());
                }
            }
//            System.out.println("\n" + "**************解析URL完成(第" + i + "页)**************\n");
        }
//        System.out.println("\n" + "**************全部URL解析完成**************\n");


    }

    /**
     * 获取帖子目录名和对应的url
     *
     * @param pageResult
     * @return 返回map，key：图片目录path，value：帖子url
     */
    public static List<String> getPostUrl(String pageResult) {
        Document doc = Jsoup.parse(pageResult);
        List<String> rtn = new ArrayList<String>();
        Elements es = doc.select(POST_SELECTOR);
        for (Iterator<Element> i = es.iterator(); i.hasNext(); ) {
            Element e = i.next();
            rtn.add(e.attr("href"));
//            System.out.println("图片帖子链接：" + e.attr("href"));
        }
        String html = HttpClient.callAPI("https://www.instagram.com/p/_ZiJp9wlxs/");
        Document doc1 = Jsoup.parse(html);
        Element vedioMeta = doc1.select("meta[property=og:video]").first();
        String vedio = null;
        if (vedioMeta != null) {
            vedio = vedioMeta.attr("content");
        }
        Element imageMeta = doc1.select("meta[property=og:image]").first();
        String image = imageMeta.attr("content");
        Element urlMeta = doc1.select("meta[property=og:url]").first();
        String url = urlMeta.attr("content");
        Element titleMeta = doc1.select("meta[property=og:title]").first();
        String title = titleMeta.attr("content");
        String authorName = title.split("\\s+")[0];
        Element descMeta = doc1.select("meta[property=og:description]").first();
        String desc = descMeta.attr("content");
        System.out.println("vedio=" + vedio);
        System.out.println("image=" + image);
        System.out.println("url=" + url);
        System.out.println("authorname=" + authorName);
        System.out.println("desc=" + desc);
        return rtn;
    }

    /**
     * 给定url获取整个页面内容
     *
     * @param url
     * @return
     */
    public static String getResultByUrl(String url) {
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


    /**
     * 从帖子内容中获取图片url
     */
    public static List<String> getImgUrl(String str) {
        List<String> img_urls = new ArrayList<String>();
        Document doc = Jsoup.parse(str);

        Elements es = doc.select(IMG_DIV_SELECTOR);
        for (Iterator<Element> i = es.iterator(); i.hasNext(); ) {
            Element e = i.next();
            img_urls.add(e.attr("src"));
        }
        return img_urls;
    }

    /**
     * 从图片url和帖子名，生成图片的存储路径
     */
    public static File getStoreFile(String imgUrl, String postPath) {

        String[] tmp = imgUrl.split("/");

        String imgName = tmp[tmp.length - 1];

        File dir = new File(postPath);
        if (!dir.exists())
            dir.mkdirs();
        File imgFile = new File(postPath + "/" + imgName);
        if (!imgFile.exists()) {
            try {
                imgFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imgFile;
    }

    /**
     * 将图片写入本地
     */
    public static boolean saveImg(String img_url, File file) {
//        HttpClient hc = new DefaultHttpClient();
//        try {
//            HttpGet httpget = new HttpGet(img_url);
//            httpget.setHeader("User-Agent", UA);
//            httpget.setHeader("Accept-Encoding", "utf-8");
//
//            HttpResponse response = hc.execute(httpget);
//            HttpEntity entity = response.getEntity();
//            if (entity != null) {
//                InputStream in = entity.getContent();
//                OutputStream os = new FileOutputStream(file);
//                int count = IOUtils.copy(in, os);
//                IOUtils.closeQuietly(in);
//                IOUtils.closeQuietly(os);
//                if (0 != count)
//                    return true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
        URL url = null;
        try {
            url = new URL(img_url);

            InputStream in = new BufferedInputStream(url.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            while (-1!=(n=in.read(buf)))
            {
                out.write(buf, 0, n);
                out.close();
                in.close();
            }
            byte[] response = out.toByteArray();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(response);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }
}
