package ie.tcd.scss.dsg.particpatory;

import com.google.android.gcm.GCMRegistrar;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

/**
 * The Main Activity.
 * 
 * This activity starts up the RegisterActivity immediately, which communicates
 * with your App Engine backend using Cloud Endpoints. It also receives push
 * notifications from backend via Google Cloud Messaging (GCM).
 * 
 * Check out RegisterActivity.java for more details.
 */
public class MainActivity extends Activity {
	AppContext context;
	private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = (AppContext) getApplicationContext();

		if (GCMRegistrar.getRegistrationId(this.getApplicationContext()) != null) {
			Intent intent = new Intent(this, UserRegister.class);
			startActivity(intent);
		} else {
			// Start up RegisterActivity right away
			Intent intent = new Intent(this, RegisterActivity.class);
			startActivity(intent);
			// Since this is just a wrapper to start the main activity,
			// finish it after launching RegisterActivity
		}
		finish();
	}

}
