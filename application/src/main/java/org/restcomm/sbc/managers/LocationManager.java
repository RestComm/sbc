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

import org.infinispan.Cache;

import org.infinispan.manager.DefaultCacheManager;
import org.restcomm.sbc.bo.Location;
import org.restcomm.sbc.bo.LocationFilter;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3/5/2016 22:47:34
 * @class   LocationHelper.java
 * @project Servlet2.5SBC
 *
 */
public class LocationManager {
	
	private Cache<Object, Object> registers;
	private static LocationManager locationManager;
	
	private LocationManager() {
		registers = new DefaultCacheManager().getCache("location");
		
	}
	
	public static LocationManager getLocationManager() {
		if(locationManager==null) {
			locationManager=new LocationManager();
		}
		locationManager.register("00", "192.168.0.2", 5060, "friendly-scanner", "udp", 30);
		return locationManager;
	}
	
	
	public Location register(String user, String host, int port, String userAgent, String transport, int ttl) {
		Location location=new Location();
		location.setHost(host);
		location.setPort(port);
		location.setUserAgent(userAgent);
		location.setTransport(transport);
		location.setUser(user);
		
		registers.put(user, location, ttl, TimeUnit.SECONDS);
		
		return location;
			
	}
	
	public Location getLocation(String user) {
		return (Location) registers.get(user);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Location> getLocations() {	
		ArrayList al=new ArrayList(registers.values());
		//Collections.sort(al);
		return al;
	}
	
	public boolean isMzAlive(String user) {
		Location location=getLocation(user);
		return (location!=null && !location.isMzExpired())?true:false;
		
	}
	
	public boolean isDmzAlive(String user) {
		Location location=getLocation(user);
		return (location!=null && !location.isDmzExpired())?true:false;
		
	}
	
	public boolean exists(String user) {
		return getLocation(user)!=null;
	}
	
	public boolean match(String user, String host, int port) {
		Location location=getLocation(user);
		if(location!=null && location.getHost().equals(host) && location.getPort()==port) {
			return true;
		}
		return false;
		
	}

	public Location unregister(String user) {	
		return (Location) registers.remove(user);	
	}
	
	public void setDmzExpirationTimeInSeconds(String user, int expires) {
		Location location=getLocation(user);
		location.setDmzExpireTimestamp((System.currentTimeMillis())+(((long)expires)*1000L));
		
	}
	
	public void setMzExpirationTimeInSeconds(String user, int expires) {
		Location location=getLocation(user);
		location.setMzExpireTimestamp((System.currentTimeMillis())+(((long)expires)*1000L));
		
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
