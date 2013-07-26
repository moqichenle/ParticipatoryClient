package ie.tcd.scss.dsg.po;


/**
 * tasks that are created for unsolved queries from certain user, depending on
 * the category users queried
 * 
 * @author Lele
 * 
 */
public class TaskModel {

	private Long taskId;

	private Long queryId;
	private byte categoryId;
	private String description;
	private double latitude;
	private double longitude;
	private byte sensorType;
	private long expirePeriod;
	private long createdTime;
	private double searchRange;
	private boolean status;
	private byte[] picture;
	private String comment;

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Long getQueryId() {
		return queryId;
	}

	public void setQueryId(Long queryId) {
		this.queryId = queryId;
	}

	public byte getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(byte categoryId) {
		this.categoryId = categoryId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public byte getSensorType() {
		return sensorType;
	}

	public void setSensorType(byte sensorType) {
		this.sensorType = sensorType;
	}

	public long getExpirePeriod() {
		return expirePeriod;
	}

	public void setExpirePeriod(long expirePeriod) {
		this.expirePeriod = expirePeriod;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public double getSearchRange() {
		return searchRange;
	}

	public void setSearchRange(double searchRange) {
		this.searchRange = searchRange;
	}


	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public byte[] getPicture() {
		return picture;
	}

	public void setPicture(byte[] picture) {
		this.picture = picture;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
