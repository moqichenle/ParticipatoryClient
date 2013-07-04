package ie.tcd.scss.dsg.po;


/**
 * 
 * @author Lele
 * 
 *         define user class, to store the userId, the registedId to the cloud
 *         messaging, the updated time, the location, the street's name, what
 *         kind of sensor does the user have, the mode(Still, Walking, Cycling,
 *         Driving ), accepted percentage which is used to calculate the
 *         probability of accepting a task.(=accept/(accept+deny)), the average
 *         speed of user under different mode to help to calculate how far the
 *         user moves.
 * 
 */
public class User {

	private Long userId;

	private String registerId;
	private long updatedTime;

	private UserLocation location;
	private byte hasSensor;
	private String streetName;
	
	private String mode;
	private float acceptPercent;
	private float averWalkSpeed;
	private float averDriveSpeed;
	private float averCycleSpeed;

	

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getRegisterId() {
		return registerId;
	}

	public void setRegisterId(String registerId) {
		this.registerId = registerId;
	}

	public long getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(long updatedTime) {
		this.updatedTime = updatedTime;
	}

	public UserLocation getLocation() {
		return location;
	}

	public void setLocation(UserLocation location) {
		this.location = location;
	}

	public String getStreetName() {
		return streetName;
	}

	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	public byte getHasSensor() {
		return hasSensor;
	}

	public void setHasSensor(byte hasSensor) {
		this.hasSensor = hasSensor;
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

}
