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
package org.restcomm.sbc;

import javax.servlet.sip.SipFactory;

import org.apache.commons.configuration.Configuration;

public class ConfigurationCache {
	
	
	private static String mzIface;
	private static String mzIPAddress;
	private static String mzTransport;
	private static int mzPort;
	private static String dmzIface;
	private static String dmzIPAddress;
	private static String dmzTransport;
	private static int dmzPort;
	private static String routeMZIPAddress;
	private static String routeMZTransport;
	private static int routeMZPort;
	private static SipFactory sipFactory;
	private static boolean regThrottleEnabled;
	private static int regThrottleMinRegistartTTL;
	private static int regThrottleMaxUATTL;
	

	private ConfigurationCache(SipFactory factory, Configuration configuration) {
		sipFactory=factory;
		mzIface    =configuration.getString("runtime-settings.militarized-zone.iface-name");
	    mzIPAddress=configuration.getString("runtime-settings.militarized-zone.ip-address");
	    mzTransport=configuration.getString("runtime-settings.militarized-zone.transport");
	    mzPort     =configuration.getInt   ("runtime-settings.militarized-zone.port");
	    
	    dmzIface    =configuration.getString("runtime-settings.de-militarized-zone.iface-name");
	    dmzIPAddress=configuration.getString("runtime-settings.de-militarized-zone.ip-address");
	    dmzTransport=configuration.getString("runtime-settings.de-militarized-zone.transport");
	    dmzPort     =configuration.getInt   ("runtime-settings.de-militarized-zone.port");
	  
	    routeMZIPAddress=configuration.getString("runtime-settings.routing-policy.militarized-zone-target.ip-address");
		routeMZTransport=configuration.getString("runtime-settings.routing-policy.militarized-zone-target.transport");
		routeMZPort     =configuration.getInt   ("runtime-settings.routing-policy.militarized-zone-target.port");
		
		regThrottleEnabled=configuration.getBoolean    ("registrar-throttle.enable");  
        regThrottleMinRegistartTTL=configuration.getInt("registrar-throttle.min-registrar-expiration");
        regThrottleMaxUATTL=configuration.getInt       ("registrar-throttle.max-ua-expiration");
        

	}

	public static void build(SipFactory factory, Configuration configuration){
		new ConfigurationCache(factory, configuration);
		
	}
	public static String getMzIface() {
		return mzIface;
	}


	public static String getMzIPAddress() {
		return mzIPAddress;
	}


	public static String getMzTransport() {
		return mzTransport;
	}


	public static int getMzPort() {
		return mzPort;
	}


	public static String getDmzIface() {
		return dmzIface;
	}


	public static String getDmzIPAddress() {
		return dmzIPAddress;
	}


	public static String getDmzTransport() {
		return dmzTransport;
	}


	public static int getDmzPort() {
		return dmzPort;
	}


	public static String getRouteMZIPAddress() {
		return routeMZIPAddress;
	}


	public static String getRouteMZTransport() {
		return routeMZTransport;
	}


	public static int getRouteMZPort() {
		return routeMZPort;
	}


	public static SipFactory getSipFactory() {
		return sipFactory;
	}

	public static boolean isRegThrottleEnabled() {
		return regThrottleEnabled;
	}

	public static int getRegThrottleMinRegistartTTL() {
		return regThrottleMinRegistartTTL;
	}

	public static int getRegThrottleMaxUATTL() {
		return regThrottleMaxUATTL;
	}
	

}
