package ie.tcd.scss.dsg.particpatory.report;

import ie.tcd.scss.dsg.particpatory.AppContext;
import ie.tcd.scss.dsg.particpatory.R;
import ie.tcd.scss.dsg.particpatory.SampleListFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class AddReportActivity extends SlidingFragmentActivity {
	private static final String TAG = "AddReportActivity";
	private AppContext context;
	private ListFragment mFrag;
	private Spinner keywords;
	@SuppressWarnings("unused")
	private byte categoryId = 0;
	private ArrayAdapter<CharSequence> keywordsAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		context = (AppContext) getApplicationContext();
		setContentView(R.layout.activity_add_report);
		setupSlidingMenu(savedInstanceState);
		Log.d(TAG, "loaded");
		Spinner category = (Spinner) findViewById(R.id.category);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter
				.createFromResource(this, R.array.category_array,
						android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		categoryAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		category.setAdapter(categoryAdapter);

		keywords = (Spinner) findViewById(R.id.hintword);
		keywordsAdapter = ArrayAdapter.createFromResource(this,
				R.array.keyword_array, android.R.layout.simple_spinner_item);
		keywordsAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		keywords.setAdapter(keywordsAdapter);

		category.setOnItemSelectedListener(this.selectCategory);
	}

	private OnItemSelectedListener selectCategory = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			String category = (String) parent.getItemAtPosition(pos);
			if (category.equals("Select Category")) {
				keywords.setAdapter(keywordsAdapter);
			} else if (category.equals("Traffic")) {
				ArrayAdapter<CharSequence> tra_key = ArrayAdapter
						.createFromResource(context, R.array.tra_keyword_array,
								android.R.layout.simple_spinner_item);
				keywords.setAdapter(tra_key);
				categoryId = 0;
			} else {
				ArrayAdapter<CharSequence> imp_key = ArrayAdapter
						.createFromResource(context, R.array.imp_keyword_array,
								android.R.layout.simple_spinner_item);
				keywords.setAdapter(imp_key);
				categoryId = 1;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	};

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

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		// getActionBar().setDisplayHomeAsUpEnabled(true);
	}
}
