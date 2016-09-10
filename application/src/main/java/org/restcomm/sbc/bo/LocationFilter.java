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
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    22 jul. 2016 16:42:28
 * @class   LocationFilter.java
 *
 */
@Immutable
public class LocationFilter {

	private String user;
	private String host;
	private String domain;
	private String userAgent;
	private int port;
	private String transport;
	private Integer limit;
	private Integer offset;
	

    public LocationFilter(String user, String domain, String host, int port, String transport, String userAgent, Integer limit, Integer offset) throws ParseException {
        this(user, domain, host, port, transport, userAgent, limit, offset, null);
    }

    public LocationFilter(String user, String domain, String host, int port, String transport, String userAgent, Integer limit, Integer offset, String instanceId) throws ParseException {
        //
    	// The LIKE keyword uses '%' to match any (including 0) number of characters, and '_' to match exactly one character
        // Add here the '%' keyword so +15126002188 will be the same as 15126002188 and 6002188
        if (user != null)
            user = "%".concat(user);
        if (host != null)
            host = "%".concat(host);
        
        this.port = port;
        this.transport = transport;
        this.host = host;
        this.user = user;
        this.domain = domain;
        
        this.limit = limit;
        this.offset = offset;
        
    }

    

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }


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

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getTransport() {
		return transport;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}
