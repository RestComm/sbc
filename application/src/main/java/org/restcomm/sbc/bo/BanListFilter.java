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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.mobicents.servlet.sip.restcomm.annotations.concurrency.Immutable;




/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    22 jul. 2016 17:23:07
 * @class   BanListFilter.java
 *
 */
@Immutable
public class BanListFilter {

    
	private final String action;
	private final String color;
    private final Date dateCreated;
    private final Date dateExpires;
    private final String ipAddress;
    private final String accountSid;
    private final String reason;
    private final Integer limit;
    private final Integer offset;
    private final String instanceid;

    public BanListFilter(String color, String ipAddress, String accountSid, String dateCreated, String dateExpires, String reason, String action, Integer limit, Integer offset) throws ParseException {
        this(color, ipAddress, accountSid, dateCreated, dateExpires, reason, action, limit, offset, null);
    }

    public BanListFilter(String color, String ipAddress, String accountSid, String dateCreated, String dateExpires, String reason, String action, Integer limit, Integer offset, String instanceId) throws ParseException { 

        if (ipAddress != null)
            ipAddress = "%".concat(ipAddress);
        
        if (action != null)
            action = "%".concat(action);
        
        this.ipAddress = ipAddress;
        this.reason = reason;
        this.action = action;
        this.color = color;
        this.accountSid = accountSid;
        this.limit = limit;
        this.offset = offset;
        if (dateCreated != null) {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
            Date date = parser.parse(dateCreated);
            this.dateCreated = date;
        } else
            this.dateCreated = null;

        if (dateExpires != null) {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
            Date date = parser.parse(dateExpires);
            this.dateExpires = date;
        } else {
            this.dateExpires = null;
        }
        if (instanceId != null && !instanceId.isEmpty()) {
            this.instanceid = instanceId;
        } else {
            this.instanceid = null;
        }
    }


    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public String getInstanceid() { return instanceid; }

	public String getAction() {
		return action;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public Date getDateExpires() {
		return dateExpires;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getAccountSid() {
		return accountSid;
	}

	public String getReason() {
		return reason;
	}

	public String getColor() {
		return color;
	}
}
