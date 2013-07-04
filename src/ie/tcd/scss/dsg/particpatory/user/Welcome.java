package ie.tcd.scss.dsg.particpatory.user;

import ie.tcd.scss.dsg.particpatory.GCMIntentService;
import ie.tcd.scss.dsg.particpatory.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Welcome extends Activity {
	Button regButton;
	Button unregButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		regButton = (Button) findViewById(R.id.regButton);

		regButton.setOnClickListener(this.regListener);

	}

	private OnClickListener regListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			regButton = (Button) findViewById(R.id.regButton);
			regButton.setText("Please wait...");
			regButton.setEnabled(false);
			GCMIntentService.register(getApplicationContext());
		}
	};
}
