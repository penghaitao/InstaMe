package com.wartechwick.instame.activity;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

import com.sdsmdg.tastytoast.TastyToast;
import com.wartechwick.instame.R;
import com.wartechwick.instame.utils.Utils;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;


public class PlayActivity extends AppCompatActivity {


    @Bind(R.id.video_view) VideoView gramVideoView;
    ProgressDialog progDailog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_play);
        ButterKnife.bind(this);
        Uri uri = Uri.parse(getIntent().getExtras().getString("videoUrl"));
        String filename = getIntent().getExtras().getString("filename");
        File file = new File(Utils.getImageDirectory(this)+filename);
        gramVideoView.setMediaController(null);
        if (!file.exists()) {
            gramVideoView.setVideoURI(uri);
        } else {
            gramVideoView.setVideoURI(Uri.fromFile(file));
        }
        gramVideoView.seekTo(100);
        gramVideoView.requestFocus();
        gramVideoView.start();
        TastyToast.makeText(getApplicationContext(), "Loading...", TastyToast.LENGTH_SHORT, TastyToast.DEFAULT);

        gramVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }


        });

    }


}
