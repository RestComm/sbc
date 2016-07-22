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


import org.joda.time.DateTime;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.Immutable;



/**
 * Represent a user Account
 *
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@Immutable
public final class WhiteList {
    private final Sid sid;
    private final DateTime dateCreated;
    private final DateTime dateExpires;
    private final String ipAddress;
    private final Sid accountSid;
    private final Reason reason;
   

    public WhiteList(final Sid sid, final DateTime dateCreated, final DateTime dateExpires, final String ipAddress,
            final Sid accountSid, final Reason reason) {
        super();
        this.sid = sid;
        this.dateCreated = dateCreated;
        this.dateExpires = dateExpires;
        this.ipAddress = ipAddress;
        this.accountSid = accountSid;
        this.reason = reason;
       
    }

    public static Builder builder() {
        return new Builder();
    }

    public Sid getSid() {
        return sid;
    }

    public DateTime getDateCreated() {
        return dateCreated;
    }

    public DateTime getDateExpires() {
        return dateExpires;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Sid getAccountSid() {
        return accountSid;
    }

    public Reason getReason() {
        return reason;
    }

    
    public WhiteList setIpAddress(final String ipAddress) {
        return new WhiteList(sid, dateCreated, DateTime.now(), ipAddress, accountSid, reason);
    }


    public WhiteList setReason(final Reason reason) {
        return new WhiteList(sid, dateCreated, DateTime.now(), ipAddress, accountSid, reason);
    }

   
    public enum Reason {
        ADMIN("Admin"), THREAT("Threat");

        private final String text;

        private Reason(final String text) {
            this.text = text;
        }

        public static Reason getValueOf(final String text) {
            Reason[] values = values();
            for (final Reason value : values) {
                if (value.text.equals(text)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(text + " is not a valid reason.");
        }

        @Override
        public String toString() {
            return text;
        }
    };

    public static final class Builder {
        private Sid sid;
        private String ipAddress;
        private Sid accountSid;
        private Reason reason;
        
        private Builder() {
            super();
        }

        public WhiteList build() {
            final DateTime now = DateTime.now();
            return new WhiteList(sid, now, now, ipAddress, accountSid, reason);
        }

        public void setSid(final Sid sid) {
            this.sid = sid;
        }

        public void setipAddress(final String ipAddress) {
            this.ipAddress = ipAddress;
        }


        public void setAccountSid(final Sid accountSid) {
            this.accountSid = accountSid;
        }

        public void setReason(final Reason reason) {
            this.reason = reason;
        }

        
    }
}
