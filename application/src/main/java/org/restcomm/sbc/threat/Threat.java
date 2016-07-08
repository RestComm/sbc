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
 *******************************************************************************/
package org.restcomm.sbc.threat;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    14/5/2016 12:37:55
 * @class   Threat.java
 *
 */
public class Threat {
	
	public enum Type {
		DOS_ATTACK,
		DDOS_ATTACK,
		DIAL_SPOOF,
		USER_ENUM,
		SERVICE_SCAN,
		BRUTE_FORCE_CRACK_ATTEMPT,
		HEURISTIC,
		POTENTIAL
	}
	
	public static long	ACTION_IGNORE         = 00000001L;
	public static long	ACTION_BLACKLIST_HOST = 00000010L;
	public static long	ACTION_NOTIFY         = 00000100L;
	public static long	ACTION_ALERT          = 00001000L;
	public static long	ACTION_ACCOUNT        = 00010000L;
	
	
	
	protected String host;
	protected int port;
	protected Type type;
	protected String user;
	protected String userAgent;
	protected String transport;
	protected long action;
	
	
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
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
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
	public long getAction() {
		return action;
	}
	public void setAction(long action) {
		this.action = action;
	}	
	
}
