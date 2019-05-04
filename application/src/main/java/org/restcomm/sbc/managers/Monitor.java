package org.restcomm.sbc.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.sip.SipFactory;
import javax.swing.event.EventListenerList;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.bo.BanList;
import org.restcomm.sbc.bo.Connector;
import org.restcomm.sbc.bo.BanList.Reason;
import org.restcomm.sbc.call.Call;
import org.restcomm.sbc.call.CallManager;
import org.restcomm.sbc.bo.Sid;
import org.restcomm.sbc.bo.Sid.Type;
import org.restcomm.sbc.bo.Statistics;
import org.restcomm.sbc.bo.shiro.ShiroResources;
import org.restcomm.sbc.dao.BlackListDao;
import org.restcomm.sbc.dao.CallDetailRecordsDao;
import org.restcomm.sbc.dao.ConnectorsDao;
import org.restcomm.sbc.dao.DaoManager;
import org.restcomm.sbc.dao.StatisticsDao;
import org.restcomm.sbc.dao.WhiteListDao;
import org.restcomm.sbc.managers.controller.ManagementProvider;
import org.restcomm.sbc.managers.controller.ManagementProviderFactory;
import org.restcomm.sbc.managers.MonitoringServiceResponse;
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
	private ManagementProvider jmxManager;
	private CallManager callManager;
	private ThreatManager threatManager;
	private ScheduledExecutorService scheduledExecutorService;
	private static boolean init=false;
	private static boolean reloaded=false;
	private int lastLiveCallMetric = 0;
	private double callRate = 0;
	
	private Monitor() {
		threatManager=ThreatManager.getThreatManager();
		cache = SuspectActivityCache.getCache(CACHE_MAX_ITEMS, CACHE_ITEM_TTL);
		daoManager = (DaoManager) ShiroResources.getInstance().get(DaoManager.class);
		callManager = (CallManager) ShiroResources.getInstance().get(CallManager.class);
		try {
			jmxManager=ManagementProviderFactory.getDefaultProvider();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			LOG.error("ManagementProvider Error", e);
		}
		
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
			BanList entry=new BanList(DateTime.now(), DateTime.now().plusHours(24), threat.getHost(), Sid.generate(Type.ACCOUNT), Reason.THREAT, BanList.Action.APPLY);
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
						
						entry=entry.setAction(BanList.Action.NONE);
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
						entry=entry.setAction(BanList.Action.NONE);
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
		MonitoringServiceResponse calls = callManager.getMonitoringService().getLiveCalls();
		Integer lCalls = calls.getCountersMap().get("LiveCalls");
		callRate=(double)(lCalls-this.lastLiveCallMetric)/this.LOOP_INTERVAL;
		this.lastLiveCallMetric=lCalls;
		return lCalls;
	          
	}
	
	public double getCallRatePerSecond() {
		 return this.callRate;
	          
	}
	
	public int getCallRejectedCount() {
		MonitoringServiceResponse calls = callManager.getMonitoringService().getLiveCalls();
		return calls.getCountersMap().get("FailedCalls");     
	}
	
	public void start(SipFactory sipFactory, Configuration configuration) {
		
		
		scheduledExecutorService =
		        Executors.newSingleThreadScheduledExecutor();
		
		    scheduledExecutorService.scheduleWithFixedDelay(new Task(sipFactory, configuration),
		    0,
		    LOOP_INTERVAL,
		    TimeUnit.SECONDS);
		    
	}
	
	public void stop() throws IOException {		
		scheduledExecutorService.shutdown();
		jmxManager.close();
	}
	
	private void reload() {
		jmxManager.reload();
	}
	
	private void bindConnectors()  {
    	
    	boolean status;
    	DaoManager daoManager = (DaoManager) ShiroResources.getInstance().get(DaoManager.class);
		ManagementProvider jmxManager = null;
		try {
			jmxManager =ManagementProviderFactory.getDefaultProvider();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			LOG.error("Management Error", e);
		}
    	ConnectorsDao dao=daoManager.getConnectorsDao();
    	for(Connector connector:dao.getConnectors()) {
    		String npoint=connector.getPoint();
    		String ipAddress=NetworkManager.getNetworkPoint(npoint).getAddress().getHostAddress();
    		if(connector.getState()==Connector.State.UP) {
	    		try {
					status=jmxManager.addSipConnector(ipAddress, connector.getPort(), connector.getTransport().toString(), npoint);
				} catch (IOException e) {
					LOG.error("Cannot add SIP Connector "+e.getMessage());
					continue;
				}
	    		if(status) {
		    		if(LOG.isDebugEnabled()) {
		    			LOG.debug("Binding Connector on "+npoint+":"+ipAddress+":"+connector.getPort()+"/"+connector.getTransport().toString());
		    		}
	    		}	
	    		else {
		    			LOG.error("CANNOT Bind Connector on "+npoint+":"+ipAddress+":"+connector.getPort()+"/"+connector.getTransport().toString());
		    		
	    		}
    		}
    	}
    	
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
		private Configuration configuration;
		private SipFactory sipFactory;

		public Task(SipFactory sipFactory, Configuration configuration) {
			this.sipFactory = sipFactory;
			this.configuration = configuration;
		}
		
		@Override
		public void run() {
			
			//SipApplicationSession aSession = sipFactory.createApplicationSession();
			//sipManager = ((MobicentsSipApplicationSessionFacade) aSession).getSipContext().getSipManager();	
			//System.out.println(DateTime.now()+" Monitor Thread tick pass");
			if(LOG.isInfoEnabled()) {
				LOG.info("Monitor Thread tick pass");
			}
			/*
			if(init && !reloaded) {
				reload();
				reloaded = true;
			}*/
			if(!init && !reloaded) {
				try {
					bindConnectors();
					RouteManager.getRouteManager(); // init routes
				} catch (Exception e) {
					LOG.error("Cannot bind connectors", e);
				}
				ConfigurationCache.build(sipFactory, configuration);
				init = true;
				return;
			}
			
			try {
				synchronizeCDR();
				applyThreatsToBlackList();
				applyBanningRules();			
				//writeStats();
				
			} catch (Exception e) {
				LOG.error("OUCH!",e);
				
			}
			
		}
		
	}
	
    
   
	
}
