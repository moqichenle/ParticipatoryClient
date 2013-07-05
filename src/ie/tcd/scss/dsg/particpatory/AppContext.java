package ie.tcd.scss.dsg.particpatory;

import android.app.Application;
import android.content.SharedPreferences;

public class AppContext extends Application {
	public static final String PREFS_NAME = "MyPrefsFile";
	// private static final String TAG = "AppContext";
	private String userId;// added user into the database
	private String registeredId;// registered to GCM
	private String nickName;
	private String mode;
	private float acceptPercent;
	private float averWalkSpeed;
	private float averDriveSpeed;
	private float averCycleSpeed;
	private int taskingAmount;
	private int acceptedTaskAmount;
	private String reportId;
	private String taskId;

	@Override
	public void onCreate() {
		super.onCreate();
		SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME,
				MODE_PRIVATE);
		String name = sharedPreferences.getString("nickName", null);
		setNickName(name);
		String uId = sharedPreferences.getString("userId", null);
		setUserId(uId);
		String regId = sharedPreferences.getString("registeredId", null);
		setRegisteredId(regId);
		String m = sharedPreferences.getString("mode", null);
		setMode(m);
		float walk = sharedPreferences.getFloat("walk", 0);
		setAverWalkSpeed(walk);
		float cycle = sharedPreferences.getFloat("cycle", 0);
		setAverCycleSpeed(cycle);
		float drive = sharedPreferences.getFloat("drive", 0);
		setAverDriveSpeed(drive);
		float accept = sharedPreferences.getFloat("accept", 0);
		setAcceptPercent(accept);
		int overall = sharedPreferences.getInt("overall", 0);
		setTaskingAmount(overall);
		int acceptAmount = sharedPreferences.getInt("acceptAmount", 0);
		setAcceptedTaskAmount(acceptAmount);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRegisteredId() {
		return registeredId;
	}

	public void setRegisteredId(String registeredId) {
		this.registeredId = registeredId;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public float getAcceptPercent() {
		return acceptPercent;
	}

	public void setAcceptPercent(float acceptPercent) {
		this.acceptPercent = acceptPercent;
	}

	public float getAverWalkSpeed() {
		return averWalkSpeed;
	}

	public void setAverWalkSpeed(float averWalkSpeed) {
		this.averWalkSpeed = averWalkSpeed;
	}

	public float getAverDriveSpeed() {
		return averDriveSpeed;
	}

	public void setAverDriveSpeed(float averDriveSpeed) {
		this.averDriveSpeed = averDriveSpeed;
	}

	public float getAverCycleSpeed() {
		return averCycleSpeed;
	}

	public void setAverCycleSpeed(float averCycleSpeed) {
		this.averCycleSpeed = averCycleSpeed;
	}

	public int getTaskingAmount() {
		return taskingAmount;
	}

	public void setTaskingAmount(int taskingAmount) {
		this.taskingAmount = taskingAmount;
	}

	public int getAcceptedTaskAmount() {
		return acceptedTaskAmount;
	}

	public void setAcceptedTaskAmount(int acceptedTaskAmount) {
		this.acceptedTaskAmount = acceptedTaskAmount;
	}

	public String getReportId() {
		return reportId;
	}

	public void setReportId(String reportId) {
		this.reportId = reportId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

}
