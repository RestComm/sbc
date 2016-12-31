package org.restcomm.sbc.call;

import java.util.EventListener;


public interface CallListener extends EventListener {
	
	void onCallInitiating();
	void onCallCompleted();
	void onCallFailed();
	void onCallRinging();
	void onCallAlerting();
	void onCallBridged();
	
}
