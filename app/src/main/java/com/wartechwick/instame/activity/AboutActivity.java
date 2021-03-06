package com.wartechwick.instame.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.wartechwick.instame.BuildConfig;
import com.wartechwick.instame.R;
import com.wartechwick.instame.utils.IntentUtils;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle(R.string.action_about);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class AboutActivityFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.about_preference_fragment);

            Preference version = findPreference("Version");
            version.setSummary(BuildConfig.VERSION_NAME);

            Preference licence = findPreference("Licence");
            licence.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), LicenceActivity.class));
                    return true;
                }
            });

            Preference feedback = findPreference("feedback");
            feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    IntentUtils.sendFeedback(getActivity());
                    return true;
                }
            });

            Preference rate = findPreference("rate");
            rate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    IntentUtils.rateInstaMe(getActivity());
                    return true;
                }
            });
//            Preference guide = findPreference("Guidelines");
//            guide.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//                    String version = BuildConfig.VERSION_NAME;
//                    Utils.showHelpMessage(getActivity(), getResources().getString(R.string.app_name) + " v" + version);
//                    return true;
//                }
//            });

            Preference share = findPreference("share");
            share.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String app_share_details = "https://play.google.com/store/apps/details?id="+getActivity().getPackageName();
                    Intent myIntent = new Intent(Intent.ACTION_SEND);
                    myIntent.setType("text/plain");
                    myIntent.putExtra(Intent.EXTRA_TEXT, "Check out this awesome Instagram image download tool.\n" + "*InstantSave*\n" + app_share_details);
                    startActivity(Intent.createChooser(myIntent, "Share with"));
                    return true;
                }
            });

            Preference privacyPolicy = findPreference("Privacy Policy");
            privacyPolicy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), PrivacyActivity.class));
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
