package ie.tcd.scss.dsg.particpatory;

import ie.tcd.scss.dsg.po.User;
import android.app.Application;

public class AppContext extends Application {
//	private static final String TAG = "AppContext";
	private String userId;//added user into the database
	private String registeredId;//registered to GCM
	private User user;
	private String nickName;
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
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

	

}
