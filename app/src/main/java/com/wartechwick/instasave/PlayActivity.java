package com.wartechwick.instasave;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import com.wartechwick.instasave.Utils.Utils;

import java.io.File;


public class PlayActivity extends Activity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_play);
        Uri uri = Uri.parse(getIntent().getExtras().getString("videoUrl"));
        String filename = getIntent().getExtras().getString("filename");
        File file = new File(Utils.getImageDirectory(this)+filename);
        VideoView gramVideoView = (VideoView) findViewById(R.id.video_view);
        gramVideoView.setMediaController(null);
        if (!file.exists()) {
            gramVideoView.setVideoURI(uri);
        } else {
            gramVideoView.setVideoURI(Uri.fromFile(file));
        }
        gramVideoView.seekTo(100);
        gramVideoView.requestFocus();
        gramVideoView.start();
        gramVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

    }


}
