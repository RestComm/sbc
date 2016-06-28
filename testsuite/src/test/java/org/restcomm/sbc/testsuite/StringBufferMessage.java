package org.restcomm.sbc.testsuite;

import java.util.Properties;

import org.restcomm.chain.processor.impl.MutableMessage;

public class StringBufferMessage implements MutableMessage {
	
	private Properties properties=new Properties();
	private boolean linked=true;

	public StringBufferMessage(String content) {
		StringBuffer ct = new StringBuffer(content);
		ct.append(":"+ct.hashCode());
		setProperty("content", ct);
	}
	
	@Override
	public Object getProperty(Object property) {
		return properties.get(property);
	}

	@Override
	public void setProperty(Object property, Object value) {
		properties.put(property, value);
		
	}

	@Override
	public boolean isLinked() {
		return linked;
	}

	@Override
	public void unlink() {
		linked=false;
		
	}

}
