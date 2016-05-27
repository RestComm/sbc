package org.restcomm.sbc.notification;

import javax.servlet.sip.SipServletMessage;

import org.restcomm.sbc.threat.Threat;

public class SuspectActivity implements SuspectActivityElectable {
	
	protected long timestamp;
	protected String host;
	protected Threat threatCandidate;
	protected int unauthorizedAccessCount = 1;
	protected SipServletMessage message;

	public SuspectActivity () {
		this.timestamp = System.currentTimeMillis();
		this.threatCandidate=new Threat();
		this.threatCandidate.setType(Threat.Type.POTENTIAL);
	}
	
	public SuspectActivity (String host) {
		this();
		this.host = host;
	}
	

	public boolean isExpired() {
		// TODO Auto-generated method stub
		return false;
	}

	public Threat becomesThreatCandidate() {
		return threatCandidate;
	}

	public String getHost() {
		return host;
	}

	public int getUnauthorizedAccessCount() {
		return unauthorizedAccessCount;
	}

	public SipServletMessage getLastMessage() {
		return message;
	}
	
	public void setLastMessage(SipServletMessage message) {
		this.message=message;
	}

	public void setUnauthorizedAccessCount(int unauthorizedAccessCount) {
		this.unauthorizedAccessCount = unauthorizedAccessCount;
	}

}
