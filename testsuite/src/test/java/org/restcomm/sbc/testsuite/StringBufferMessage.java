package org.restcomm.sbc.testsuite;


import org.restcomm.chain.processor.impl.MutableMessage;

public class StringBufferMessage implements MutableMessage {
	
	private StringBuffer content=new StringBuffer();
	private boolean linked=true;
	private boolean aborted=false;

	public StringBufferMessage(String content) {
		StringBuffer ct = new StringBuffer(content);
		ct.append(":"+ct.hashCode());
		setContent(ct);
	}
	

	@Override
	public boolean isLinked() {
		return linked;
	}

	@Override
	public void unlink() {
		linked=false;
		
	}

	@Override
	public Object getContent() {
		return content;
	}

	@Override
	public void setContent(Object value) {
		this.content=(StringBuffer) value;
		
	}


	@Override
	public void abort() {
		aborted=true;
		
	}


	@Override
	public boolean isAborted() {
		return aborted;
	}

}
