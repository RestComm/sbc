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
package org.restcomm.sbc.bo;
 
import java.util.ArrayList;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;


 
/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    21/6/2016 8:40:50
 * @class   Cache.java
 *
 */
public class Cache<K, T>   {
 
    private long timeToLive    = 20000;
    private long timerInterval = 60;
    private int  maxItems      = 1024;
    private LRUMap cacheMap;
   
    
    /**
     * 
     * @param suspectActivityTimeToLive, Expiration
     * @param suspectActivityTimerInterval, Checkpoint interval
     * @param maxItems, Maximum items in Cache
     */
    public Cache() {
    	
        cacheMap = new LRUMap(maxItems);
 
        if (timeToLive > 0 && timerInterval > 0) {
 
            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(timerInterval * 1000);
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
 

    public void put(K key, T value) {
    	T c =  get(key);
        synchronized (cacheMap) {
        	if(c!=null) {
        		cacheMap.put(key, c);
        	}
        	else {
        		cacheMap.put(key, value);
        	}
        }
    }
 
    @SuppressWarnings("unchecked")
    public T get(K key) {
        synchronized (cacheMap) {       
        	T c = (T) cacheMap.get(key);
        	return c;
        }
    }
 
    public void remove(K key) {
        synchronized (cacheMap) {
            cacheMap.remove(key);
        }
    }
 
    public int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }
 
    @SuppressWarnings("unchecked")
    public void cleanup() {
 
        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;
 
        synchronized (cacheMap) {
            MapIterator itr = cacheMap.mapIterator();
 
            deleteKey = new ArrayList<K>((cacheMap.size() / 2) + 1);
            K key = null;
            T c = null;
 
            while (itr.hasNext()) {
                key = (K) itr.next();
                c =  (T) itr.getValue();
 
                if (c != null && (now > (timeToLive)) ) {
                    deleteKey.add(key);
                }
            }
        }
 
        for (K key : deleteKey) {
            synchronized (cacheMap) {
                cacheMap.remove(key);
            }
 
            Thread.yield();
        }
    }
	
}