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
import org.infinispan.manager.DefaultCacheManager;
import org.restcomm.sbc.bo.Location;
import org.restcomm.sbc.bo.LocationFilter;


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
		registers = new DefaultCacheManager().getCache("location");
		registers.start();
		
	}
	
	public static LocationManager getLocationManager() {
		if(locationManager==null) {
			locationManager=new LocationManager();
		}
		
		return locationManager;
	}
	
	
	public void register(Location location, String userAgent, int ttl) {
	
		location.setUserAgent(userAgent);
		location.setExpirationTimeInSeconds(ttl);
		registers.put(location.getUser(), location, ttl, TimeUnit.SECONDS);
		if(LOG.isDebugEnabled()) {
			LOG.debug("registers.put "+location.getUser());
		}
		
	}
	
	public Location unregister(String user) {
		return registers.remove(user);
	}
	
	
	public Location getLocation(String user) {
		return registers.get(user);
	}
	
	public Location getLocation(String user, String host) {
		Location location=registers.get(user);
		if(location.getHost().equals(host)) {
			return location;
		}
		
		return null;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Location> getLocations() {	
		ArrayList al=new ArrayList(registers.values());
		//Collections.sort(al);
		return al;
	}
	
	
	public boolean isExpired(String user) {
		Location location=getLocation(user);
		
		boolean result=(location!=null && !location.isExpired())?true:false;
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("isExpired "+result+":"+location);
		}
		return result;
		
		
	}
	
	public boolean exists(String user) {
		return getLocation(user)!=null;
	}
	
	public boolean match(String user) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("match "+user);
		}
		Location location=getLocation(user);
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
	
	
}
