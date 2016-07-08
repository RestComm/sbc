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
package org.restcomm.sbc.notification.impl;
 
import java.util.concurrent.TimeUnit;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.restcomm.chain.processor.impl.MutableMessage;
import org.restcomm.sbc.notification.SuspectActivityElectable;

 

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    27/6/2016 10:35:36
 * @class   SuspectActivityCache.java
 *
 */
public class SuspectActivityCache<K, T>  implements MutableMessage  {
 
    private boolean linked=true;
    private int ttl;
    private Cache<Object, Object> cache;
    private static SuspectActivityCache<String, SuspectActivityElectable> scache;
    
    private SuspectActivityCache(int size, int ttl) {
        this.ttl=ttl;
    	cache = new DefaultCacheManager().getCache("suspectactivity");
    	
    }
    
    public static SuspectActivityCache<String, SuspectActivityElectable> getCache(int size, int ttl) {
    	if(scache==null) {
    		scache=new SuspectActivityCache<String, SuspectActivityElectable>(size, ttl);
    	}
    	return scache;
    	
    }

    public void update(String key, SuspectActivityElectable value) {
    	cache.put(key, value, ttl, TimeUnit.SECONDS);
    }
 
 
	public SuspectActivityElectable get(String key) {
    	return (SuspectActivityElectable) cache.get(key);
        
    }
 
    public void remove(String key) {
    	cache.remove(key);
    }
 
    public int size() {
    	return cache.size();
        
    }
 
    public void cleanup() {
    	cache.clear();
 
    }


	@Override
	public Object getProperty(Object property) {
		return cache.get((String) property);
	}


	@Override
	public void setProperty(Object property, Object value) {
		this.update((String)property, (SuspectActivityElectable)value);
		
	}


	@Override
	public void unlink() {
		linked=false;
		
	}


	@Override
	public boolean isLinked() {
		return linked;
	}    
	
}