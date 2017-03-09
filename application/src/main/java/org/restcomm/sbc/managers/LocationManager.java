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
 */

package org.restcomm.sbc.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.restcomm.sbc.bo.Location;
import org.restcomm.sbc.bo.LocationFilter;
import org.restcomm.sbc.bo.LocationNotFoundException;
import org.restcomm.sbc.bo.Sid;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    1 sept. 2016 20:08:51
 * @class   LocationManager.java
 *
 */
public class LocationManager  {
	
	private Cache<String, Location> registers;
	private static LocationManager locationManager;
	private static final Logger LOG = Logger.getLogger(LocationManager.class);

	
	private LocationManager() {
		registers = CacheManager.getCacheManager().getCache("location");
		registers.start();
		
	}
	
	public static LocationManager getLocationManager() {
		if(locationManager==null) {
			locationManager=new LocationManager();
		}
		
		return locationManager;
	}
	
	public Location create(String user, String domain, String host, int port, String transport, Sid connector) {
		return new Location(user, domain, host, port, transport, connector);
	}
	
	public void register(Location location, String userAgent, String callID, int cSeq, int ttl) {
	
		location.setUserAgent(userAgent);
		location.setCallID(callID);
		location.setcSeq(cSeq);
		location.setExpirationTimeInSeconds(ttl);
		registers.put(key(location.getUser(),location.getDomain()), location, ttl, TimeUnit.SECONDS);
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("registers.put "+key(location.getUser(),location.getDomain())+":"+ttl+" secs.");
		}
		
	}
	
	public Location unregister(String user, String domain) {
		return registers.remove(key(user, domain));
	}
	
	public Location unregister(String aor) {
		return registers.remove(aor);
	}
	
	public Location getLocation(String user, String domain) throws LocationNotFoundException {
		Location location=registers.get(key(user, domain));
		if(location == null)
			throw new LocationNotFoundException(key(user, domain));
		
		return location;
	}
	
	public Location getLocation(String aor) throws LocationNotFoundException {
		Location location=registers.get(aor);
		if(location == null)
			throw new LocationNotFoundException(aor);
		
		return location;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Location> getLocations() {	
		ArrayList al=new ArrayList(registers.values());
		//Collections.sort(al);
		return al;
	}
	
	
	public boolean isExpired(String user, String domain) throws LocationNotFoundException {
		Location location=getLocation(user, domain);
		
		boolean result=(location!=null && !location.isExpired())?true:false;
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("isExpired "+result+":"+location);
		}
		return result;
		
		
	}
	
	public boolean exists(String user, String domain) {
		try {
			return getLocation(user, domain)!=null;
		} catch (LocationNotFoundException e) {
			return false;
		}
	}
	
	public boolean match(String user, String domain) throws LocationNotFoundException {
		if(LOG.isDebugEnabled()) {
			LOG.debug("match "+user);
		}
		Location location=getLocation(user, domain);
		if(location!=null) {
			return true;
		}
		return false;
		
	}

	

	public int getTotalLocations(LocationFilter filterForTotal) {
		int counter = 0;
		
		for(Object l:registers.values()) {
			Location location=(Location)l;
			String fHost=filterForTotal.getHost();
			String fUser=filterForTotal.getUser();
			String fTransport=filterForTotal.getTransport();
			
			if(fHost!=null) {
				if(!location.getHost().startsWith(fHost.replace("%", "")))
					continue;	
			}
			if(fUser!=null) {
				if(!location.getUser().startsWith(fUser.replace("%", ""))) 
					continue;	
			}
			if(fTransport!=null) {
				if(!location.getTransport().startsWith(fTransport))
					continue;	
			}
			counter++;	
		}
		return counter;
	}

	public List<Location> getLocations(LocationFilter filter) {	
		int offsetCounter=0;
		int limitCounter=0;
		int limit=filter.getLimit();
		int offset=filter.getOffset();
		
		ArrayList<Location> locations=new ArrayList<Location>();
		
		for(Object l:registers.values()) {
			Location location=(Location)l;
			String fHost=filter.getHost();
			String fUser=filter.getUser();
			String fTransport=filter.getTransport();
			
			if(fHost!=null) {
				if(!location.getHost().startsWith(fHost.replace("%", "")))
					continue;	
			}
			if(fUser!=null) {
				if(!location.getUser().startsWith(fUser.replace("%", ""))) 
					continue;	
			}
			if(fTransport!=null) {
				if(!location.getTransport().startsWith(fTransport))
					continue;	
			}
			offsetCounter++;
			
			if(offsetCounter>offset && limitCounter<limit) {
				locations.add(location);
				limitCounter++;
			}
				
		}
		return locations;
				
	}
	
	private String key(String user, String domain) {
		return user+"@"+domain;
	}
	
}
