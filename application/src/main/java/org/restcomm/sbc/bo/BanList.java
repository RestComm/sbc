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
 * @author Oscar Andres Carriles <ocarriles@eolos.la>.
 *******************************************************************************/
package org.restcomm.sbc.bo;


import org.joda.time.DateTime;
import org.mobicents.servlet.sip.restcomm.annotations.concurrency.Immutable;
import org.restcomm.sbc.servlet.sip.SBCMonitorServlet.Action;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    22 jul. 2016 16:40:32
 * @class   BanList.java
 *
 */
@Immutable
public class BanList {
    private final DateTime dateCreated;
    private final DateTime dateExpires;
    private final String ipAddress;
    private final Sid accountSid;
    private final Reason reason;
    private final Action action;
    
   

    public BanList(final DateTime dateCreated, final DateTime dateExpires, final String ipAddress,
            final Sid accountSid, final Reason reason, final Action action) {
        super();
        this.dateCreated = dateCreated;
        this.dateExpires = dateExpires;
        this.ipAddress = ipAddress;
        this.accountSid = accountSid;
        this.reason = reason;
        this.action = action;
       
    }

    public static Builder builder() {
        return new Builder();
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
    
    public Action getAction() {
    	return action;
    }

    
    public BanList setIpAddress(final String ipAddress) {
        return new BanList(DateTime.now(), DateTime.now().plusDays(1), ipAddress, accountSid, reason, action);
    } 

    public BanList setReason(final Reason reason) {
        return new BanList(DateTime.now(), DateTime.now().plusDays(1), ipAddress, accountSid, reason, action);
    }
    
    public BanList setAction(final Action action) {
        return new BanList(DateTime.now(), DateTime.now().plusDays(1), ipAddress, accountSid, reason, action);
    }

    public enum Type {
        WHITE("White"), BLACK("Black");

        private final String text;

        private Type(final String text) {
            this.text = text;
        }

        public static Type getValueOf(final String text) {
            Type[] values = values();
            for (final Type value : values) {
                if (value.text.equals(text)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(text + " is not a valid type.");
        }

        @Override
        public String toString() {
            return text;
        }
    };
   
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
        private String ipAddress;
        private Sid accountSid;
        private Reason reason;
        private Action action;
        
        private Builder() {
            super();
        }

        public BanList build() {
            final DateTime now = DateTime.now();
            return new BanList(now, now.plusDays(1), ipAddress, accountSid, reason, action);
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
		
		public void setAction(final Action action) {
			this.action = action;
		}

        
    }
}
