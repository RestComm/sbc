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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.event.EventListenerList;
import org.apache.log4j.Logger;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    24 nov. 2016 6:08:46
 * @class   MediaSession.java
 *
 */
public class MediaSession implements MediaZoneListener {
	
	private static transient Logger LOG = Logger.getLogger(MediaSession.class);
	private EventListenerList listenerList = new EventListenerList();

	private State state;
	private String sessionId;
	private MediaController offer;
	private MediaController answer;
	protected ScheduledExecutorService timeService;
	
	
	
	public MediaSession(String id)   {
		this.sessionId=id;
		this.setState(State.INACTIVE);
		
	}
	
	private String toPrint() {
    	return "[MediaSession ("+sessionId+")]";
    }
	
	public void setAnswer(MediaController answer) {
		this.answer=answer;
		
	}
	
	public void setOffer(MediaController offer) {
		this.offer=offer;
		
	}
	
	public void attach() {
		answer.attach(offer);
	}
	
	
	public void addMediaSessionListener(MediaSessionListener listener) {
	     listenerList.add(MediaSessionListener.class, listener);
	}
	
	protected void fireRTPTimeoutEvent(MediaZone mediaZone, String message) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==MediaSessionListener.class) {             
	             ((MediaSessionListener)listeners[i+1]).onRTPTimeout(this, mediaZone, message);
	         }
	         
	     }
	 }
	
	protected void fireRTPTerminatedEvent(MediaZone mediaZone, String message) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==MediaSessionListener.class) {             
	             ((MediaSessionListener)listeners[i+1]).onRTPTerminated(this, mediaZone, message);
	         }
	         
	     }
	 }
	
	public void start() throws UnknownHostException  {
		if(LOG.isInfoEnabled()) {
			LOG.info("Starting "+this.toPrint());	
		}
		offer.start();
		answer.start();
		timeService = Executors.newSingleThreadScheduledExecutor();
		timeService.scheduleWithFixedDelay(new MediaTimer(), 60, 60, TimeUnit.SECONDS);
		this.setState(State.ACTIVE);
	}
	
	public void finalize() throws IOException {
		if(LOG.isInfoEnabled()) {
			LOG.info("Finalizing "+this.toPrint());	
		}
		if(offer!=null)
			offer.finalize();
		if(answer!=null)
			answer.finalize();
		if(timeService!=null) {
			timeService.shutdown();
			timeService=null;
		}
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
	    	MediaZone offerZone =offer.checkStreaming();
	    	MediaZone answerZone=answer.checkStreaming();
		    if(offerZone!=null) {
		    	// either leg is stuck	
		    	fireRTPTimeoutEvent(offerZone,"Controller detected a media flow stuck!"); 	
		    }
		    if(answerZone!=null) {
		    	// either leg is stuck	
		    	fireRTPTimeoutEvent(answerZone,"Controller detected a media flow stuck!"); 	
		    }
	             
	    }
	}
	

	

	public MediaController getOffer() {
		return offer;
	}

	public MediaController getAnswer() {
		return answer;
	}

	@Override
	public void onRTPTerminated(MediaZone mediaZone, String message) {
		this.fireRTPTerminatedEvent(mediaZone, message);
		
	}
	
	@Override
	public void onRTPTimeout(MediaZone mediaZone, String message) {
		this.fireRTPTimeoutEvent(mediaZone, message);
		
	}
}
