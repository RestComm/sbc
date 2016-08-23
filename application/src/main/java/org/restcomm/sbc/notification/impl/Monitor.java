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

import java.util.List;

import javax.servlet.ServletContext;
import javax.swing.event.EventListenerList;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.restcomm.sbc.bo.BanList;
import org.restcomm.sbc.dao.BlackListDao;
import org.restcomm.sbc.dao.DaoManager;
import org.restcomm.sbc.dao.WhiteListDao;
import org.restcomm.sbc.managers.ScriptDelegationService;
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
	@Context
	protected ServletContext context;
	
	private static Monitor monitor;
	
	private static transient Logger LOG = Logger.getLogger(Monitor.class);	
	private EventListenerList listenerList = new EventListenerList();
	
	private int CACHE_MAX_ITEMS      = 1024;
	private int CACHE_ITEM_TTL 		 = 60; 	// segs
	
	private SuspectActivityCache<String, SuspectActivityElectable> cache;
	private DaoManager daoManager;
	
	private Monitor() {
		cache = SuspectActivityCache.getCache(CACHE_MAX_ITEMS, CACHE_ITEM_TTL);
		daoManager = (DaoManager) context.getAttribute(DaoManager.class.getName());     
		
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
	protected void fireNotificationEvent(Object source, String message) {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==NotificationListener.class) {             
	             ((NotificationListener)listeners[i+1]).onActionRequired(source, message);
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
	
	private void applyBanningRules() {
		BlackListDao blackListDao = daoManager.getBlackListDao();
		List<BanList> entries = blackListDao.getBanLists();
		int status=0;
		
		for(BanList entry:entries) {
			switch(entry.getAction()) {
				case APPLY:
					status=ScriptDelegationService.runBanScript(entry.getIpAddress());
					if(status==0) {
						entry.setAction(Action.NONE);
						blackListDao.updateBanList(entry);		
					}
					else {
						LOG.error("Cannot execute BlackList adding!, operation rollback");
						this.fireNotificationEvent(entry, "Cannot execute BlackList adding!, operation rollback");
					}
					break;
				case REMOVE:
					status=ScriptDelegationService.runUnBanScript(entry.getIpAddress());
					if(status==0) {
						blackListDao.removeBanList(entry);	
					}
					else {
						LOG.error("Cannot execute BlackList removing!, operation rollback");
						this.fireNotificationEvent(entry, "Cannot execute BlackList removing!, operation rollback");
					}
					break;
				case NONE:
			}
		}
		
		WhiteListDao whiteListDao = daoManager.getWhiteListDao();
		entries = whiteListDao.getBanLists();
		status=0;
		

		for(BanList entry:entries) {
			switch(entry.getAction()) {
				case APPLY:
					status=ScriptDelegationService.runAllowScript(entry.getIpAddress());
					if(status==0) {
						entry.setAction(Action.NONE);
						whiteListDao.updateBanList(entry);		
					}
					else {
						LOG.error("Cannot execute WhiteList adding!, operation rollback");
						this.fireNotificationEvent(entry, "Cannot execute WhiteList adding!, operation rollback");
					}
					break;
				case REMOVE:
					status=ScriptDelegationService.runDisallowScript(entry.getIpAddress());
					if(status==0) {
						whiteListDao.removeBanList(entry);	
					}
					else {
						LOG.error("Cannot execute WhiteList removing!, operation rollback");
						this.fireNotificationEvent(entry, "Cannot execute WhiteList adding!, operation rollback");
					}
					break;
				case NONE:
			}
		}
		
	}

	@Override
	public void run() {
		// Traverses SupectActivityCache in a regular basis
		while(true) {
			try {
				applyBanningRules();
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				LOG.error("Thread interrupt",e);
			}
			
		}
		
	}
	
	public enum Action {
        APPLY("Apply"),
		REMOVE("Remove"),
		NONE("None");

        private final String text;

        private Action(final String text) {
            this.text = text;
        }

        public static Action getValueOf(final String text) {
            Action[] values = values();
            for (final Action value : values) {
                if (value.text.equals(text)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(text + " is not a valid action.");
        }

        @Override
        public String toString() {
            return text;
        }
    };

}
