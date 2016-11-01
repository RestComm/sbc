package org.restcomm.chain.processor.impl;


import javax.servlet.sip.SipServletMessage;

import org.restcomm.chain.processor.impl.MutableMessage;


public class SIPMutableMessage implements  MutableMessage {
	
	private SipServletMessage  content;
	private String sourceLocalAddress;
	private String targetLocalAddress;
	private String sourceRemoteAddress;
	private String targetRemoteAddress;
	private String sourceProtocol;
	private String targetProtocol;
	private int direction;
	
	
	private boolean linked=true;
	private boolean aborted=false;
	
	public SIPMutableMessage(SipServletMessage content) {	
		this.content=content;
	}
	
	@Override
	public SipServletMessage getContent() {
		return content;
	}

	@Override
	public void setContent(Object content) {
		this.content=(SipServletMessage) content;
		
	}

	@Override
	public void unlink() {
		linked=false;
		
	}

	@Override
	public boolean isLinked() {
		return linked;
	}
	
	@Override
	public boolean isAborted() {
		return aborted;
	}

	@Override
	public void abort() {
		aborted=true;
		
	}

	@Override
	public int getDirection() {
		return direction;
	}


	public void setDirection(int direction) {
		this.direction = direction;
	}

	@Override
	public String getSourceLocalAddress() {
		return sourceLocalAddress;
	}

	public void setSourceLocalAddress(String sourceLocalAddress) {
		this.sourceLocalAddress = sourceLocalAddress;
	}

	@Override
	public String getTargetLocalAddress() {
		return targetLocalAddress;
	}

	public void setTargetLocalAddress(String targetLocalAddress) {
		this.targetLocalAddress = targetLocalAddress;
	}

	@Override
	public String getSourceRemoteAddress() {
		return sourceRemoteAddress;
	}

	public void setSourceRemoteAddress(String sourceRemoteAddress) {
		this.sourceRemoteAddress = sourceRemoteAddress;
	}

	@Override
	public String getTargetRemoteAddress() {
		return targetRemoteAddress;
	}

	public void setTargetRemoteAddress(String targetRemoteAddress) {
		this.targetRemoteAddress = targetRemoteAddress;
	}

	public String getSourceProtocol() {
		return sourceProtocol;
	}

	public void setSourceProtocol(String sourceProtocol) {
		this.sourceProtocol = sourceProtocol;
	}

	public String getTargetProtocol() {
		return targetProtocol;
	}

	public void setTargetProtocol(String targetProtocol) {
		this.targetProtocol = targetProtocol;
	}

	
}
