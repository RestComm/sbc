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

import java.util.Calendar;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3/5/2016 22:45:05
 * @class   Location.java
 * @project Servlet2.5SBC
 *
 */
public class Location {
	
	private String user;
	private String host;
	private int port;
	private String userAgent;
	private String transport;

	private long mzExpireTimestamp;
	private long dmzExpireTimestamp;
	
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
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
	public void setTransport(String transport) {
		this.transport = transport;
	}
	
	public boolean isMzExpired() {
		//System.err.println("Actual Calendar "+Calendar.getInstance().getTimeInMillis());
		return (Calendar.getInstance().getTimeInMillis())>=getMzExpireTimestamp()?true:false;
	}
	
	public long getMzMiliSecondsToExpiration() {
		return (getMzExpireTimestamp()-(Calendar.getInstance().getTimeInMillis()));
	}
	
	public boolean isDmzExpired() {
		//System.err.println("Actual Calendar "+Calendar.getInstance().getTimeInMillis());
		return (Calendar.getInstance().getTimeInMillis())>=getDmzExpireTimestamp()?true:false;
	}
	
	public long getDmzMiliSecondsToExpiration() {
		return (getDmzExpireTimestamp()-(Calendar.getInstance().getTimeInMillis()));
	}
	
	public String toString() {
		return "<Location> user:"+user+" expires on MZ/DMZ "+getMzMiliSecondsToExpiration()+"/"+getDmzMiliSecondsToExpiration()+" ms host:"+host+":"+port+" transport:"+transport+" User-Agent:"+userAgent;
	}
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public long getMzExpireTimestamp() {
		return mzExpireTimestamp;
	}
	public void setMzExpireTimestamp(long mzExpireTimestamp) {
		this.mzExpireTimestamp = mzExpireTimestamp;
	}
	public long getDmzExpireTimestamp() {
		return dmzExpireTimestamp;
	}
	public void setDmzExpireTimestamp(long dmzExpireTimestamp) {
		this.dmzExpireTimestamp = dmzExpireTimestamp;
	}
	
	public void setDmzExpirationTimeInSeconds(int expires) {	
		setDmzExpireTimestamp((Calendar.getInstance().getTimeInMillis())+(((long)expires)*1000L));	
	}
	
	public void setMzExpirationTimeInSeconds(int expires) {
		setMzExpireTimestamp((Calendar.getInstance().getTimeInMillis())+(((long)expires)*1000L));	
	}
	

}
