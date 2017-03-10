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


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

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
	private static int mediaStartPort;
	private static int mediaEndPort;
	private static boolean mediaDecryptionEnabled;
	private static String ipOfDomain;
	private static String apiVersion;
	private static int rtpLog;
	private static List<String> localNetworks;
	private static String home;
	
	private static transient Logger LOG = Logger.getLogger(ConfigurationCache.class);
	
	
	@SuppressWarnings("unchecked")
	private ConfigurationCache(SipFactory factory, Configuration configuration) {
		sipFactory=factory;
		home=configuration.getString				("runtime-settings.home-directory");
		apiVersion=configuration.getString			("runtime-settings.api-version");	
		domain=configuration.getString				("runtime-settings.domain");	
	    targetHost=configuration.getString			("runtime-settings.routing-policy.militarized-zone-target.ip-address");	
	    targetHAHost=configuration.getString		("runtime-settings.routing-policy.militarized-zone-target.failover-ip-address");	
		regThrottleEnabled=configuration.getBoolean ("registrar-throttle.enable");  
        regThrottleMZTTL=configuration.getInt		("registrar-throttle.force-mz-expiration");
        regThrottleUATTL=configuration.getInt       ("registrar-throttle.force-ua-expiration");
        mediaStartPort=configuration.getInt			("media-proxy.start-port");
        mediaEndPort=configuration.getInt			("media-proxy.end-port");
        rtpLog=configuration.getInt					("media-proxy.rtp-log");
        mediaDecryptionEnabled=configuration.getBoolean	
        											("media-proxy.security-policy.encryption-handle");
        
        ipOfDomain="";
        localNetworks=configuration.getList			("runtime-settings.nat-helper.local-networks.local-address");
		
		try {
			ipOfDomain=InetAddress.getByName(domain).getHostAddress();
		} catch (UnknownHostException e) {
			LOG.fatal("Cannot resolve IP Address of "+domain);
			System.exit(100);
		}
		

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


	public static int getMediaStartPort() {
		return mediaStartPort;
	}


	public static int getMediaEndPort() {
		return mediaEndPort;
	}


	public static String getIpOfDomain() {
		return ipOfDomain;
	}


	public static String getApiVersion() {
		return apiVersion;
	}


	public static List<String> getLocalNetworks() {
		return localNetworks;
	}


	public static boolean isMediaDecryptionEnabled() {
		return mediaDecryptionEnabled;
	}

	public static int getRtpCountLog() {
		return rtpLog;
	}


	public static String getHome() {
		return home;
	}



}
