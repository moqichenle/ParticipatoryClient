package ie.tcd.scss.dsg.particpatory.user;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.R;
import ie.tcd.scss.dsg.particpatory.SampleListFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class UserProfile extends SlidingFragmentActivity {
	private static final String TAG = "UserProfileActivity";
	private AppContext context;
	private ListFragment mFrag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "load the page");
		setContentView(R.layout.activity_user_profile);
		context = (AppContext) getApplicationContext();
		setupSlidingMenu(savedInstanceState);
		Log.d(TAG, "get User");
		String nickName = context.getNickName();
		TextView userName = (TextView) findViewById(R.id.userName);
		userName.setText(nickName);
		TextView mode = (TextView) findViewById(R.id.mode);
		TextView walk = (TextView) findViewById(R.id.walk);
		TextView cycle = (TextView) findViewById(R.id.cycle);
		TextView drive = (TextView) findViewById(R.id.drive);
		TextView rate = (TextView) findViewById(R.id.rate);
		mode.setText(context.getMode());
		walk.setText(context.getAverWalkSpeed() + "");
		cycle.setText(context.getAverCycleSpeed() + "");
		drive.setText(context.getAverDriveSpeed() + "");
		rate.setText(context.getAcceptPercent() + "");
	}

	

	private void setupSlidingMenu(Bundle savedInstanceState){
		
		setBehindContentView(R.layout.menu_frame);
		if (savedInstanceState == null) {
			FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
			mFrag = new SampleListFragment();
			t.replace(R.id.menu_frame, mFrag);
			t.commit();
		} else {
			mFrag = (ListFragment)this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
		}
		
		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
//		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
}
