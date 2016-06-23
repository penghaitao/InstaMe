package com.wartechwick.instasave.db;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by penghaitao on 2015/12/17.
 */
public class Photo extends RealmObject{


    @SerializedName("author_name")
    private String authorName;
    @SerializedName("media_id")
    private String mediaId;
    @Required
    @PrimaryKey
    @SerializedName("thumbnail_url")
    private String thumbnailUrl;
    @SerializedName("title")
    private String title;
    @SerializedName("html")
    private String html;
    @SerializedName("author_url")
    private String authorUrl;
    @SerializedName("author_id")
    private String authorId;

    @SerializedName("thumbnail_width")
    private int thumbnailWidth;
    @SerializedName("thumbnail_height")
    private int thumbnailHeight;

    private String thumbnailLargeUrl;
    private String url;
    private String videoUrl;
    private long time;

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    public void setThumbnailHeight(int thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

    public String getThumbnailLargeUrl() {
        return thumbnailLargeUrl;
    }

    public void setThumbnailLargeUrl(String thumbnailLargeUrl) {
        this.thumbnailLargeUrl = thumbnailLargeUrl;
    }
}
