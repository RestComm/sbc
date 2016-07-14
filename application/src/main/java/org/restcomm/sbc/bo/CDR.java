package org.restcomm.sbc.bo;
// Generated 24-jun-2016 11:18:37 by Hibernate Tools 3.5.0.Final


import org.joda.time.DateTime;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    1/7/2016 17:35:29
 * @class   CDR.java
 *
 */
public class CDR implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1253408456489126710L;
	private int uid;
	private String fromUser;
	private String toUser;
	private String fromIp;
	private DateTime startTime;
	private DateTime endTime;
	private Integer duration;
	private Integer status;

	public CDR() {
	}

	public CDR(int uid) {
		this.uid = uid;
	}

	public CDR(int uid, String fromUser, String toUser, String fromIp, DateTime startTime, DateTime endTime, Integer duration,
			Integer status) {
		this.uid = uid;
		this.fromUser = fromUser;
		this.toUser = toUser;
		this.fromIp = fromIp;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.status = status;
	}

	public int getUid() {
		return this.uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getFromUser() {
		return this.fromUser;
	}

	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}

	public String getToUser() {
		return this.toUser;
	}

	public void setToUser(String toUser) {
		this.toUser = toUser;
	}

	public String getFromIp() {
		return this.fromIp;
	}

	public void setFromIp(String fromIp) {
		this.fromIp = fromIp;
	}

	
	public DateTime getStartTime() {
		return this.startTime;
	}

	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}

	
	public DateTime getEndTime() {
		return this.endTime;
	}

	public void setEndTime(DateTime endTime) {
		this.endTime = endTime;
	}

	public Integer getDuration() {
		return this.duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public Integer getStatus() {
		return this.status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}
