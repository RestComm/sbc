package org.restcomm.sbc.bo;

import java.util.Date;


public class Call  {
	
	private int uid;

	private String fromIP;

	private String fromUser;

	private Date startTime;

	private int status;

	private String toUser;

	public Call(int uid, String fromIP, String fromUser, String toUser) {
		this.uid=uid;
		this.fromIP=fromIP;
		this.fromUser=fromUser;
		this.toUser=toUser;
		this.startTime=new Date(System.currentTimeMillis());
		this.status=-1;
	}
	public int getUid() {
		return this.uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getFromIP() {
		return this.fromIP;
	}

	public void setFromIP(String fromIP) {
		this.fromIP = fromIP;
	}

	public String getFromUser() {
		return this.fromUser;
	}

	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}

	public Date getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getToUser() {
		return this.toUser;
	}

	public void setToUser(String toUser) {
		this.toUser = toUser;
	}

}