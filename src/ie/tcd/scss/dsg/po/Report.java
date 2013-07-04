package ie.tcd.scss.dsg.po;


/**
 * 
 * @author Lele
 * 
 *         store user reports
 */
public class Report {

	private Long reportId;

	private Long userId;
	private byte categoryId;
	private String contend;
	private long reportTime;

	private byte[] attachment;
	private User user;
	
	

	public Long getReportId() {
		return reportId;
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public byte getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(byte categoryId) {
		this.categoryId = categoryId;
	}

	public String getContend() {
		return contend;
	}

	public void setContend(String contend) {
		this.contend = contend;
	}

	public long getReportTime() {
		return reportTime;
	}

	public void setReportTime(long reportTime) {
		this.reportTime = reportTime;
	}


	public byte[] getAttachment() {
		return attachment;
	}

	public void setAttachment(byte[] attachment) {
		this.attachment = attachment;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}


}
