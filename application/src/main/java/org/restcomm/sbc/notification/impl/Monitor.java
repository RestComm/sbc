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

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.restcomm.sbc.notification.AlertListener;
import org.restcomm.sbc.notification.NotificationListener;
import org.restcomm.sbc.notification.SuspectActivityElectable;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    4/6/2016 13:11:49
 * @class   Monitor.java
 *
 */
public class Monitor implements Runnable {
	
	private static Monitor monitor;
	
	private static transient Logger LOG = Logger.getLogger(Monitor.class);	
	private EventListenerList listenerList = new EventListenerList();
	
	private int CACHE_MAX_ITEMS      = 1024;
	private int CACHE_ITEM_TTL 		 = 60; 	// segs
	
	private SuspectActivityCache<String, SuspectActivityElectable> cache;
	
	private Monitor() {
		cache = SuspectActivityCache.getCache(CACHE_MAX_ITEMS, CACHE_ITEM_TTL);
        
		
	}
	
	public static Monitor getInstance() {
		if(monitor==null) {
			monitor=new Monitor();
		}
		return monitor;
	}
	
	public void addNotificationListener(NotificationListener listener) {
	     listenerList.add(NotificationListener.class, listener);
	}
	
	public void addAlertListener(AlertListener listener) {
	     listenerList.add(AlertListener.class, listener);
	}
	
	
	/*
	 * void onActionRequired (Notifiable data);
	 */
	protected void fireNotificationEvent(SuspectActivityElectable sae) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==NotificationListener.class) {             
	             ((NotificationListener)listeners[i+1]).onActionRequired(sae);
	         }
	         
	     }
	 }
	
	/*
	 * void onActionRequired (Notifiable data);
	 */
	protected void fireAlertEvent(SuspectActivityElectable sae) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==AlertListener.class) {             
	             ((AlertListener)listeners[i+1]).onActionRequired(sae);
	         }
	         
	     }
	 }

	@Override
	public void run() {
		// Traverses SupectActivityCache in a regular basis
		while(true) {
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

}
