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


import org.mobicents.servlet.sip.restcomm.annotations.concurrency.Immutable;


/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */

@Immutable
public class LocationFilter {


	private String user;
	private String host;
	private String userAgent;
	private Integer limit;
	private Integer offset;
	private String instanceid;
	

	

    public LocationFilter(String user, String host,  String userAgent, Integer limit, Integer offset) throws ParseException {
        this(user, host,  userAgent, limit, offset,null);
    }

    public LocationFilter(String user, String host,  String userAgent, Integer limit, Integer offset, String instanceId) throws ParseException {
       
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

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}


	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}
}
