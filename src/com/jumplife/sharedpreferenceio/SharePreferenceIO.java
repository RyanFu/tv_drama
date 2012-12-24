package com.jumplife.sharedpreferenceio;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceIO {
	private final String preferenceName = "Preference";
	private static SharedPreferences settings;
	
	public SharePreferenceIO(Context context) {
		settings = context.getSharedPreferences(preferenceName, 0);
	}

	// setting
	public void SharePreferenceI(String attr, String defValue) {
		settings.edit().putString(attr, defValue).commit();
	}
	
	public void SharePreferenceI(String attr, int defValue) {
		settings.edit().putInt(attr, defValue).commit();
	}
		
	public void SharePreferenceI(String attr, boolean defValue) {
		settings.edit().putBoolean(attr, defValue).commit();
	}
	
	public void SharePreferenceI(String attr, float defValue) {
		settings.edit().putFloat(attr, defValue).commit();
	}

	public void SharePreferenceI(String attr, long defValue) {
		settings.edit().putLong(attr, defValue).commit();
	}
	
	//getting
	public String SharePreferenceO(String attr, String defValue) {
		String token = settings.getString(attr, defValue);
		return token;
	}
	
	public int SharePreferenceO(String attr, int defValue) {
		int token = settings.getInt(attr, defValue);
		return token;
	}
		
	public boolean SharePreferenceO(String attr, boolean defValue) {
		boolean token = settings.getBoolean(attr, defValue);
		return token;
	}
	
	public float SharePreferenceO(String attr, float defValue) {
		float token = settings.getFloat(attr, defValue);
		return token;
	}

	public long SharePreferenceO(String attr, long defValue) {
		long token = settings.getLong(attr, defValue);
		return token;
	}
}
