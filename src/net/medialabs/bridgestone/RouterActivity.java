package net.medialabs.bridgestone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

public class RouterActivity extends Activity {
	
	private static final String PREFERENCES_FILE = "net.medialabs.bridgestone.PREFERENCE_FILE_KEY";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Context context = this;
		SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
		boolean firstTime = preferences.getBoolean("FIRST_TIME", true);
		
		Intent intent = null;
		
		if(firstTime) {
			intent = new Intent(this, ConfigurationActivity.class);
		} else {
			intent = new Intent(this, EncuestaActivity.class);
		}
		
		startActivity(intent);
		finish();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.router, menu);
		return true;
	}

}
