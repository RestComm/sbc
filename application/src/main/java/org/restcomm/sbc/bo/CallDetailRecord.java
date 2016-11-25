/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
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
import java.util.Currency;

import org.joda.time.DateTime;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.Immutable;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.NotThreadSafe;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 * @author pavel.slegr@telestax.com
 */
@Immutable
public class CallDetailRecord {
    private final Sid sid;
    private final String instanceId;
    private final Sid parentCallSid;
    private final DateTime dateCreated;
    private final DateTime dateUpdated;
    private final String to;
    private final String from;
    private final String status;
    private final DateTime startTime;
    private final DateTime endTime;
    private final Integer duration;
    private final Integer ringDuration;
    private final BigDecimal price;
    private final Currency priceUnit;
    private final String direction;
    private final String answeredBy;
    private final String apiVersion;
    private final String forwardedFrom;
    private final String callerName;
    private final URI uri;
    private final String callPath;
    private final Boolean muted;
    private final Boolean onHold;

    public CallDetailRecord(final Sid sid, final String instanceId, final Sid parentCallSid, final DateTime dateCreated, final DateTime dateUpdated,
            final String to, final String from, final String status,
            final DateTime startTime, final DateTime endTime, final Integer duration, final BigDecimal price,
            final Currency priceUnit, final String direction, final String answeredBy, final String apiVersion,
            final String forwardedFrom, final String callerName, final URI uri, final String callPath,final Integer ringDuration,
            final Boolean muted, final Boolean onHold) {
        super();
        this.sid = sid;
        this.instanceId = instanceId;
        this.parentCallSid = parentCallSid;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.to = to;
        this.from = from;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.price = price;
        this.priceUnit = priceUnit;
        this.direction = direction;
        this.answeredBy = answeredBy;
        this.apiVersion = apiVersion;
        this.forwardedFrom = forwardedFrom;
        this.callerName = callerName;
        this.uri = uri;
        this.callPath = callPath;
        this.ringDuration = ringDuration;
        this.muted = muted;
        this.onHold = onHold;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Sid getSid() {
        return sid;
    }

    public String getInstanceId() { return instanceId; }

    public Sid getParentCallSid() {
        return parentCallSid;
    }

    public DateTime getDateCreated() {
        return dateCreated;
    }

    public DateTime getDateUpdated() {
        return dateUpdated;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getStatus() {
        return status;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public Integer getRingDuration() {
        return ringDuration;
    }

    public BigDecimal getPrice() {
        return (price == null) ? new BigDecimal("0.0") : price;
    }

    public Currency getPriceUnit() {
        return (priceUnit == null) ? Currency.getInstance("USD") : priceUnit;
    }

    public String getDirection() {
        return direction;
    }

    public String getAnsweredBy() {
        return answeredBy;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getForwardedFrom() {
        return forwardedFrom;
    }

    public String getCallerName() {
        return callerName;
    }

    public URI getUri() {
        return uri;
    }

    public String getCallPath() {
        return callPath;
    }

    public Boolean isMuted() {
        return muted;
    }

    public Boolean isOnHold() {
        return onHold;
    }

    public CallDetailRecord setStatus(final String status) {
        return new CallDetailRecord(sid, instanceId, parentCallSid, dateCreated, DateTime.now(), to, from, 
                status, startTime, endTime, duration, price, priceUnit, direction, answeredBy, apiVersion, forwardedFrom,
                callerName, uri, callPath, ringDuration, muted, onHold);
    }

    public CallDetailRecord setStartTime(final DateTime startTime) {
        return new CallDetailRecord(sid, instanceId, parentCallSid, dateCreated, DateTime.now(), to, from, 
                status, startTime, endTime, duration, price, priceUnit, direction, answeredBy, apiVersion, forwardedFrom,
                callerName, uri, callPath, ringDuration, muted, onHold);
    }

    public CallDetailRecord setEndTime(final DateTime endTime) {
        return new CallDetailRecord(sid, instanceId, parentCallSid, dateCreated, DateTime.now(), to, from, 
                status, startTime, endTime, duration, price, priceUnit, direction, answeredBy, apiVersion, forwardedFrom,
                callerName, uri, callPath, ringDuration, muted, onHold);
    }

    public CallDetailRecord setDuration(final Integer duration) {
        return new CallDetailRecord(sid, instanceId, parentCallSid, dateCreated, DateTime.now(), to, from, 
                status, startTime, endTime, duration, price, priceUnit, direction, answeredBy, apiVersion, forwardedFrom,
                callerName, uri, callPath, ringDuration, muted, onHold);
    }

    public CallDetailRecord setRingDuration(final Integer ringDuration) {
        return new CallDetailRecord(sid, instanceId, parentCallSid, dateCreated, DateTime.now(), to, from, 
                status, startTime, endTime, duration, price, priceUnit, direction, answeredBy, apiVersion, forwardedFrom,
                callerName, uri, callPath, ringDuration, muted, onHold);
    }

    public CallDetailRecord setPrice(final BigDecimal price) {
        return new CallDetailRecord(sid, instanceId, parentCallSid, dateCreated, DateTime.now(), to, from,
                status, startTime, endTime, duration, price, priceUnit, direction, answeredBy, apiVersion, forwardedFrom,
                callerName, uri, callPath, ringDuration, muted, onHold);
    }

    public CallDetailRecord setAnsweredBy(final String answeredBy) {
        return new CallDetailRecord(sid, instanceId, parentCallSid, dateCreated, DateTime.now(), to, from, 
                status, startTime, endTime, duration, price, priceUnit, direction, answeredBy, apiVersion, forwardedFrom,
                callerName, uri, callPath, ringDuration, muted, onHold);
    }


    public CallDetailRecord setMuted(final Boolean muted){
        return new CallDetailRecord(sid, instanceId, parentCallSid, dateCreated, DateTime.now(), to, from, 
                status, startTime, endTime, duration, price, priceUnit, direction, answeredBy, apiVersion, forwardedFrom,
                callerName, uri, callPath, ringDuration, muted,  onHold);
    }

  
    public CallDetailRecord setOnHold(final Boolean onHold){
        return new CallDetailRecord(sid, instanceId, parentCallSid, dateCreated, DateTime.now(), to, from, 
                status, startTime, endTime, duration, price, priceUnit, direction, answeredBy, apiVersion, forwardedFrom,
                callerName, uri, callPath, ringDuration, muted, onHold);
    }
    
    @Override
	public boolean equals(Object cdr) {
		CallDetailRecord otherRecord=(CallDetailRecord) cdr;
		if (!(cdr instanceof CallDetailRecord)) {
			return false;
		}
		
		if (otherRecord.getSid().equals(getSid())) {
			return true;
		}
		return false;
		
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((sid == null) ? 0 : sid.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((dateCreated == null) ? 0 : dateCreated.hashCode());
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		return result;

	}

    @NotThreadSafe
    public static final class Builder {
        private Sid sid;
        private String instanceId;
        private Sid parentCallSid;
        private DateTime dateCreated;
        private DateTime dateUpdated;
        private String to;
        private String from;
        private String status;
        private DateTime startTime;
        private DateTime endTime;
        private Integer duration;
        private Integer ringDuration;
        private BigDecimal price;
        private Currency priceUnit;
        private String direction;
        private String answeredBy;
        private String apiVersion;
        private String forwardedFrom;
        private String callerName;
        private URI uri;
        private String callPath;
        private Boolean muted;
        private Boolean onHold;

        private Builder() {
            super();
            sid = null;
            instanceId = null;
            parentCallSid = null;
            dateCreated = null;
            dateUpdated = DateTime.now();
            to = null;
            from = null;
            status = null;
            startTime = null;
            endTime = null;
            duration = null;
            price = null;
            direction = null;
            answeredBy = null;
            apiVersion = null;
            forwardedFrom = null;
            callerName = null;
            uri = null;
            callPath = null;
            ringDuration = null;
            muted = null;
            onHold = null;
        }

        public CallDetailRecord build() {
            return new CallDetailRecord(sid, instanceId, parentCallSid, dateCreated, dateUpdated, to, from, 
                    status, startTime, endTime, duration, price, priceUnit, direction, answeredBy, apiVersion, forwardedFrom,
                    callerName, uri, callPath, ringDuration, muted, onHold);
        }

        public void setSid(final Sid sid) {
            this.sid = sid;
        }

        public void setInstanceId(final String instanceId) { this.instanceId = instanceId; }

        public void setParentCallSid(final Sid parentCallSid) {
            this.parentCallSid = parentCallSid;
        }

        public void setDateCreated(final DateTime dateCreated) {
            this.dateCreated = dateCreated;
        }

        public void setTo(final String to) {
            this.to = to;
        }

        public void setFrom(final String from) {
            this.from = from;
        }

        public void setStatus(final String status) {
            this.status = status;
        }

        public void setStartTime(final DateTime startTime) {
            this.startTime = startTime;
        }

        public void setEndTime(final DateTime endTime) {
            this.endTime = endTime;
        }

        public void setDuration(final Integer duration) {
            this.duration = duration;
        }

        public void setPrice(final BigDecimal price) {
            this.price = price;
        }

        public void setPriceUnit(final Currency priceUnit) {
            this.priceUnit = priceUnit;
        }

        public void setDirection(final String direction) {
            this.direction = direction;
        }

        public void setAnsweredBy(final String answeredBy) {
            this.answeredBy = answeredBy;
        }

        public void setApiVersion(final String apiVersion) {
            this.apiVersion = apiVersion;
        }

        public void setForwardedFrom(final String forwardedFrom) {
            this.forwardedFrom = forwardedFrom;
        }

        public void setCallerName(final String callerName) {
            this.callerName = callerName;
        }

        public void setUri(final URI uri) {
            this.uri = uri;
        }

        public void setCallPath(final String callPath) {
            this.callPath = callPath;
        }

        public void setMuted(final Boolean muted) {
            this.muted = muted;
        }

        public void setOnHold(final Boolean onHold) {
            this.onHold = onHold;
        }
    }
}
