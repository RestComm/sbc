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
 */

package org.restcomm.sbc.bo;


import org.joda.time.DateTime;

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    16/6/2016 9:32:13
 * @class   Location.java
 *
 */
public class Location {
	
	private String user;
	private String host;
	private int port;
	private String transport;
	private String userAgent;
	
	private DateTime expires;
	
	public Location(String user, String host, int port, String transport) {
		this(user);
		this.host=host;
		this.port=(port<0?5060:port);
		this.transport=transport.toUpperCase();
		
	}
	
	public Location(String user) {
		this.user=user;
		
	}
	
	public String getUser() {
		return user;
	}
	
	public String getUserAgent() {
		return userAgent;
	}
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	
	public String getTransport() {
		return transport;	
	}
	
	public boolean isExpired() {
		return (getExpires().isAfterNow())?true:false;
	}
	
	
	public long getMiliSecondsToExpiration() {
		return getExpires().minus(System.currentTimeMillis()).getMillis();
	}
	
	public String toString() {
		return "<Location> user:"+user+" expires on MZ "+getMiliSecondsToExpiration()+" ms host:"+getHost()+":"+getPort()+" transport:"+getTransport()+" User-Agent:"+userAgent;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public DateTime getExpires() {
		return expires;
	}
	public void setExpires(DateTime expireTimestamp) {
		this.expires = expireTimestamp;
	}
	
	public void setExpirationTimeInSeconds(int expires) {	
		setExpires(DateTime.now().plus(expires*1000));	
	}
	
	
	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}
	
	@Override
	public boolean equals(Object location) {
		Location otherLocation=(Location) location;
		if (otherLocation.host.equals(host) && otherLocation.user.equals(user)) {
			return true;
		}
		return false;
		
	}

}
