package org.restcomm.sbc.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.restcomm.sbc.bo.BanList;
import org.restcomm.sbc.bo.BanList.Reason;
import org.restcomm.sbc.call.Call;
import org.restcomm.sbc.call.CallManager;
import org.restcomm.sbc.bo.Sid;
import org.restcomm.sbc.bo.Sid.Type;
import org.restcomm.sbc.bo.Statistics;
import org.restcomm.sbc.bo.shiro.ShiroResources;
import org.restcomm.sbc.dao.BlackListDao;
import org.restcomm.sbc.dao.CallDetailRecordsDao;
import org.restcomm.sbc.dao.DaoManager;
import org.restcomm.sbc.dao.StatisticsDao;
import org.restcomm.sbc.dao.WhiteListDao;
import org.restcomm.sbc.managers.jmx.JMXProvider;
import org.restcomm.sbc.managers.jmx.JMXProviderFactory;
import org.restcomm.sbc.notification.AlertListener;
import org.restcomm.sbc.notification.NotificationListener;
import org.restcomm.sbc.notification.SuspectActivityElectable;
import org.restcomm.sbc.notification.impl.SuspectActivityCache;
import org.restcomm.sbc.threat.Threat;




public class Monitor {
	
	private static Monitor monitor;

	private static transient Logger LOG = Logger.getLogger(Monitor.class);
	
	
	private EventListenerList listenerList = new EventListenerList();
	
	private final int CACHE_MAX_ITEMS      	= 1024;
	private final int CACHE_ITEM_TTL 		= 60; 	// segs
	private final long LOOP_INTERVAL		= 60L;
	
	
	@SuppressWarnings("unused")
	private SuspectActivityCache<String, SuspectActivityElectable> cache;
	private DaoManager daoManager;
	private JMXProvider jmxManager;
	private CallManager callManager;
	private ThreatManager threatManager;
	private ScheduledExecutorService scheduledExecutorService;
	
	private Monitor() {
		threatManager=ThreatManager.getThreatManager();
		cache = SuspectActivityCache.getCache(CACHE_MAX_ITEMS, CACHE_ITEM_TTL);
		daoManager = (DaoManager) ShiroResources.getInstance().get(DaoManager.class);
		try {
			jmxManager=JMXProviderFactory.getJMXProvider();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			LOG.error("JMX Error", e);
		}
		callManager=CallManager.getCallManager();
		Runtime.getRuntime().addShutdownHook(new ProcessorHook());
	}
	
	
	public static Monitor getMonitor() {
		if(monitor==null)
			monitor=new Monitor();
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
	
	private synchronized void  applyThreatsToBlackList() {
		BlackListDao blackListDao = daoManager.getBlackListDao();
		
		for(Threat threat:threatManager.getThreats()) {
			BanList entry=new BanList(DateTime.now(), DateTime.now().plusHours(24), threat.getHost(), Sid.generate(Type.ACCOUNT), Reason.THREAT, Monitor.Action.APPLY);
			blackListDao.addBanList(entry);
		}
	}
		
	
	private synchronized void  applyBanningRules() {
		BlackListDao blackListDao = daoManager.getBlackListDao();
		List<BanList> entries = blackListDao.getBanLists();
		int status=0;
		
		for(BanList entry:entries) {
			switch(entry.getAction()) {
				case APPLY:
					status=ScriptDelegationService.runBanScript(entry.getIpAddress());
					if(status==0) {
						
						entry=entry.setAction(Action.NONE);
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
						entry=entry.setAction(Action.NONE);
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
	
	private synchronized void synchronizeCDR() {
		CallDetailRecordsDao cdrDao = daoManager.getCallDetailRecordsDao();
		Collection<Call> entries = callManager.getCalls();
		ArrayList<String> removed=new ArrayList<String>();
		
		for(Call call:entries) {
			switch(call.getStatus()) {
				case FAILED:
				case COMPLETED:
					cdrDao.addCallDetailRecord(call.getCdr());
					removed.add(call.getSessionId());
					break;
				default:
					break;		
			}
		}	
		for(String id:removed) {
			callManager.remove(id);	
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
		return callManager.getCalls().size();
	    // return sipManager.getActiveSipApplicationSessions();        
	}
	
	public double getCallRatePerSecond() {
		 return 0.0;
	     //return sipManager.getNumberOfSipApplicationSessionCreationPerSecond();        
	}
	
	public int getCallRejectedCount() {
		return 0;
	    // return sipManager.getRejectedSipApplicationSessions();        
	}
	
	public void start() {
		
		scheduledExecutorService =
		        Executors.newSingleThreadScheduledExecutor();

		    scheduledExecutorService.scheduleWithFixedDelay(new Task(),
		    LOOP_INTERVAL,
		    LOOP_INTERVAL,
		    TimeUnit.SECONDS);

	}
	
	public void stop() throws IOException {		
		scheduledExecutorService.shutdown();
		jmxManager.close();
	}
	
	class ProcessorHook extends Thread {
		 
	    @Override
	    public void run(){
	        try {
				Monitor.this.stop();
			} catch (IOException e) {
				LOG.error("Cannot stop JMX Provider");
			}
	        
	         
	    }
	}
	
	class Task implements Runnable {
		
		@Override
		public void run() {
			//SipApplicationSession aSession = sipFactory.createApplicationSession();
			//sipManager = ((MobicentsSipApplicationSessionFacade) aSession).getSipContext().getSipManager();	
			//System.out.println(DateTime.now()+" Monitor Thread tick pass");
			if(LOG.isInfoEnabled()) {
				LOG.info("Monitor Thread tick pass");
			}
			try {
				
				synchronizeCDR();
				applyThreatsToBlackList();
				applyBanningRules();
				
				writeStats();
				
			} catch (Exception e) {
				LOG.error("OUCH!",e);
				
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
    
    public static void main(String argv[]) {
    	Monitor monitor=Monitor.getMonitor();
    	monitor.start();
    }



}
