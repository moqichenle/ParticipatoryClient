package ie.tcd.scss.dsg.particpatory;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class UserProfile extends Activity {
	private static final String TAG = "UserProfileActivity";
	private AppContext context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);
		context = (AppContext) getApplicationContext();
		Log.d(TAG, "load the page");
		Log.d(TAG, "get User");
		String nickName = context.getNickName();
		EditText userName = (EditText) findViewById(R.id.userName);
		userName.setEnabled(false);
		userName.setText(nickName);
		TextView mode = (TextView) findViewById(R.id.mode);
		TextView walk = (TextView) findViewById(R.id.walk);
		TextView cycle = (TextView) findViewById(R.id.cycle);
		TextView drive = (TextView) findViewById(R.id.drive);
		TextView rate = (TextView) findViewById(R.id.rate);
		mode.setText(context.getMode());
		walk.setText(context.getAverWalkSpeed()+"");
		cycle.setText(context.getAverCycleSpeed()+"");
		drive.setText(context.getAverDriveSpeed()+"");
		rate.setText(context.getAcceptPercent()+"");
	}

}
