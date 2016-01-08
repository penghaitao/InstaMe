package com.wartechwick.instasave;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.wartechwick.instasave.Sync.HttpClient;
import com.wartechwick.instasave.UI.OnPhotoClickListener;
import com.wartechwick.instasave.Utils.Constant;
import com.wartechwick.instasave.Utils.IntentUtils;
import com.wartechwick.instasave.Utils.Utils;
import com.wartechwick.instasave.db.Photo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    private static final String tag = "MainActivity";
    private ClipboardManager clipboard;
    private List<Photo> photoList;
    private RecyclerView recyclerView;
    private PhotoAdapter gramAdapter;

    private Bitmap bitmap = null;
    String clipContent = null;

    // Storage Permissions
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/billabong.ttf");
        TextView textView = (TextView) findViewById(R.id.title);
        textView.setTypeface(myTypeface);
        textView.setGravity(Gravity.CENTER);
        photoList = new ArrayList<Photo>();
        recyclerView = (RecyclerView) findViewById(R.id.insta_recyler_view);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);

        setupAdapter();

        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                checkClipboard();
            }
        });
        checkClipboard();
        verifyStoragePermissions();

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("3EDBD52D74D95B8CBE8E95973F7864DF").build();
        mAdView.loadAd(adRequest);
    }

    private void checkClipboard() {
        if (clipboard.getPrimaryClip() != null && clipboard.getPrimaryClip().getItemAt(0) != null && clipboard.getPrimaryClip().getItemAt(0).getText() != null) {
            clipContent = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
            if (clipContent.contains(Constant.INSTAGRAM_BASE_URL)) {
                boolean isExist = false;
                for (Photo photo : photoList) {
                    if (clipContent.equals(photo.getUrl())) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    new LoadUrlTask().execute();
                }
            }
        }
    }

    private void setupAdapter() {
        Realm realm = Realm.getInstance(MainActivity.this);
        RealmResults<Photo> result2 = realm.where(Photo.class)
                .findAll();
        if (result2 != null && result2.size() > 0) {
            result2.sort("time", Sort.DESCENDING);
            photoList = new ArrayList<Photo>(result2.subList(0, result2.size()));
            gramAdapter = new PhotoAdapter(MainActivity.this, photoList);
            gramAdapter.setiPhotoClickListener(getGramClickListener());
            recyclerView.setAdapter(gramAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        }
    }

    private OnPhotoClickListener getGramClickListener() {
        OnPhotoClickListener listener = new OnPhotoClickListener() {
            @Override
            public void onTouch(View v, ImageView itemView, int position) {
                switch (v.getId()) {
                    case R.id.author_name:
                        IntentUtils.gotoAuthorUrl(photoList.get(position).getAuthorUrl(), MainActivity.this);
                        break;
                    case R.id.btn_save:
                        save(position, itemView);
                        break;
                    case R.id.btn_share:
                        share(position, itemView);
                        break;
                    case R.id.btn_wallpaper:
                        wallpaper(itemView);
                        break;
                    case R.id.btn_delete:
                        delete(position);
                        break;
                    case R.id.insta_play:
                        Photo gram = photoList.get(position);
                        String[] urlBits = gram.getUrl().split("/");
                        String filename = urlBits[urlBits.length-1]+".mp4";
                        IntentUtils.playVideo(MainActivity.this, gram.getVideoUrl(), filename);
                        break;
                }
            }
        };

        return listener;
    }

    private void save(int position, ImageView itemView) {
        if (verifyStoragePermissions()) {
            Photo photo = photoList.get(position);
            String videoUrl = photo.getVideoUrl();
            String[] urlBits = photo.getUrl().split("/");
            if (videoUrl == null) {
                String filename = photo.getAuthorName() + "_"+ urlBits[urlBits.length-1] + ".jpg";
                File file = new File(Utils.getImageDirectory(this)+filename);
                if (!file.exists()) {
                    bitmap = ((BitmapDrawable) itemView.getDrawable()).getBitmap();
                    Utils.saveImage(bitmap, filename, MainActivity.this);
                } else {
                    IntentUtils.showSnackbar(R.string.image_saved_already, this);
                }
            } else {
                String filename = photo.getAuthorName() + "_"+ urlBits[urlBits.length-1]+".mp4";
                File file = new File(Utils.getImageDirectory(this)+filename);
                if (!file.exists()) {
                    new SaveVideoTask(false).execute(videoUrl, filename);
                } else {
                    IntentUtils.showSnackbar(R.string.video_saved_already, this);
                }
            }
        }
    }

    private void share(int position, ImageView itemView) {
        if (verifyStoragePermissions()) {
            Photo gram = photoList.get(position);
            String videoUrl = gram.getVideoUrl();
            String[] urlBits = gram.getUrl().split("/");
            if (videoUrl == null) {
                bitmap = ((BitmapDrawable) itemView.getDrawable()).getBitmap();
                Uri uri = getImageUri();
                IntentUtils.shareImage(uri, MainActivity.this);
            } else {
                String filename = gram.getAuthorName() + "_"+ urlBits[urlBits.length-1]+".mp4";
                File file = new File(Utils.getImageDirectory(this)+filename);
                if (!file.exists()) {
                    new SaveVideoTask(true).execute(videoUrl, filename);
                } else {
                    Uri uri = Uri.fromFile(file);
                    IntentUtils.shareVideo(uri, this);
                }
            }
        }
    }

    private void wallpaper(ImageView itemView) {
        if (verifyStoragePermissions()) {
            bitmap = ((BitmapDrawable) itemView.getDrawable()).getBitmap();
            Uri uri = getImageUri();
            IntentUtils.setWallPaper(uri, MainActivity.this);
        }
    }

    private void delete(int position) {
        Realm realm = Realm.getInstance(MainActivity.this);
        realm.beginTransaction();
        RealmResults<Photo> result = realm.where(Photo.class)
                .findAll();
        result.sort("time", Sort.DESCENDING);
        Photo gram = result.get(position);
        gram.removeFromRealm();
        realm.commitTransaction();
        photoList.remove(position);
        gramAdapter.notifyItemRemoved(position);
        gramAdapter.notifyItemRangeChanged(position, gramAdapter.getItemCount());
    }

    private Uri getImageUri() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(), bitmap, "Title", null);
        Uri uri = Uri.parse(path);
        return uri;
    }

    class SaveVideoTask extends AsyncTask<String, Integer, String> {

        private ProgressDialog progressBar;
        private Boolean needShare = false;

        public SaveVideoTask(boolean b) {
            needShare = b;
        }

        @Override
        protected void onPreExecute() {
            progressBar = new ProgressDialog(MainActivity.this);
            progressBar.setMessage("Downloading...Please wait");
            progressBar.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
            progressBar.show();

        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.dismiss();
        }

        @Override
        protected String doInBackground(String... params) {
            IntentUtils.saveVideoOrShare(MainActivity.this, params[0], params[1], needShare);
            return null;
        }
    }

    class LoadUrlTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            Photo gram = HttpClient.getPhoto(clipContent);
            if (gram != null) {
                photoList.add(0, gram);
                Realm realm = Realm.getInstance(MainActivity.this);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(gram);
                realm.commitTransaction();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (gramAdapter != null) {
                gramAdapter.notifyDataSetChanged();

            } else {
                setupAdapter();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_help:
                showHelpMessage();
                break;
            case R.id.action_feedback:
                IntentUtils.sendFeedback(this);
                break;
            case R.id.action_rate:
                IntentUtils.rateInstaMe(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Utils.init(this);
                } else {
                    Toast.makeText(MainActivity.this, "SAVING_IMAGE Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public boolean verifyStoragePermissions() {

        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showMessageOKCancel("You need to allow access to your storage, So we can save the picture",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        });
                return false;
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return false;
        } else {
            Utils.init(this);
            return true;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showHelpMessage() {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(R.string.insta_help)
                .setPositiveButton("WATCH DEMO VIDEO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/vakdQiqBZ50")));
                    }
                })
                .create()
                .show();
    }

}