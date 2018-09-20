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

package org.restcomm.sbc.media;

import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.event.EventListenerList;
import org.apache.log4j.Logger;
import org.mobicents.media.server.io.sdp.SdpException;
import org.restcomm.sbc.media.MediaController.StreamProfile;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    24 nov. 2016 6:08:46
 * @class   MediaSession.java
 *
 */
public class MediaSession  {
	
	private static transient Logger LOG = Logger.getLogger(MediaSession.class);
	private EventListenerList listenerList = new EventListenerList();

	private State state;
	private String sessionId;
	private MediaController offer;
	private MediaController answer;
	protected ScheduledExecutorService timeService;
	private PortManager portManager=PortManager.getPortManager();
	
	public final int MEDIATYPE_AUDIO   = portManager.getNextAvailablePort();
	public final int MEDIATYPE_VIDEO   = portManager.getNextAvailablePort();
	
	public final int[] proxyPorts = { 
			MEDIATYPE_AUDIO,
			MEDIATYPE_VIDEO		
	};
	
	public MediaSession(String id)   {
		this.sessionId=id;
		this.setState(State.INACTIVE);
		
	}
	
	private String toPrint() {
    	return "[MediaSession ("+sessionId+")]";
    }
	
	public MediaController buildOffer(StreamProfile streamProfile, String sdpOffer, String targetProxyAddress) throws UnknownHostException, SdpException {
		this.offer=new MediaController(this, streamProfile, MediaZone.Direction.OFFER, sdpOffer, targetProxyAddress);
		return this.offer;
	}
	
	public MediaController buildAnswer(StreamProfile streamProfile, String sdpAnswer, String targetProxyAddress) throws UnknownHostException, SdpException {
		this.answer=new MediaController(this, streamProfile, MediaZone.Direction.ANSWER, sdpAnswer, targetProxyAddress);
		return this.answer;
	}
	
	public void attach() {
		if(answer==null || offer==null) {
			throw new IllegalStateException("Cannot attach uninitialized MediaControllers!");
		}
		// Attach the offer to the answer only
		answer.attach(offer);
	}
	
	
	public void addMediaSessionListener(MediaSessionListener listener) {
	     listenerList.add(MediaSessionListener.class, listener);
	}
	
	protected void fireMediaTimeoutEvent(MediaZone mediaZone) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==MediaSessionListener.class) {             
	             ((MediaSessionListener)listeners[i+1]).onMediaTimeout(this, mediaZone);
	         }
	         
	     }
	 }
	
	protected void fireMediaTerminatedEvent(MediaZone mediaZone) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==MediaSessionListener.class) {             
	             ((MediaSessionListener)listeners[i+1]).onMediaTerminated(this, mediaZone);
	         }
	         
	     }
	 }
	
	protected void fireMediaReadyEvent(MediaZone mediaZone) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==MediaSessionListener.class) {             
	             ((MediaSessionListener)listeners[i+1]).onMediaReady(this, mediaZone);
	         }
	         
	     }
	 }
	
	protected void fireMediaFailedEvent(MediaZone mediaZone) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==MediaSessionListener.class) {             
	             ((MediaSessionListener)listeners[i+1]).onMediaFailed(this, mediaZone);
	         }
	         
	     }
	 }
	
	public void start() throws UnknownHostException  {
		if(LOG.isInfoEnabled()) {
			LOG.info("Starting "+this.toPrint());	
		}
		// MediaZones are started atomically
		// while mediaTypes are ready
		
		timeService = Executors.newSingleThreadScheduledExecutor();
		timeService.scheduleWithFixedDelay(new MediaTimer(), 60, 60, TimeUnit.SECONDS);
		this.setState(State.ACTIVE);
	}
	
	public void finalize()  {
	
		if(offer!=null)
			offer.finalize();
		if(answer!=null)
			answer.finalize();
		
		if(timeService!=null) {
			timeService.shutdown();
			timeService=null;
		}
		
		answer=offer=null;
		this.setState(State.CLOSED);
	}
	
	public enum State {
		ACTIVE("active"), CLOSED("closed"), INACTIVE("inactive");

		private final String text;

		private State(final String text) {
		    this.text = text;
		}

		public static State getValueOf(final String text) {
		    State[] values = values();
		    for (final State value : values) {
		        if (value.toString().equals(text)) {
		            return value;
		        }
		    }
		    throw new IllegalArgumentException(text + " is not a valid media session state.");
		}

		@Override
		public String toString() {
		    return text;
		}
	}
	
	public boolean isActive() {
		return state==State.ACTIVE?true:false;
	}

	public State getState() {
		return state;
	}

	private void setState(State state) {
		this.state = state;
	}

	public String getSessionId() {
		return sessionId;
	}
	
	@Override
	public boolean equals(Object session) {
		MediaSession otherSession=(MediaSession) session;
		if (!(session instanceof MediaSession)) {
			return false;
		}
		
		if (otherSession.sessionId.equals(this.sessionId)) {
			return true;
		}
		return false;
		
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
		return result;

	}
	
	class MediaTimer implements Runnable {
	    @Override
	    public void run() {
	    	if(LOG.isDebugEnabled()) {
				LOG.debug("MediaTimer Watchdog active on "+MediaSession.this.toPrint());	
			}
	    	MediaZone offerZone =offer.checkStreaming();
	    	MediaZone answerZone=answer.checkStreaming();
		    
	    	if(offerZone!=null) {
		    	// either leg is stuck	
		    	fireMediaTimeoutEvent(offerZone);
		    	offer.finalize(offerZone);
		    }
		    if(answerZone!=null) {
		    	// either leg is stuck	
		    	fireMediaTimeoutEvent(answerZone);
		    	answer.finalize(answerZone);
		    }
	             
	    }
	}


	public MediaController getOffer() {
		return offer;
	}

	public MediaController getAnswer() {
		return answer;
	}

	
}
