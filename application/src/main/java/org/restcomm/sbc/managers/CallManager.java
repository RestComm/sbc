/*******************************************************************************
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc, Eolos IT Corp and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.restcomm.sbc.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.restcomm.sbc.bo.Call;
import org.restcomm.sbc.bo.Call.Status;
import org.restcomm.sbc.bo.CallDetailRecord;
import org.restcomm.sbc.bo.CallDetailRecordFilter;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    24 nov. 2016 4:41:54
 * @class   CallManager.java
 *
 */
public class CallManager  {
	
	private Cache<String, Call> calls;
	private static CallManager callManager;
	private static final Logger LOG = Logger.getLogger(CallManager.class);

	
	private CallManager() {
		calls = CacheManager.getCacheManager().getCache("callmanager");
		calls.start();
		
	}
	
	public static CallManager getCallManager() {
		if(callManager==null) {
			callManager=new CallManager();
		}
		
		return callManager;
	}
	
	
	public Call createCall(final String sessionId,
            final String to, final String from,    
            final String direction, 
            final String forwardedFrom, final String callerName) {
		
	        
	    Call call=new Call(sessionId, to, from, direction, callerName);
	    
	    calls.put(sessionId, call);
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("calls.put "+sessionId+" hashcode "+call.hashCode());
		}
		
		return call;
		
	}
	
	public Call remove(String sessionId) {
		return calls.remove(sessionId);
	}
	
	
	public Call getCall(String sessionId)  {
		Call call=calls.get(sessionId);
		if(LOG.isTraceEnabled()) {
			if(call==null)
				LOG.error("cannot find call for SessionID "+sessionId);
			else
				LOG.trace("call hashCode "+call.hashCode());
		}
		
		return call;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Call> getCalls() {	
		ArrayList al=new ArrayList(calls.values());
		//Collections.sort(al);
		return al;
	}
	
	
	public int getTotalCalls(CallDetailRecordFilter filterForTotal) {
		int counter = 0;
		
		for(Object l:calls.values()) {
			CallDetailRecord call=(CallDetailRecord)l;
			String recipient=filterForTotal.getRecipient();
			String sender=filterForTotal.getSender();
			
			
			if(recipient!=null) {
				if(!call.getAnsweredBy().startsWith(recipient.replace("%", "")))
					continue;	
			}
			if(sender!=null) {
				if(!call.getFrom().startsWith(sender.replace("%", ""))) 
					continue;	
			}
			
			counter++;	
		}
		return counter;
	}

	public List<Call> getCalls(CallDetailRecordFilter filter) {	
		int offsetCounter=0;
		int limitCounter=0;
		int limit=filter.getLimit();
		int offset=filter.getOffset();
		
		ArrayList<Call> acalls=new ArrayList<Call>();
		
		for(Object l:calls.values()) {
			Call call=(Call)l;
			String recipient=filter.getRecipient();
			String sender=filter.getSender();
			
			
			if(recipient!=null) {
				if(!call.getCdr().getAnsweredBy().startsWith(recipient.replace("%", "")))
					continue;	
			}
			if(sender!=null) {
				if(!call.getCdr().getFrom().startsWith(sender.replace("%", ""))) 
					continue;	
			}
			offsetCounter++;
			
			if(offsetCounter>offset && limitCounter<limit) {
				acalls.add(call);
				limitCounter++;
			}
				
		}
		return acalls;
				
	}

	
}
