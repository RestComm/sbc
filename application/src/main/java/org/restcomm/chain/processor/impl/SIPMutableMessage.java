package org.restcomm.chain.processor.impl;

import java.util.Properties;

import javax.servlet.sip.SipServletMessage;
import org.restcomm.chain.processor.impl.MutableMessage;

public class SIPMutableMessage implements  MutableMessage {
	
	private Properties properties=new Properties();
	private boolean linked=true;
	
	public SIPMutableMessage(SipServletMessage content) {	
		setProperty("content", content);
	}
	
	@Override
	public SipServletMessage getProperty(Object property) {
		return (SipServletMessage) properties.get(property);
	}

	@Override
	public void setProperty(Object property, Object value) {
		properties.put(property, value);
		
	}

	@Override
	public void unlink() {
		linked=false;
		
	}

	@Override
	public boolean isLinked() {
		return linked;
	}

}
