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
 
import java.util.ArrayList;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;
import org.restcomm.chain.processor.impl.MutableMessage;

 
/**
 * @author Crunchify.com
 */
 
public class SuspectActivityLegacyCache<K, SuspectActivityElectable>  implements MutableMessage  {
 
    private long timeToLive;
    private LRUMap suspectActivityCacheMap;
    private boolean linked=true;
    
    /**
     * 
     * @param suspectActivityTimeToLive, Expiration
     * @param suspectActivityTimerInterval, Checkpoint interval
     * @param maxItems, Maximum items in Cache
     */
    public SuspectActivityLegacyCache(long suspectActivityTimeToLive, final long suspectActivityTimerInterval, int maxItems) {
        this.timeToLive = suspectActivityTimeToLive * 1000;
 
        suspectActivityCacheMap = new LRUMap(maxItems);
 
        if (timeToLive > 0 && suspectActivityTimerInterval > 0) {
 
            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(suspectActivityTimerInterval * 1000);
                        } catch (InterruptedException ex) {
                        }
                        cleanup();
                    }
                }
            });
 
            t.setDaemon(true);
            t.start();
        }
    }
 

    public void update(K key, SuspectActivityElectable value) {
    	SuspectActivity c = (SuspectActivity) get(key);
        synchronized (suspectActivityCacheMap) {
        	if(c!=null) {
        		suspectActivityCacheMap.put(key, c);
        	}
        	else {
        		suspectActivityCacheMap.put(key, value);
        	}
        }
    }
 
    @SuppressWarnings("unchecked")
    public SuspectActivityElectable get(K key) {
        synchronized (suspectActivityCacheMap) {       
        	SuspectActivityElectable c = (SuspectActivityElectable) suspectActivityCacheMap.get(key);
        	return c;
        }
    }
 
    public void remove(K key) {
        synchronized (suspectActivityCacheMap) {
            suspectActivityCacheMap.remove(key);
        }
    }
 
    public int size() {
        synchronized (suspectActivityCacheMap) {
            return suspectActivityCacheMap.size();
        }
    }
 
    @SuppressWarnings("unchecked")
    public void cleanup() {
 
        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;
 
        synchronized (suspectActivityCacheMap) {
            MapIterator itr = suspectActivityCacheMap.mapIterator();
 
            deleteKey = new ArrayList<K>((suspectActivityCacheMap.size() / 2) + 1);
            K key = null;
            SuspectActivityElectable c = null;
 
            while (itr.hasNext()) {
                key = (K) itr.next();
                c = (SuspectActivityElectable) itr.getValue();
 
                if (c != null && (now > (timeToLive)) ) {
                    deleteKey.add(key);
                }
            }
        }
 
        for (K key : deleteKey) {
            synchronized (suspectActivityCacheMap) {
                suspectActivityCacheMap.remove(key);
            }
 
            Thread.yield();
        }
    }


	@Override
	public Object getProperty(Object property) {
		return get((K) property);
	}


	@Override
	public void setProperty(Object property, Object value) {
		this.update((K)property, (SuspectActivityElectable)value);
		
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