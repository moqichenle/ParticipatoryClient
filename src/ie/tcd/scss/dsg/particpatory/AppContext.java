package ie.tcd.scss.dsg.particpatory;

import android.app.Application;
import android.location.Location;

public class AppContext extends Application {
	private static final String TAG = "AppContext";
	private Location location;
	private String registerId;// user registered id in GCM.
	private String userId;// user's Id in database, Long type

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getRegisterId() {
		return registerId;
	}

	public void setRegisterId(String registerId) {
		this.registerId = registerId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
