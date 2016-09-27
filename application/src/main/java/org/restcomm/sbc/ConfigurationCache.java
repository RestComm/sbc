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
import org.apache.log4j.Logger;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    30 ago. 2016 10:32:30
 * @class   ConfigurationCache.java
 *
 */
public class ConfigurationCache {
	
	private static String domain;
	private static String targetHost;
	private static String targetHAHost;
	private static SipFactory sipFactory;
	private static boolean regThrottleEnabled;
	private static int regThrottleMZTTL;
	private static int regThrottleUATTL;

	private static transient Logger LOG = Logger.getLogger(ConfigurationCache.class);
	
	private ConfigurationCache(SipFactory factory, Configuration configuration) {
		sipFactory=factory;
		
		domain=configuration.getString		("runtime-settings.domain");	
	    targetHost=configuration.getString		("runtime-settings.routing-policy.militarized-zone-target.ip-address");	
	    targetHAHost=configuration.getString	("runtime-settings.routing-policy.militarized-zone-target.failover-ip-address");	
		regThrottleEnabled=configuration.getBoolean ("registrar-throttle.enable");  
        regThrottleMZTTL=configuration.getInt		("registrar-throttle.force-mz-expiration");
        regThrottleUATTL=configuration.getInt       ("registrar-throttle.force-ua-expiration");
        
	}
	
	
	public static void build(SipFactory factory, Configuration configuration){
		new ConfigurationCache(factory, configuration);
		
	}
	

	public static String getTargetHost() {
		return targetHost;
	}
	
	public static String getHATargetHost() {
		return targetHAHost;
	}


	public static SipFactory getSipFactory() {
		return sipFactory;
	}

	public static boolean isRegThrottleEnabled() {
		return regThrottleEnabled;
	}

	public static int getRegThrottleMZTTL() {
		return regThrottleMZTTL;
	}

	public static int getRegThrottleUATTL() {
		return regThrottleUATTL;
	}


	public static String getDomain() {
		return domain;
	}
	

}
