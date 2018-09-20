/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.restcomm.sbc.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.restcomm.sbc.bo.InstanceId;
import org.restcomm.sbc.bo.Location;

import org.restcomm.sbc.call.Call;
import org.restcomm.sbc.call.CallManager;
import org.restcomm.sbc.call.Call.Direction;

import org.restcomm.sbc.call.CallManagerListener;
import org.restcomm.sbc.call.CallStateChanged;

import org.restcomm.sbc.managers.MonitoringServiceResponse;







/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */
public class MonitoringService implements LocationListener, CallManagerListener {
	private static transient Logger logger = Logger.getLogger(MonitoringService.class);

    private final Map<String, Call> callMap;
    private final Map<String, Call> callDetailsMap;
    private final Map<String, CallStateChanged.State> callStateMap;
    private final Map<String, String> registeredUsers;
    private final AtomicInteger callsUpToNow;
    private final AtomicInteger incomingCallsUpToNow;
    private final AtomicInteger outgoingCallsUpToNow;
    private final AtomicInteger completedCalls;
    private final AtomicInteger failedCalls;
    private final AtomicInteger busyCalls;
    private final AtomicInteger canceledCalls;
    private final AtomicInteger noAnswerCalls;
    private final AtomicInteger notFoundCalls;
    private final AtomicInteger textInboundToApp;
    private final AtomicInteger textInboundToClient;
    private final AtomicInteger textInboundToProxyOut;
    private final AtomicInteger textOutbound;
    private final AtomicInteger textNotFound;
    private InstanceId instanceId;
    private CallManager callManager;
    private LocationManager locationManager;
  

    public MonitoringService(CallManager callManager) {
        this.callMap = new ConcurrentHashMap<String, Call>();
        this.callDetailsMap = new ConcurrentHashMap<String, Call>();
        this.callStateMap = new ConcurrentHashMap<String, CallStateChanged.State>();
        registeredUsers = new ConcurrentHashMap<String, String>();
        callsUpToNow = new AtomicInteger();
        incomingCallsUpToNow = new AtomicInteger();
        outgoingCallsUpToNow = new AtomicInteger();
        completedCalls = new AtomicInteger();
        failedCalls = new AtomicInteger();
        busyCalls = new AtomicInteger();
        canceledCalls = new AtomicInteger();
        noAnswerCalls = new AtomicInteger();
        notFoundCalls = new AtomicInteger();
        textInboundToApp = new AtomicInteger();
        textInboundToClient = new AtomicInteger();
        textInboundToProxyOut = new AtomicInteger();
        textOutbound = new AtomicInteger();
        textNotFound = new AtomicInteger();
		this.callManager = callManager;
        this.callManager.addCallManagerListener(this);
        
        locationManager = LocationManager.getLocationManager();
        locationManager.addLocationListener(this);
        
        if(logger.isInfoEnabled()){
            logger.info("Monitoring Service started");
        }
    }
    
    
    /**
     * @param message
     * @param self
     * @param sender
     */
    /*
    private void onTextMessage(TextMessage message, ActorRef self, ActorRef sender) {
        TextMessage.SmsState state = message.getState();
        if (state.equals(TextMessage.SmsState.INBOUND_TO_APP)) {
            textInboundToApp.incrementAndGet();
        } else if (state.equals(TextMessage.SmsState.INBOUND_TO_CLIENT)) {
            textInboundToClient.incrementAndGet();
        } else if (state.equals(TextMessage.SmsState.INBOUND_TO_PROXY_OUT)) {
            textInboundToProxyOut.incrementAndGet();
        } else if (state.equals(TextMessage.SmsState.OUTBOUND)) {
            textOutbound.incrementAndGet();
        } else if (state.equals(TextMessage.SmsState.NOT_FOUND)) {
            textNotFound.incrementAndGet();
        }
    }
   */
   
    /**
     * @param message
     * @param self
     * @param sender
     */
    public MonitoringServiceResponse getLiveCalls() {
        List<Call> callDetailsList = new ArrayList<Call>(callDetailsMap.values());
        Map<String, Integer> countersMap = new HashMap<String, Integer>();

        final AtomicInteger liveIncomingCalls = new AtomicInteger();
        final AtomicInteger liveOutgoingCalls = new AtomicInteger();

        countersMap.put("TotalCallsSinceUptime",callsUpToNow.get());
        countersMap.put("IncomingCallsSinceUptime", incomingCallsUpToNow.get());
        countersMap.put("OutgoingCallsSinceUptime", outgoingCallsUpToNow.get());
        countersMap.put("RegisteredUsers", registeredUsers.size());
        countersMap.put("LiveCalls", callDetailsList.size());

        for (Call callInfo : callDetailsList) {
            if (callInfo.getDirection()==Direction.INBOUND) {
                liveIncomingCalls.incrementAndGet();
            } else if (callInfo.getDirection()==Direction.OUTBOUND) {
                liveOutgoingCalls.incrementAndGet();
            }
        }
        countersMap.put("LiveIncomingCalls", liveIncomingCalls.get());
        countersMap.put("LiveOutgoingCalls", liveOutgoingCalls.get());

        countersMap.put("CompletedCalls", completedCalls.get());
        countersMap.put("NoAnswerCalls", noAnswerCalls.get());
        countersMap.put("BusyCalls", busyCalls.get());
        countersMap.put("FailedCalls", failedCalls.get());
        countersMap.put("NotFoundCalls", notFoundCalls.get());
        countersMap.put("CanceledCalls", canceledCalls.get());
        countersMap.put("TextMessageInboundToApp", textInboundToApp.get());
        countersMap.put("TextMessageInboundToClient", textInboundToClient.get());
        countersMap.put("TextMessageInboundToProxyOut", textInboundToProxyOut.get());
        countersMap.put("TextMessageNotFound", textNotFound.get());
        countersMap.put("TextMessageOutbound", textOutbound.get());

        return new MonitoringServiceResponse(instanceId, callDetailsList, countersMap);
       
    }

 

	@Override
	public void onCallCreated(Call call) {
		
        callMap.put(call.getSid().toString(), call);
        //callsUpToNow.incrementAndGet();
		
	}

	@Override
	public void onCallIncoming(Call call) {
		String senderPath=call.getSid().toString();
		
        callDetailsMap.put(senderPath, call);
        callsUpToNow.incrementAndGet();

        incomingCallsUpToNow.incrementAndGet();
       
	}

	@Override
	public void onCallDialing(Call call) {
		String senderPath=call.getSid().toString();
		
        callDetailsMap.put(senderPath, call);
        callsUpToNow.incrementAndGet();

        outgoingCallsUpToNow.incrementAndGet();
      
	}

	@Override
	public void onCallAnswered(Call call) {
		String senderPath = call.getSid().toString();        
        
        callStateMap.put(senderPath, call.getStatus());
		completedCalls.incrementAndGet();
	}

	@Override
	public void onCallReleased(Call call) {
		String senderPath = call.getSid().toString();
		callMap.remove(senderPath);
        callDetailsMap.remove(senderPath);
        callStateMap.remove(senderPath);
		
	}

	@Override
	public void onCallRejected(Call call) {
		String senderPath = call.getSid().toString();
		callMap.remove(senderPath);
        callDetailsMap.remove(senderPath);
        callStateMap.remove(senderPath);
		
	}

	@Override
	public void onRegistered(Location location) {		
        registeredUsers.put(location.getUser(), location.getHost());	
	}

	@Override
	public void onUnregistered(Location location) {
         if (registeredUsers.containsKey(location.getUser())) {
             registeredUsers.remove(location.getUser());
         }  
		
	}


	@Override
	public void onCallNotAnswered(Call call) {
		String senderPath = call.getSid().toString();        
            
        callStateMap.put(senderPath, call.getStatus());
		noAnswerCalls.incrementAndGet();
		
	}

	@Override
	public void onCallNotFound(Call call) {
		String senderPath = call.getSid().toString();        
        
        callStateMap.put(senderPath, call.getStatus());
		notFoundCalls.incrementAndGet();
		
	}

	@Override
	public void onCallBusy(Call call) {
		String senderPath = call.getSid().toString();        
        
        callStateMap.put(senderPath, call.getStatus());
		busyCalls.incrementAndGet();
		
	}

	@Override
	public void onCallFailed(Call call) {
		String senderPath = call.getSid().toString();        
        
        callStateMap.put(senderPath, call.getStatus());
		failedCalls.incrementAndGet();
		
	}

	@Override
	public void onCallCanceled(Call call) {
		String senderPath = call.getSid().toString();        
        
        callStateMap.put(senderPath, call.getStatus());
		canceledCalls.incrementAndGet();
		
	}

}
