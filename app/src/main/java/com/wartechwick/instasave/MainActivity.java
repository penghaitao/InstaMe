package com.wartechwick.instasave;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.wartechwick.instasave.Sync.HttpClient;
import com.wartechwick.instasave.UI.OnPhotoClickListener;
import com.wartechwick.instasave.Utils.Constant;
import com.wartechwick.instasave.Utils.IntentUtils;
import com.wartechwick.instasave.Utils.Utils;
import com.wartechwick.instasave.db.Photo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    private static final String tag = "MainActivity";
    private ClipboardManager clipboard;
    private List<Photo> photoList;
    private PhotoAdapter gramAdapter;

    @Bind(R.id.insta_recyler_view) RecyclerView recyclerView;
    @Bind(R.id.adView) AdView mAdView;

    String clipContent = null;
    String lastUrl = null;
    boolean isFirstOpen = false;

    // Storage Permissions
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/billabong.ttf");
        TextView textView = (TextView) findViewById(R.id.title);
        textView.setTypeface(myTypeface);
        textView.setGravity(Gravity.CENTER);
        photoList = new ArrayList<Photo>();
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);

        setupAdapter();
        lastUrl = PreferenceManager.getDefaultSharedPreferences(this).getString(getResources().getString(R.string.last_url), "");
        isFirstOpen = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.first_open), true);
        if (isFirstOpen) {
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean(getResources().getString(R.string.first_open), false).commit();
            showHelpMessage(R.string.welcome);
        }
        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                checkClipboard();
            }
        });
        checkClipboard();
//        verifyStoragePermissions();

        AdRequest adRequest = new AdRequest.Builder().addTestDevice("3EDBD52D74D95B8CBE8E95973F7864DF").build();
        mAdView.loadAd(adRequest);
    }

    private void checkClipboard() {
        if (clipboard.getPrimaryClip() != null && clipboard.getPrimaryClip().getItemAt(0) != null && clipboard.getPrimaryClip().getItemAt(0).getText() != null) {
            clipContent = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
            if (clipContent.contains(Constant.INSTAGRAM_BASE_URL)) {
                boolean isExist = false;
                for (Photo photo : photoList) {
                    if (photo.isValid() && clipContent.equals(photo.getUrl())) {
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
                        wallpaper(position, itemView);
                        break;
                    case R.id.btn_delete:
                        delete(position);
                        break;
                    case R.id.insta_play:
                        play(position);
                        break;
                }
            }
        };

        return listener;
    }

    private String getFileName(int position) {
        String filename = null;
        Photo photo = photoList.get(position);
        String[] urlBits = photo.getUrl().split("/");
        String videoUrl = photo.getVideoUrl();
        if (videoUrl == null) {
            filename = photo.getAuthorName() + "_"+ urlBits[urlBits.length-1] + ".jpg";
        } else {
            filename = photo.getAuthorName() + "_"+ urlBits[urlBits.length-1] + ".mp4";
        }
        return filename;
    }

    private void save(int position, ImageView itemView) {
        if (verifyStoragePermissions()) {
            Photo photo = photoList.get(position);
            String videoUrl = photo.getVideoUrl();
            String filename = getFileName(position);
            File file = new File(Utils.getImageDirectory(this)+filename);
            if (videoUrl == null) {
                if (!file.exists()) {
                    Uri uri = Utils.saveImage(itemView, filename, this);
                    if (uri != null) {
                        IntentUtils.showSnackbar(R.string.image_saved, this, Snackbar.LENGTH_SHORT);
                    }
                } else {
                    IntentUtils.showSnackbar(R.string.image_saved_already, this, Snackbar.LENGTH_SHORT);
                }
            } else {
                if (!file.exists()) {
                    new SaveVideoTask(false).execute(videoUrl, filename);
                } else {
                    IntentUtils.showSnackbar(R.string.video_saved_already, this, Snackbar.LENGTH_SHORT);
                }
            }
        }
    }

    private void share(int position, ImageView itemView) {
        if (verifyStoragePermissions()) {
            Photo photo = photoList.get(position);
            String videoUrl = photo.getVideoUrl();
            String filename = getFileName(position);
            if (videoUrl == null) {
                IntentUtils.shareImage(itemView, filename, this);
            } else {
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

    private void wallpaper(int position, ImageView itemView) {
        if (verifyStoragePermissions()) {
            String filename = getFileName(position);
            IntentUtils.setWallPaper(itemView, filename, MainActivity.this);
        }
    }

    private void delete(int position) {
        Realm realm = Realm.getInstance(MainActivity.this);
        realm.beginTransaction();
        RealmResults<Photo> result = realm.where(Photo.class)
                .findAll();
        result.sort("time", Sort.DESCENDING);
        Photo photo = result.get(position);
        if (photo.getUrl().equals(lastUrl)) {
            ClipData clipData = ClipData.newPlainText("", "");
            clipboard.setPrimaryClip(clipData);
        }
        photo.removeFromRealm();
        realm.commitTransaction();
        photoList.remove(position);
        gramAdapter.notifyItemRemoved(position);
        gramAdapter.notifyItemRangeChanged(position, gramAdapter.getItemCount());
    }

    private void play(int position) {
        Photo photo = photoList.get(position);
        String[] urlBits = photo.getUrl().split("/");
        String filename = urlBits[urlBits.length-1]+".mp4";
        IntentUtils.playVideo(MainActivity.this, photo.getVideoUrl(), filename);
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
            progressBar.setMessage(getResources().getString(R.string.video_start_saving));
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
            IntentUtils.showSnackbar(R.string.video_saved, MainActivity.this, Snackbar.LENGTH_SHORT);
            return null;
        }
    }

    class LoadUrlTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString(getResources().getString(R.string.last_url), clipContent).commit();
            lastUrl = clipContent;
            Photo gram = HttpClient.getPhoto(MainActivity.this, clipContent);
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
                showHelpMessage(R.string.app_name);
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
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    IntentUtils.showSnackbar(R.string.permission_granted, this, Snackbar.LENGTH_LONG);
                    Utils.init(this);
                } else {
                    IntentUtils.showSnackbar(R.string.need_permission, this, Snackbar.LENGTH_LONG);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public boolean verifyStoragePermissions() {

        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
//            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                showMessageOKCancel("You need to allow access to your storage, So we can save the picture",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                                        REQUEST_CODE_ASK_PERMISSIONS);
//                            }
//                        });
//                return false;
//            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return false;
        } else {
            Utils.init(this);
            return true;
        }

    }

//    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
//        new AlertDialog.Builder(MainActivity.this)
//                .setMessage(message)
//                .setPositiveButton(R.string.ok, okListener)
//                .setNegativeButton(R.string.cancel, null)
//                .create()
//                .show();
//    }

    private void showHelpMessage(int titleId) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(titleId)
                .setMessage(R.string.insta_help)
                .setPositiveButton(R.string.watch_demo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/vakdQiqBZ50")));
                    }
                })
                .setNegativeButton(R.string.go_to_instagram, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PackageManager manager = getPackageManager();
                        Intent i = manager.getLaunchIntentForPackage("com.instagram.android");
                        if (i == null) {
                            //throw new PackageManager.NameNotFoundException();
                            i = new Intent(Intent.ACTION_VIEW);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.setData(Uri.parse("market://details?id=" + "com.instagram.android"));
                            startActivity(i);
                        } else {
                            i.addCategory(Intent.CATEGORY_LAUNCHER);
                            startActivity(i);
                        }
                    }
                })
                .create()
                .show();
    }

}