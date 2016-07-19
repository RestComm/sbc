package org.restcomm.sbc.managers;

import java.io.IOException;












import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.sip.ListeningPoint;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.listener.SipConnectorListener;


public class JMXManager implements
 NotificationListener, SipConnectorListener {
	private static transient Logger LOG = Logger.getLogger(JMXManager.class);
	
	private JMXConnector jmxc;
	private MBeanServerConnection mbsc;
	private ObjectName objectName;
	private static JMXManager jmxManager;
	
	private JMXManager() throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException {
		System.setProperty("com.sun.management.jmxremote", "");
		System.setProperty("com.sun.management.jmxremote.port", "9999");
		System.setProperty("com.sun.management.jmxremote.ssl", "false");
		System.setProperty("com.sun.management.jmxremote.authenticate", "false");
		LOG.info("\nCreate an RMI connector client and " +
				"connect it to the RMI connector server");
		StringBuffer urlString = new StringBuffer();
		urlString.append("service:jmx:rmi://localhost:");
		urlString.append(9999);
		urlString.append("/jndi/rmi://localhost:");
		urlString.append(9999);
		urlString.append("/jmxrmi");
		JMXServiceURL url = new JMXServiceURL(urlString.toString());
		jmxc = JMXConnectorFactory.connect(url, null);


		objectName = new ObjectName("Sip-Servlets:type=Service");
		LOG.info("object "+objectName);
		// Get the Platform MBean Server
		mbsc = jmxc.getMBeanServerConnection();
		mbsc.addNotificationListener(objectName, this, null, null);
		

	}
	
	public static JMXManager getInstanace() throws MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
		if(jmxManager==null){
			jmxManager=new JMXManager();
		}
		return jmxManager;
	}
	
	public boolean removeSipConnector(String ipAddress, int port, String transport) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		LOG.info("-------Removing SipConnector "+ipAddress+":"+port+"/"+transport);
        Boolean stat=(Boolean) mbsc.invoke(objectName, "removeSipConnector",
        		new Object[] {ipAddress , port, transport},
        		new String[]{String.class.getCanonicalName(), int.class.getCanonicalName(), String.class.getCanonicalName()});
        return stat;
		
	}
	
	public boolean addSipConnector(String ipAddress, int port, String transport) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		LOG.info("-------Adding SipConnector "+ipAddress+":"+port+"/"+transport);
		// adding connector
        SipConnector udpSipConnector = new SipConnector();
        udpSipConnector.setIpAddress(ipAddress);
        udpSipConnector.setPort(port);
        udpSipConnector.setTransport(transport);
        
        Boolean stat = (Boolean) mbsc.invoke(objectName, "addSipConnector",
        		new Object[] {udpSipConnector}, 
        		new String[]{SipConnector.class.getCanonicalName()});
		return stat;
	}
	
	public void traceSipConnectors() throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		SipConnector[] sipConnectors = (SipConnector[]) mbsc.invoke(objectName, "findSipConnectors", null, null);  
		for (int i = 0; i < sipConnectors.length; i++) {
			LOG.info(sipConnectors[i]);
		}
	}
	public void close() throws IOException {
		jmxc.close();
	}
	
	public void init() throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException {
		MBeanInfo mbeanInfo = mbsc.getMBeanInfo(objectName);
		
		MBeanOperationInfo[] operationInfos = mbeanInfo.getOperations();
		System.out.println("MBean Operations:");
		String[] operationNames = new String[operationInfos.length];
	
	        for (int i = 0; i < operationInfos.length; i++) {
	           System.out.println(i + ": " + operationInfos[i].getDescription() + " " +
	        		   operationInfos[i].getSignature());
	           operationNames[i] = operationInfos[i].getName();
	        }
			
	        SipConnector[] sipConnectors = (SipConnector[]) mbsc.invoke(objectName, "findSipConnectors", null, null);
//	       
	        for (int i = 0; i < sipConnectors.length; i++) {
				LOG.info(sipConnectors[i]);
			}
	        LOG.info("-------Removing 192.168.88.2:5080/UDP");
	        mbsc.invoke(objectName, "removeSipConnector",
	        		new Object[] {"192.168.88.2" , 5080, "udp"},
	        		new String[]{String.class.getCanonicalName(), int.class.getCanonicalName(), String.class.getCanonicalName()});
	        
	        		
	        sipConnectors = (SipConnector[]) mbsc.invoke(objectName, "findSipConnectors", null, null);
	        
	        for (int i = 0; i < sipConnectors.length; i++) {
				LOG.info(sipConnectors[i]);
			}
	       
	        
	        // adding udp connector
	        SipConnector udpSipConnector = new SipConnector();
	        udpSipConnector.setIpAddress("192.168.88.2");
	        udpSipConnector.setPort(5072);
	        udpSipConnector.setTransport(ListeningPoint.UDP);
	        
	        mbsc.invoke(objectName, "addSipConnector",
	        		new Object[] {udpSipConnector}, 
	        		new String[]{SipConnector.class.getCanonicalName()});
	        LOG.info("-------Adding 192.168.88.2:5072/UDP");
	        sipConnectors = (SipConnector[]) mbsc.invoke(objectName, "findSipConnectors", null, null);
	        
	        for (int i = 0; i < sipConnectors.length; i++) {
				LOG.info(sipConnectors[i]);
			}
	        
	        // adding tcp connector
	        SipConnector tcpSipConnector = new SipConnector();
	        tcpSipConnector.setIpAddress("" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "");
	        tcpSipConnector.setPort(5072);
	        tcpSipConnector.setTransport(ListeningPoint.TCP);
	       // assertTrue((Boolean)mbsc.invoke(objectName, "addSipConnector",new Object[] {tcpSipConnector}, new String[]{SipConnector.class.getCanonicalName()}));
	        // making sure they were both added correctly
	        sipConnectors = (SipConnector[]) mbsc.invoke(objectName, "findSipConnectors", null, null);
	       // assertEquals(2, sipConnectors.length);
	        
	       // Thread.sleep(TIMEOUT);
	}
	public static void main(String argv[]) {
		
		try {
			JMXManager m=JMXManager.getInstanace();
			m.traceSipConnectors();
			m.removeSipConnector("192.168.88.3", 5060, "udp");
			System.in.read();
			m.traceSipConnectors();
			m.addSipConnector("192.168.88.3", 5060, "udp");
			
			m.traceSipConnectors();
			System.in.read();
			m.close();
			
		} catch (MalformedObjectNameException | InstanceNotFoundException
				| IntrospectionException | ReflectionException | MBeanException
				| IOException e) {
			LOG.error("ERROR",e);
		}
	}

	@Override
	public void handleNotification(Notification notification, Object handback) {
		LOG.info("Notification "+notification+" callback "+handback);
		
	}

	@Override
	public void onKeepAliveTimeout(SipConnector arg0, String arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sipConnectorAdded(SipConnector arg0) {
		LOG.info("ADDED "+arg0);
		
	}

	@Override
	public void sipConnectorRemoved(SipConnector arg0) {
		LOG.info("REMOVED "+arg0);
		
	}



}
