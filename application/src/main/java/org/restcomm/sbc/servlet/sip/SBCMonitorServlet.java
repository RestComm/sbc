package org.restcomm.sbc.servlet.sip;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.mobicents.servlet.sip.core.SipManager;
import org.mobicents.servlet.sip.message.MobicentsSipApplicationSessionFacade;
import org.restcomm.sbc.bo.BanList;
import org.restcomm.sbc.bo.Sid;
import org.restcomm.sbc.bo.Statistics;
import org.restcomm.sbc.bo.shiro.ShiroResources;
import org.restcomm.sbc.dao.BlackListDao;
import org.restcomm.sbc.dao.DaoManager;
import org.restcomm.sbc.dao.StatisticsDao;
import org.restcomm.sbc.dao.WhiteListDao;
import org.restcomm.sbc.managers.JMXManager;
import org.restcomm.sbc.managers.ScriptDelegationService;
import org.restcomm.sbc.notification.AlertListener;
import org.restcomm.sbc.notification.NotificationListener;
import org.restcomm.sbc.notification.SuspectActivityElectable;
import org.restcomm.sbc.notification.impl.SuspectActivityCache;


public class SBCMonitorServlet extends SipServlet {
	
	private static SBCMonitorServlet monitor;

	private static transient Logger LOG = Logger.getLogger(SBCMonitorServlet.class);
	private static final long serialVersionUID = -9170263176960604645L;
	private SipFactory sipFactory;
	private SipManager sipManager;	
	
	private EventListenerList listenerList = new EventListenerList();
	
	private final int CACHE_MAX_ITEMS      	= 1024;
	private final int CACHE_ITEM_TTL 		= 60; 	// segs
	private final int LOOP_INTERVAL			= 60000;
	
	
	private SuspectActivityCache<String, SuspectActivityElectable> cache;
	private DaoManager daoManager;
	private JMXManager jmxManager;
	
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		LOG.info("Monitor sip servlet has been started");
		
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> init()");
	    }
		
		sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		
		
		cache = SuspectActivityCache.getCache(CACHE_MAX_ITEMS, CACHE_ITEM_TTL);
		daoManager = (DaoManager) ShiroResources.getInstance().get(DaoManager.class);
		try {
			jmxManager=JMXManager.getInstance();
		} catch (MalformedObjectNameException | InstanceNotFoundException | IntrospectionException | ReflectionException
				| IOException e) {
			LOG.error("JMX Error", e);
		}
		
		monitor=this;
		execute();
	}
	
	public static SBCMonitorServlet getMonitor() {
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
	
	private void writeStats() {
		StatisticsDao statsDao = daoManager.getStatisticsDao();
		
		Statistics record=new Statistics(
				
				Sid.generate(Sid.Type.RANDOM),
				jmxManager.getMemoryUsage(),
				jmxManager.getCPULoadAverage(),
				getLiveCallCount(),
				getCallRatePerSecond(),
				getCallRejectedCount(),
				0,
				DateTime.now()
				);
		statsDao.addRecord(record);
		
	}
	public int getLiveCallCount() {	 
	     return sipManager.getActiveSipApplicationSessions();        
	}
	
	public double getCallRatePerSecond() {	 
	     return sipManager.getNumberOfSipApplicationSessionCreationPerSecond();        
	}
	
	public int getCallRejectedCount() {	 
	     return sipManager.getRejectedSipApplicationSessions();        
	}
	
	private void execute() {
		
		ScheduledExecutorService scheduledExecutorService =
		        Executors.newScheduledThreadPool(5);

		ScheduledFuture scheduledFuture =
		    scheduledExecutorService.scheduleWithFixedDelay(new Task() {
		        public Object call() throws Exception {
		        	if(LOG.isInfoEnabled()) {
						LOG.info("Monitor Thread tick pass");
					}
		            return "Called!";
		        }
		    },
		    60L,
		    60L,
		    TimeUnit.SECONDS);

	}

	
	class Task implements Runnable {
		
		@Override
		public void run() {
			SipApplicationSession aSession = sipFactory.createApplicationSession();
			sipManager = ((MobicentsSipApplicationSessionFacade) aSession).getSipContext().getSipManager();	
			if(LOG.isInfoEnabled()) {
				LOG.info("Monitor Thread tick pass");
			}
			applyBanningRules();
			writeStats();
			
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
