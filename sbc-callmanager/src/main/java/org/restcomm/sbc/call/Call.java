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

package org.restcomm.sbc.call;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;


import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.bo.CallDetailRecord;
import org.restcomm.sbc.bo.Sid;
import org.restcomm.sbc.call.CallStateChanged.State;

import org.restcomm.sbc.call.CreateCall;

import org.restcomm.sbc.media.MediaSession;
import org.restcomm.sbc.media.MediaSessionListener;
import org.restcomm.sbc.media.MediaZone;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    24 nov. 2016 5:58:31
 * @class   Call.java
 *
 */
public class Call  implements MediaSessionListener {
	
	private static transient Logger LOG = Logger.getLogger(Call.class);
	
	private Call parent;
	private Sid sid;
	private CallDetailRecord cdr;
	private MediaSession mediaSession;
	private State status;
	private Direction direction;
	private String sessionId;
	
	
	  
	private final CreateCall.Type type;
	   
	private final DateTime dateCreated;
	private final DateTime dateConUpdated;
	private final String fromName;
	private final String from;
	private final String to;

	
	private EventListenerList listenerList = new EventListenerList();
	
	
	public Call(Call parent, final String sessionId, final String to, final String from,    
            final String direction, 
            final String fromName) {
		
		this.parent=parent;
		this.sessionId=sessionId;
		this.sid=Sid.generate(Sid.Type.RANDOM);
		Sid parentCallSid=Sid.generate(Sid.Type.RANDOM);
		this.dateCreated=DateTime.now();
		this.dateConUpdated=DateTime.now();
		this.type = CreateCall.Type.SIP;
		this.to= to;
		this.from = from;
		this.fromName = fromName;
		String apiVersion=ConfigurationCache.getApiVersion();
		
		mediaSession=new MediaSession(sessionId);
		mediaSession.addMediaSessionListener(this);
		
		URI uri=null;
		try {
			uri = new URI(apiVersion+"/Calls/"+sid.toString());
		} catch (URISyntaxException e) {
			LOG.error("Cannot Create URI");
		}
		
		this.direction=Direction.getValueOf(direction);
		this.status=State.QUEUED;
			
		this.cdr=new CallDetailRecord(sid, "", parentCallSid, dateCreated, dateCreated, to, from, 
                status.name(), dateCreated, dateCreated, 0, new BigDecimal(0), null, direction, null, apiVersion, null,
                fromName, uri, null, 0, false, false);	
	}
	
	public void addCallListener(CallListener listener) {
	     listenerList.add(CallListener.class, listener);
	}
	
	public CallDetailRecord getCdr() {
		return cdr;
	}

	public MediaSession getMediaSession() {
		return mediaSession;
	}
		
	
	

	public enum Direction {
		INBOUND("inbound"),
        OUTBOUND("outbound");

        private final String text;

        private Direction(final String text) {
            this.text = text;
        }

        public static Direction getValueOf(final String text) {
        	Direction[] values = values();
            for (final Direction value : values) {
                if (value.toString().equals(text)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(text + " is not a valid call direction.");
        }

        @Override
        public String toString() {
            return text;
        }
    }


	public State getStatus() {
		return status;
	}

	public Direction getDirection() {
		return direction;
	}

	public Sid getSid() {
		return sid;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	protected void setStatus(int statusCode, String reasonPhrase) {
		switch(statusCode) {
		case 404:
			setStatus(CallStateChanged.State.NOT_FOUND);
			return;
		case 408:
			setStatus(CallStateChanged.State.NO_ANSWER);
			return;
		case 480:
		case 486:
			setStatus(CallStateChanged.State.BUSY);
			return;
		case 487:
			setStatus(CallStateChanged.State.CANCELED);
			return;
		}			
		if(statusCode>=400 ) {
			setStatus(CallStateChanged.State.FAILED);
			return;
		}
		else if(statusCode>200)
			setStatus(CallStateChanged.State.RINGING);
		if(statusCode>=300 )
			setStatus(CallStateChanged.State.FAILED);
		else if(statusCode>=200)
			setStatus(CallStateChanged.State.COMPLETED);
	
	}

	protected void setStatus(State status) {
		
		if(LOG.isTraceEnabled()) {
			LOG.trace("Call was in status "+getStatus()+", now changing to "+status.name());
		}
		
		this.status = status;
		cdr=cdr.setStatus(status.name());
				
		switch(status) {
		case RINGING:
			cdr=cdr.setStartTime(DateTime.now());
			break;
		case IN_PROGRESS:
			if(LOG.isTraceEnabled()) {
				LOG.trace("Datecreated "+cdr.getDateCreated());
				LOG.trace("StartTime   "+cdr.getStartTime());
			}
			cdr=cdr.setRingDuration((int)(cdr.getStartTime().minus(cdr.getDateCreated().getMillis())).getMillis()/1000);
			break;
		case COMPLETED:
			cdr=cdr.setEndTime(DateTime.now());
			cdr=cdr.setDuration((int)(cdr.getEndTime().minus(cdr.getStartTime().getMillis())).getMillis()/1000);
			break;
		case FAILED:
			cdr=cdr.setEndTime(DateTime.now());
			cdr=cdr.setDuration((int)(cdr.getEndTime().minus(cdr.getStartTime().getMillis())).getMillis()/1000);
			break;
			
		default:
			break;
			
		}	
	}
	
	@Override
	public boolean equals(Object call) {
		Call otherCall=(Call) call;
		if (!(call instanceof Call)) {
			return false;
		}
		
		if (otherCall.getSessionId().equals(getSessionId())) {
			return true;
		}
		return false;
		
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
		result = prime * result + ((sid == null) ? 0 : sid.hashCode());
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		return result;

	}

	public Call getParent() {
		return parent;
	}

	public EventListenerList getListeners() {
		return listenerList;
	}

	@Override
	public void onMediaTimeout(MediaSession session, MediaZone zone) {
		if(LOG.isInfoEnabled()) {
			LOG.warn("Force ending media on "+zone.toPrint());
			LOG.info("TODO a BYE here ");
		}
		
		
	}

	@Override
	public void onMediaTerminated(MediaSession mediaSession, MediaZone mediaZone) {
		if(mediaSession.isActive()){
			//mediaSession.finalize();
		}
	}
	
	@Override
	public void onMediaFailed(MediaSession mediaSession, MediaZone mediaZone) {
		LOG.error("MediaSession/Zone Failed!");
		if(mediaSession.isActive())
			mediaSession.finalize();
		
	}

	@Override
	public void onMediaReady(MediaSession mediaSession, MediaZone mediaZone) {
		try {
			if(!mediaSession.isActive())
				mediaSession.start();
			if(!mediaZone.isRunning())
				mediaZone.start();
		} catch (IOException e) {
			LOG.error("Cannot start MediaSession/Zone",e);
		}
		
	}

	public CreateCall.Type getType() {
		return type;
	}

	public DateTime getDateCreated() {
		return dateCreated;
	}

	public DateTime getDateConUpdated() {
		return dateConUpdated;
	}

	public String getFromName() {
		return fromName;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

}
