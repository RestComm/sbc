package org.restcomm.sbc.call;

import java.util.EventListener;

import org.restcomm.sbc.call.Call;

public interface CallManagerListener extends EventListener {
	
	void onCallCreated(Call call);
	void onCallIncoming(Call call);
	void onCallDialing(Call call);
	void onCallAnswered(Call call);
	void onCallReleased(Call call);
	void onCallRejected(Call call);
	
}
