package com.wartechwick.instame.activity;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wartechwick.instame.App;
import com.wartechwick.instame.R;
import com.wartechwick.instame.utils.Utils;

import es.dmoral.toasty.Toasty;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoActivity extends AppCompatActivity implements View.OnClickListener{

    String fileName = "";
    PhotoView photoView;
    App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        photoView = (PhotoView) findViewById(R.id.iv_photo);
        String url = getIntent().getExtras().getString("photourl");
        fileName = getIntent().getExtras().getString("filename");
        app = (App) getApplicationContext();
        final PhotoViewAttacher attacher = new PhotoViewAttacher(photoView);
        final ImageView downloadButton = (ImageView) findViewById(R.id.btn_download);
        downloadButton.setOnClickListener(this);
        Picasso.with(this)
                .load(url)
                .into(photoView, new Callback() {
                    @Override
                    public void onSuccess() {
                        attacher.update();
                        downloadButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (Utils.verifyStoragePermissions(this)) {
            app.logFirebaseEvent(fileName, "SAVE");
            Uri uri = Utils.saveImage(photoView, fileName, this);
            if (uri != null) {
                Toasty.success(app, getResources().getString(R.string.image_saved), Toast.LENGTH_SHORT, true).show();
//                TastyToast.makeText(getApplicationContext(), getResources().getString(R.string.image_saved), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utils.REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toasty.success(app, getResources().getString(R.string.permission_granted), Toast.LENGTH_LONG, true).show();
//                    TastyToast.makeText(getApplicationContext(), getResources().getString(R.string.permission_granted), TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                    Utils.init(this);
                } else {
                    Toasty.info(app, getResources().getString(R.string.need_permission), Toast.LENGTH_LONG, true).show();
//                    TastyToast.makeText(getApplicationContext(), getResources().getString(R.string.need_permission), TastyToast.LENGTH_LONG, TastyToast.INFO);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
