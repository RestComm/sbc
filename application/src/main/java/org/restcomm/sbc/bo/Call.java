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

package org.restcomm.sbc.bo;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.media.MediaSession;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    24 nov. 2016 5:58:31
 * @class   Call.java
 *
 */
public class Call  {
	
	private static transient Logger LOG = Logger.getLogger(Call.class);
	
	private Sid sid;
	private CallDetailRecord cdr;
	private MediaSession mediaSession;
	private Status status;
	private Direction direction;
	private String sessionId;
	
	public Call(final String sessionId, final String to, final String from,    
            final String direction, 
            final String callerName) {
		
		this.sessionId=sessionId;
		this.mediaSession=new MediaSession(sessionId);
		this.sid=Sid.generate(Sid.Type.RANDOM);
		Sid parentCallSid=Sid.generate(Sid.Type.RANDOM);
		DateTime dateCreated=DateTime.now();
		String apiVersion=ConfigurationCache.getApiVersion();
		
		URI uri=null;
		try {
			uri = new URI(apiVersion+"/Calls/"+sid.toString());
		} catch (URISyntaxException e) {
			LOG.error("Cannot Create URI");
		}
		
		this.direction=Direction.getValueOf(direction);
		this.status=Status.INITIATING;
			
		this.cdr=new CallDetailRecord(sid, "", parentCallSid, dateCreated, dateCreated, to, from, 
                status.text, dateCreated, dateCreated, 0, new BigDecimal(0), null, direction, null, apiVersion, null,
                callerName, uri, null, 0, false, false);
		
		
		
	}
	
	public void setMediaSession(MediaSession mediaSession) {
		this.mediaSession=mediaSession;	
	}

	public CallDetailRecord getCdr() {
		return cdr;
	}

	public MediaSession getMediaSession() {
		return mediaSession;
	}
		
	
	public enum Status {
		INITIATING("initiating"),
		BRIDGED("bridged"),
        COMPLETED("completed"),
        FAILED("failed"),
        RINGING("ringing"),
        ALERTING("alerting");

        private final String text;

        private Status(final String text) {
            this.text = text;
        }

        public static Status getValueOf(final String text) {
            Status[] values = values();
            for (final Status value : values) {
                if (value.toString().equals(text)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(text + " is not a valid call status.");
        }

        @Override
        public String toString() {
            return text;
        }
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


	public Status getStatus() {
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
	
	public void setStatus(int statusCode, String reasonPhrase) {
		if(statusCode>=400 )
			setStatus(Call.Status.FAILED);
		else if(statusCode>=200)
			setStatus(Call.Status.COMPLETED);
	
	}

	public void setStatus(Status status) {
		
		if(LOG.isTraceEnabled()) {
			LOG.trace("Call was in status "+getStatus()+", now changing to "+status.text);
		}
		
		this.status = status;
		cdr=cdr.setStatus(status.text);
				
		switch(status) {
		case RINGING:
			cdr=cdr.setStartTime(DateTime.now());
			break;
		case BRIDGED:
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

	

}
