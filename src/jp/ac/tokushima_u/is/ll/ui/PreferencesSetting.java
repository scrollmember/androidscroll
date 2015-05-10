package jp.ac.tokushima_u.is.ll.ui;

import jp.ac.tokushima_u.is.ll.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferencesSetting extends PreferenceActivity implements
		OnPreferenceChangeListener {

	private String locationPreferKey;
	private String timePreferKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		locationPreferKey = getResources().getString(
				R.string.setting_location_prefer_key);
		timePreferKey = getResources().getString(
				R.string.setting_time_prefer_key);
		CheckBoxPreference locationCheckPref = (CheckBoxPreference) findPreference(locationPreferKey);
		CheckBoxPreference timeCheckPref = (CheckBoxPreference) findPreference(timePreferKey);

		locationCheckPref.setOnPreferenceChangeListener(this);
		timeCheckPref.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		//TODO
//		String loc_setting = this.getResources().getString(
//				R.string.setting_location_prefer_key);
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
//
//		Log.e("learninglog", "new value  " + newValue);
//
		boolean loc_setting_flg = settings.getBoolean(locationPreferKey, true);
		Log.e("learninglog", loc_setting_flg + "");
		return true;
	}

}
