package ie.tcd.scss.dsg.particpatory.task;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.R;
import ie.tcd.scss.dsg.particpatory.SampleListFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class TaskDetailActivity extends SlidingFragmentActivity {
	private static final String TAG = "AddReportActivity";
	private AppContext context;
	private ListFragment mFrag;
	private String taskId;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = (AppContext) getApplicationContext();
		setContentView(R.layout.activity_show_task);
		setupSlidingMenu(savedInstanceState);
		String taskInfo = context.getTaskInfo();
		//taskInfo =  description+expirePeriod+taskId(sub)
		String[] subMsg = taskInfo.split("_");
		TextView description = (TextView) findViewById(R.id.task_des);
		description.setText(subMsg[0]);
		int expireTime = Integer.valueOf(subMsg[1]);
		taskId = subMsg[2];
	}

	private void setupSlidingMenu(Bundle savedInstanceState) {

		setBehindContentView(R.layout.menu_frame);
		if (savedInstanceState == null) {
			FragmentTransaction t = this.getSupportFragmentManager()
					.beginTransaction();
			mFrag = new SampleListFragment();
			t.replace(R.id.menu_frame, mFrag);
			t.commit();
		} else {
			mFrag = (ListFragment) this.getSupportFragmentManager()
					.findFragmentById(R.id.menu_frame);
		}

		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}
}
