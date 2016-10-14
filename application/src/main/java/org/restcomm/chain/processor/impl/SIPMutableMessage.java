package org.restcomm.chain.processor.impl;


import javax.servlet.sip.SipServletMessage;
import org.restcomm.chain.processor.impl.MutableMessage;

public class SIPMutableMessage implements  MutableMessage {
	
	private SipServletMessage  content;
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



}
