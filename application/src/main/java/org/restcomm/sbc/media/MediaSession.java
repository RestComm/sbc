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
import java.util.HashMap;

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
	private HashMap<String, MediaZone> session=new HashMap<String, MediaZone>();
	private State state;
	private String sessionId;
	
	public MediaSession(String id) {
		this.sessionId=id;
		this.setState(State.INACTIVE);
		
	}
	
	public void addMediaZone(MediaZone mediaZone) {	
		/*
		 * MediaZones can only be added when both legs are already attached
		 */
		if(mediaZone.getMediaZonePeer()==null) {
			throw new IllegalStateException("MediaZone is not already bridged!");
		}
		session.put(mediaZone.getMediaType(), mediaZone);
		mediaZone.addMediaZoneListener(this);
		
	}
	
	public void addMediaSessionListener(MediaSessionListener listener) {
	     listenerList.add(MediaSessionListener.class, listener);
	}
	
	protected void fireRTPTimeoutEvent(String mediaType, String message) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i] instanceof MediaZoneListener) {             
	             //((MediaZoneListener)listeners[i+1]).onRTPTimeout(mediaType, message);
	         }
	         
	     }
	 }
	
	public void start()  {
		for(MediaZone mediaZone:session.values()) {
			try {
				mediaZone.start();
			} catch (UnknownHostException e) {
				LOG.error("Cannot start MediaType "+mediaZone.getMediaType());
			}
		}
		this.setState(State.ACTIVE);
	}
	
	public void stop() {
		for(MediaZone mediaZone:session.values()) {
			try {
				mediaZone.finalize();
				
			} catch (IOException e) {
				LOG.error("Cannot finalize MediaType "+mediaZone.getMediaType());
			}
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
	public void onRTPTimeout(MediaZone mediaZone, String message) {
		this.fireRTPTimeoutEvent(mediaZone.getMediaType(), message);
		
	};
}
