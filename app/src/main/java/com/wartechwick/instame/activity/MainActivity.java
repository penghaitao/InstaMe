package com.wartechwick.instame.activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.kobakei.ratethisapp.RateThisApp;
import com.wartechwick.instame.App;
import com.wartechwick.instame.BuildConfig;
import com.wartechwick.instame.PhotoAdapter;
import com.wartechwick.instame.R;
import com.wartechwick.instame.db.Photo;
import com.wartechwick.instame.sync.HttpClient;
import com.wartechwick.instame.ui.OnPhotoClickListener;
import com.wartechwick.instame.utils.Constant;
import com.wartechwick.instame.utils.IntentUtils;
import com.wartechwick.instame.utils.PreferencesLoader;
import com.wartechwick.instame.utils.Utils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";
    private ClipboardManager clipboard;
    private List<Photo> photoList;
    private PhotoAdapter gramAdapter;

    @Bind(R.id.toolbar)
    Toolbar mToolBar;
    @Bind(R.id.insta_recyler_view)
    RecyclerView recyclerView;
    @Bind(R.id.adView)
    AdView mAdView;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.empty_view)
    LinearLayout emptyView;
    @Bind(R.id.btn_goto_instagram)
    Button gotoinstagramButton;
    @Bind(R.id.progressbar)
    ProgressBar progressBar;

    String clipContent = null;
    String lastUrl = null;
    Realm realm;
    App app;
    private int progressbarNum = 0;
    ClipboardManager.OnPrimaryClipChangedListener mPrimaryClipChangedListener = null;

//    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolBar);
        getActionBarTextView();
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        introLogic();
        app = (App) getApplication();
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setCustomView(R.layout.abs_layout);
//        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/billabong.ttf");
//        TextView textView = (TextView) findViewById(R.id.title);
//        textView.setTypeface(myTypeface);
//        textView.setGravity(Gravity.CENTER);
        photoList = new ArrayList<>();
//        RecyclerView.ItemDecoration itemDecoration = new
//                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
//        recyclerView.addItemDecoration(itemDecoration);
        fab.setOnClickListener(this);
        gotoinstagramButton.setOnClickListener(this);
        setupAdapter();
        lastUrl = PreferenceManager.getDefaultSharedPreferences(this).getString(getResources().getString(R.string.last_url), "");
        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                checkClipboard();
            }
        };
        clipboard.addPrimaryClipChangedListener(mPrimaryClipChangedListener);
        checkClipboard();
//        verifyStoragePermissions();

//        MobileAds.initialize(getApplicationContext(), "ca-app-pub-7166408441889547~5644419913");//old
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5499259334073863~8369213036");
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("6B159B11D34BA65D5D179B0F836FD60E").build();
        mAdView.loadAd(adRequest);
        //test
//        new LoadUrlTask().execute("https://www.instagram.com/p/BIQuaKpDWRY/");
    }

    private void introLogic() {
        boolean isFirstOpen = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(getResources().getString(R.string.first_open), true);
        if (isFirstOpen) {
            Utils.showHelpMessage(MainActivity.this, getResources().getString(R.string.welcome));
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean(getResources().getString(R.string.first_open), false).apply();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        App.uiInForeground = true;
        // Monitor launch times and interval from installation
        RateThisApp.onStart(this);
        // If the criteria is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.uiInForeground = false;
    }

    private void getActionBarTextView() {
        try {
            Field f = mToolBar.getClass().getDeclaredField("mTitleTextView");
            Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/billabong.ttf");
            f.setAccessible(true);
            TextView titleTextView = (TextView) f.get(mToolBar);
            titleTextView.setTextSize(36);
            titleTextView.setPadding(0, 10, 0, 0);
            titleTextView.setTypeface(myTypeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private void checkClipboard() {
        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData != null && clipData.getItemAt(0) != null && clipData.getItemAt(0).getText() != null && !clipData.getItemAt(0).getText().equals(lastUrl)) {
            clipContent = clipData.getItemAt(0).getText().toString();
            if (clipContent.contains(Constant.INSTAGRAM_BASE_URL)) {
                boolean isExist = false;
                for (Photo photo : photoList) {
                    if (photo.isValid() && clipContent.equals(photo.getUrl())) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    if (emptyView.getVisibility() == View.VISIBLE) {
                        undoSetEmptyView();
                    }
                    lastUrl = clipContent;
                    new LoadUrlTask().execute(clipContent);
                    app.logFirebaseEvent("LOAD", "LOAD");
                }
            }
        }
    }

    private void setupAdapter() {
        realm = app.getDBHandler().getRealmInstance();
        RealmResults<Photo> result2 = realm.where(Photo.class)
                .findAll();
        if (result2 != null && result2.size() > 0) {
            result2 = result2.sort("time", Sort.DESCENDING);
            photoList = new ArrayList<>(result2.subList(0, result2.size()));
            gramAdapter = new PhotoAdapter(MainActivity.this, photoList);
            gramAdapter.setiPhotoClickListener(getGramClickListener());
            recyclerView.setAdapter(gramAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        } else {
            setEmptyView();
        }
    }

    private OnPhotoClickListener getGramClickListener() {
        return new OnPhotoClickListener() {
            @Override
            public void onTouch(View v, ImageView itemView, int position) {
                switch (v.getId()) {
                    case R.id.author_avatar:
                    case R.id.author_name:
                        app.logFirebaseEvent("NAME", "NAME");
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
                    case R.id.insta_image:
                        if (photoList.get(position).getVideoUrl() == null) {
                            app.logFirebaseEvent("VIEW", "VIEW");
                            String fileName = getFileName(position);
                            PreferencesLoader loader = new PreferencesLoader(app);
                            if (loader.getBoolean(R.string.action_high_resolution, true)) {
                                IntentUtils.viewImage(MainActivity.this, photoList.get(position).getThumbnailLargeUrl(), fileName);
                            } else {
                                IntentUtils.viewImage(MainActivity.this, photoList.get(position).getThumbnailUrl(), fileName);
                            }
                        } else {
                            app.logFirebaseEvent("VIDEO", "VIDEO");
                            play(position);
                        }
                        break;
                }
            }
        };
    }

    private String getFileName(int position) {
        String filename;
        Photo photo = photoList.get(position);
        String[] urlBits;
        String videoUrl = photo.getVideoUrl();
        if (videoUrl == null) {
            urlBits = photo.getThumbnailUrl().split("/");
            filename = photo.getAuthorName() + "_" + urlBits[urlBits.length - 1];
        } else {
            urlBits = photo.getVideoUrl().split("/");
            filename = photo.getAuthorName() + "_" + urlBits[urlBits.length - 1];
        }
        return filename;
    }

    private void play(int position) {
        Photo photo = photoList.get(position);
        String[] urlBits = photo.getUrl().split("/");
        String filename = photo.getAuthorName() + "_" + urlBits[urlBits.length - 1];
        IntentUtils.playVideo(MainActivity.this, photo.getVideoUrl(), filename);
    }

    private void save(int position, ImageView itemView) {
        if (Utils.verifyStoragePermissions(this)) {
            Photo photo = photoList.get(position);
            String videoUrl = photo.getVideoUrl();
            String filename = getFileName(position);
            app.logFirebaseEvent(filename, "SAVE");
            File file = new File(Utils.getImageDirectory(this) + filename);
            if (videoUrl == null) {
                if (!file.exists()) {
                    Uri uri = Utils.saveImage(itemView, filename, this);
                    if (uri != null) {
                        Toasty.success(app, getResources().getString(R.string.image_saved), Toast.LENGTH_SHORT, true).show();
//                        TastyToast.makeText(app, getResources().getString(R.string.image_saved), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                    }
                } else {
                    Toasty.success(app, getResources().getString(R.string.image_saved_already), Toast.LENGTH_SHORT, true).show();
//                    TastyToast.makeText(app, getResources().getString(R.string.image_saved_already), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                }
            } else {
                if (!file.exists() || file.length() == 0) {
                    new SaveVideoTask(false).execute(videoUrl, filename);
                } else {
                    Toasty.success(app, getResources().getString(R.string.video_saved_already), Toast.LENGTH_SHORT, true).show();
//                    TastyToast.makeText(app, getResources().getString(R.string.video_saved_already), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                }
            }
        }
    }

    private void share(int position, ImageView itemView) {
        if (Utils.verifyStoragePermissions(this)) {
            Photo photo = photoList.get(position);
            String videoUrl = photo.getVideoUrl();
            String filename = getFileName(position);
            app.logFirebaseEvent(filename, "SHARE");
            if (videoUrl == null) {
                IntentUtils.shareImage(itemView, filename, this);
            } else {
                File file = new File(Utils.getImageDirectory(this) + filename);
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
        if (Utils.verifyStoragePermissions(this)) {
            String filename = getFileName(position);
            app.logFirebaseEvent(filename, "WALLPAPER");
            IntentUtils.setWallPaper(itemView, filename, MainActivity.this);
        }
    }

    private void delete(int position) {
        realm = app.getDBHandler().getRealmInstance();
        RealmResults<Photo> result = realm.where(Photo.class)
                .findAll();
        result = result.sort("time", Sort.DESCENDING);
        //this is realm's bug, maybe in the future will be fixed;
        if (position>= result.size()) {
            position = result.size()-1;
        }
        app.logFirebaseEvent("DELETE", "DELETE");
        final Photo photo = result.get(position);
        if (photo.getUrl().equals(lastUrl)) {
            ClipData clipData = ClipData.newPlainText("", "");
            clipboard.setPrimaryClip(clipData);
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                photo.deleteFromRealm();
            }
        });
        photoList.remove(position);
        gramAdapter.notifyItemRemoved(position);
        gramAdapter.notifyItemRangeChanged(position, gramAdapter.getItemCount());
        if (photoList.size() == 0) {
            setEmptyView();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
            case R.id.btn_goto_instagram:
                gotoInstagram();
                break;
        }
    }

    class SaveVideoTask extends AsyncTask<String, Integer, Boolean> {

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
        protected void onPostExecute(Boolean s) {
            progressBar.dismiss();
            if (s) {
                Toasty.success(app, getResources().getString(R.string.video_saved), Toast.LENGTH_SHORT, true).show();
//                TastyToast.makeText(app, getResources().getString(R.string.video_saved), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
            } else {
                Toasty.error(app, getResources().getString(R.string.save_failed), Toast.LENGTH_SHORT, true).show();
//                TastyToast.makeText(app, getResources().getString(R.string.save_failed), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Uri uri = IntentUtils.saveVideoOrShare(MainActivity.this, params[0], params[1], needShare);
            boolean isSuccess = true;
            if (uri == null) {
                isSuccess = false;
            }
            return isSuccess;
        }
    }

    class LoadUrlTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            if (progressBar.getVisibility() != View.VISIBLE) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.getIndeterminateDrawable().setColorFilter(
                        getResources().getColor(R.color.chrome_blue),
                        android.graphics.PorterDuff.Mode.SRC_IN);
            }
            progressbarNum++;
        }

        @Override
        protected String doInBackground(String... params) {
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString(getResources().getString(R.string.last_url), params[0]).apply();
            String reminder = null;
            final ArrayList<Photo> gram = HttpClient.getPhotos(params[0]);
            if (gram.size() > 0) {
                photoList.addAll(0, gram);
                final Realm mRealm = app.getDBHandler().getRealmInstance();
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mRealm.copyToRealmOrUpdate(gram);
                    }
                });
                mRealm.close();
            } else {
                reminder = getString(R.string.app_name);
            }
            return reminder;
        }

        @Override
        protected void onPostExecute(String result) {
            progressbarNum--;
            if (progressbarNum == 0) {
                progressBar.setVisibility(View.INVISIBLE);
            }
            if (result != null) {
                Toasty.error(app, getResources().getString(R.string.save_url_failed), Toast.LENGTH_SHORT, true).show();
//                TastyToast.makeText(app, getResources().getString(R.string.save_url_failed), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
            }
            else {
                if (!App.uiInForeground) {
                    Toasty.success(app,getResources().getString(R.string.image_url_saved), Toast.LENGTH_SHORT, true).show();
                }
                if (gramAdapter != null) {
                    gramAdapter.notifyDataSetChanged();
                } else {
                    setupAdapter();
                }
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
//        clipboard.removePrimaryClipChangedListener(mPrimaryClipChangedListener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (emptyView.getVisibility() == View.VISIBLE) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_high_resolution);
        initHighResItemState(item);
        return true;
    }

    private void initHighResItemState(MenuItem item) {
        PreferencesLoader loader = new PreferencesLoader(this);
        item.setChecked(loader.getBoolean(R.string.action_high_resolution, true));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_help:
                app.logFirebaseEvent("HELP", "HELP");
                String version = BuildConfig.VERSION_NAME;
                Utils.showHelpMessage(this, getResources().getString(R.string.app_name) + " v" + version);
                break;
            case R.id.action_about:
                IntentUtils.gotoAbout(this);
                break;
            case R.id.action_high_resolution:
                boolean isChecked = !item.isChecked();
                item.setChecked(isChecked);
                PreferencesLoader loader = new PreferencesLoader(this);
                loader.saveBoolean(R.string.action_high_resolution, isChecked);
                Toasty.success(app, getResources().getString(isChecked
                        ? R.string.save_high_resolution_on
                        : R.string.save_high_resolution_off), Toast.LENGTH_SHORT, true).show();
//                TastyToast.makeText(app, getResources().getString(isChecked
//                        ? R.string.save_high_resolution_on
//                        : R.string.save_high_resolution_off), TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                break;
            case R.id.action_delete:
                if (photoList.size()>0) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.action_delete_all)
                            .setMessage(R.string.delete_all_message)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteAll();
                                }
                            })
                            .setNegativeButton(R.string.not_now, null)
                            .create()
                            .show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAll() {
        app.logFirebaseEvent("DELETE_ALL", "DELETE_ALL");
        photoList.clear();
        realm = app.getDBHandler().getRealmInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        setEmptyView();
        gramAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utils.REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toasty.success(app, getResources().getString(R.string.permission_granted), Toast.LENGTH_LONG, true).show();
//                    TastyToast.makeText(app, getResources().getString(R.string.permission_granted), TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                    Utils.init(this);
                } else {
                    Toasty.info(app, getResources().getString(R.string.need_permission), Toast.LENGTH_LONG, true).show();
//                    TastyToast.makeText(app, getResources().getString(R.string.need_permission), TastyToast.LENGTH_LONG, TastyToast.INFO);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setEmptyView() {
        recyclerView.setVisibility(View.INVISIBLE);
        emptyView.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);
        mAdView.setVisibility(View.GONE);
        invalidateOptionsMenu();
    }

    private void undoSetEmptyView() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);//temporary set no visibility
        mAdView.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
    }

    private void gotoInstagram() {
        app.logFirebaseEvent("GOTO", "GOTO");
        IntentUtils.gotoInstagram(this);
    }

}