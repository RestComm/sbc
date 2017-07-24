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
package org.restcomm.sbc.managers.jmx.jboss;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.mobicents.servlet.sip.startup.SipProtocolHandler;

import javax.annotation.Resource;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMX;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.servlet.sip.SipFactory;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.modeler.BaseModelMBean;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.catalina.SipStandardService;
import org.mobicents.servlet.sip.core.SipService;
import org.mobicents.servlet.sip.listener.SipConnectorListener;
import org.jboss.as.controller.operations.common.InterfaceAddHandler;
import org.restcomm.sbc.bo.Connector;
import org.restcomm.sbc.bo.NetworkPoint;
import org.restcomm.sbc.managers.NetworkManager;
import org.restcomm.sbc.managers.jmx.JMXProvider;

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    9 jun. 2017 12:43:54
 * @class   Provider.java
 *
 */
public class Provider implements JMXProvider,
 NotificationListener, SipConnectorListener {
	
	private static transient Logger LOG = Logger.getLogger(Provider.class);
	
	private JMXConnector jmxc;
	private MBeanServerConnection mbsc;
	private ObjectName osMBeanName;
	private ObjectName objectMBeanName;
	private ObjectName sipMBeanName;

	private ArrayList<Connector> connectors;
	
	
	public Provider() throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException {
		
		
		String urlString			="service:jmx:remoting-jmx://localhost:9999";
		String objectNamePointer	="jboss.sip:type=SipConnector,*";
		String osNamePointer	    ="java.lang:type=OperatingSystem";
		String sipNamePointer	    ="jboss.as:subsystem=sip";
		
		System.out.println("Starting lookup ...");

		   
		JMXServiceURL serviceURL = new JMXServiceURL(urlString);
		jmxc = JMXConnectorFactory.connect(serviceURL, null);
		mbsc = jmxc.getMBeanServerConnection();
		 
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("\nCreate an RMI connector client on: " +urlString);
				
		}
		
		//ObjectName objectName = new ObjectName(objectNamePointer);  
		
		
		osMBeanName = new ObjectName(osNamePointer);
		sipMBeanName = new ObjectName(sipNamePointer);
				
				
				/*
				Set<ObjectInstance> mbeansn = mbsc.queryMBeans(null, null);
				
				for (Object mbean : mbeansn)	{
					if(LOG.isDebugEnabled()) {
						ObjectInstance instance=(ObjectInstance)mbean;
						
						System.err.println("Ontree: "+instance.getObjectName());
						//readAttributes(mbsc, (ObjectInstance)mbean);
						//readOperations(mbsc, (ObjectInstance)mbean);
					}
				}
				*/
				
		objectMBeanName=new ObjectName(objectNamePointer);
		Set<ObjectInstance> mbeans = mbsc.queryMBeans(objectMBeanName, null);
		ObjectName interfaceMBeanName = new ObjectName("jboss.as:interface=mz-201");	
		//Interface interfaceMBean=new Interface();
		//interfaceMBean.setName("mz-201");
		//interfaceMBean.setInetAddress("192.168.88.2");
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		
		
			InterfaceMBean interfaceProxy = JMX.newMBeanProxy(
			         mbsc, interfaceMBeanName, InterfaceMBean.class);
			interfaceProxy.setName("mz-201");
			interfaceProxy.setInetAddress("192.168.88.2");
		//	ObjectInstance instance = server.registerMBean(interfaceMBean, interfaceMBeanName);
		//	System.out.println("Instance="+instance);
		//	System.out.println("Oname="+instance.getObjectName());
			
			
		
		
		
			for (Object mbean : mbeans)	{
				
				
				if(LOG.isDebugEnabled()) {
					System.err.println("SipConnector Ontree: "+(ObjectInstance)mbean);
					ObjectInstance oInstance = (ObjectInstance)mbean;
					
					System.err.println("Class  Name:t" + oInstance.getClassName());
					
					//readAttributes(mbsc, (ObjectInstance)mbean);
					//readOperations(mbsc, (ObjectInstance)mbean);
				}
			}
			
		
		//mbsc.addNotificationListener(objectName, this, null, null);
		

	}
	
	
	
	private void readAttributes(final MBeanServerConnection mBeanServer, final ObjectInstance http)
	        throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException
	{
	    MBeanInfo info = mBeanServer.getMBeanInfo(http.getObjectName());
	    MBeanAttributeInfo[] attrInfo = info.getAttributes();

	    LOG.debug("Attributes for object: " + http +":\n");
	    for (MBeanAttributeInfo attr : attrInfo)
	    {
	        LOG.debug(" -- Attribute " + attr.getName() );
	    }
	}
	
	private void readOperations(final MBeanServerConnection mBeanServer, final ObjectInstance http)
	        throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException
	{
	    MBeanInfo info = mBeanServer.getMBeanInfo(http.getObjectName());
	    MBeanOperationInfo[] operInfo = info.getOperations();

	    LOG.debug("Operations for object: " + http +":\n");
	    for (MBeanOperationInfo oper : operInfo)
	    {
	        LOG.debug(" -- Operation --- " + oper.getName() );
	    }
	}
	
	public boolean removeSipConnector(String ipAddress, int port, String transport) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		
        Boolean stat=(Boolean) mbsc.invoke(objectMBeanName, "removeSipConnector",
        		new Object[] {ipAddress , port, transport},
        		new String[]{String.class.getCanonicalName(), int.class.getCanonicalName(), String.class.getCanonicalName()});
        return stat;
		
	}
	
	public boolean addSipConnector(String ipAddress, int port, String transport) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		
		// adding connector
		transport=transport.toLowerCase();
       
        //sipConnector.setIpAddress(ipAddress);
        //sipConnector.setPort(port);
        //sipConnector.setTransport(transport);
        
        SipConnectorProxy sipConnector = JMX.newMBeanProxy(
                mbsc, sipMBeanName, SipConnectorProxy.class);
        sipConnector.addConnector(
        					"sip-"+transport+"-"+ipAddress+"-"+port,  	// name
        					 port,				// loadBalancerSip Port			
        					 false,				// useStun
        					 ipAddress,			// staticServerAddress
        					 port,				// staticServerPort
        					 "sip-"+transport,  // socketBinding
        					 "",				// loadBalancerAddress
        					 "sip",				// scheme
        					 "sip-"+transport,	// name
        					 true,				// useStaticAddress
        					 ipAddress,			// hostNames
        					 "",				// stunServerAddress
        					 true,				// enabled
        					 "SIP/2.0",			// protocol
        					 1099,				// loadBalancerRMIPort
        					 3079,				// stunServerPort
        					 false				// useLoadBalancer);
        					 );
        
       /*
        Boolean stat = (Boolean) mbsc.invoke(sipMBeanName, "addConnector",
        		new Object[] {
        					 "SipConnector",	// name
        					 port,				// loadBalancerSip Port			
        					 false,				// useStun
        					 ipAddress,			// staticServerAddress
        					 port,				// staticServerPort
        					 "sip-"+transport,  // socketBinding
        					 "",				// loadBalancerAddress
        					 "sip",				// scheme
        					 "sip-"+transport,	// name
        					 false,				// useStaticAddress
        					 ipAddress,			// hostNames
        					 "",				// stunServerAddress
        					 true,				// enabled
        					 "SIP/2.0",			// protocol
        					 1099,				// loadBalancerRMIPort
        					 3079,				// stunServerPort
        					 false				// useLoadBalancer
        					 }, 
        		new String[] {
        					 String.class.getCanonicalName(),
        					 Integer.class.getCanonicalName(),
        					 Boolean.class.getCanonicalName(),
        					 String.class.getCanonicalName(),
        					 Integer.class.getCanonicalName(),
        					 String.class.getCanonicalName(),
        					 String.class.getCanonicalName(),
        					 String.class.getCanonicalName(),
        					 String.class.getCanonicalName(),
        					 Boolean.class.getCanonicalName(),
        					 String.class.getCanonicalName(),
        					 String.class.getCanonicalName(),
        					 Boolean.class.getCanonicalName(),
        					 String.class.getCanonicalName(),
        					 Integer.class.getCanonicalName(),
        					 Integer.class.getCanonicalName(),
        					 Boolean.class.getCanonicalName()
        					 });
        					 */
		return true;
	}
	
	public List<Connector> getConnectors() {
		SipConnector[] sipConnectors;
		
		try {
			
			Set<ObjectInstance> mbeans = mbsc.queryMBeans(objectMBeanName, null);
			for (Object mbean : mbeans)	{
				if(LOG.isDebugEnabled()) {
					System.err.println("SipConnector Ontree: "+(ObjectInstance)mbean);
					SipProtocolHandler handler = (SipProtocolHandler) mbsc.getAttribute(objectMBeanName, "protocol");
					System.err.println("SipConnector: "+handler.getSipConnector());
					
				}
			}       
			
			

			sipConnectors= (SipConnector[]) mbsc.invoke(objectMBeanName, "findSipConnectors", null, null);
			for (int i = 0; i < sipConnectors.length; i++) {
				if(LOG.isDebugEnabled()) {
					LOG.debug("SipConnector "+sipConnectors[i]);		
				}
				NetworkPoint point=NetworkManager.getNetworkPointByIpAddress(sipConnectors[i].getIpAddress());
				Connector connector=new Connector(sipConnectors[i].getPort(), Connector.Transport.getValueOf(sipConnectors[i].getTransport()), point.getId(), Connector.State.DOWN);
				connectors.add(connector);		
			}
		} catch (InstanceNotFoundException e) {
			LOG.error(e);
		} catch (MBeanException e) {
			LOG.error(e);
		} catch (ReflectionException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		} catch (AttributeNotFoundException e) {
			LOG.error(e);
		}  
		return connectors;
		
		
	}
	
	public void traceSipConnectors() throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		SipConnector[] sipConnectors = (SipConnector[]) mbsc.invoke(objectMBeanName, "findSipConnectors", null, null);  
		for (int i = 0; i < sipConnectors.length; i++) {
			System.out.println(sipConnectors[i]);
			LOG.info(sipConnectors[i]);
		}
	}
	public void close() throws IOException {
		jmxc.close();
	}
	
	public int getCPULoadAverage() {
		Object attrVal=null;
		try {
			attrVal = mbsc.getAttribute(osMBeanName, "SystemCpuLoad");
		} catch (AttributeNotFoundException | InstanceNotFoundException | MBeanException | ReflectionException
				| IOException e) {
			return 0;
		}
		
		return (int) ((double)attrVal*100);
	}
	
	public int getMemoryUsage() {
		long total=0;
		long free =0;
		try {
			total = (long) mbsc.getAttribute(osMBeanName, "TotalPhysicalMemorySize");
			free  = (long) mbsc.getAttribute(osMBeanName, "FreePhysicalMemorySize");
		} catch (AttributeNotFoundException | InstanceNotFoundException | MBeanException | ReflectionException
				| IOException e) {
			return 0;
		}
		
		int used= (int) (((double)(total-free)/(double)total)*100);
		return used;
	}
	
	public static void getServer()  throws Exception
    {  
  
		System.out.println("Starting lookup ...");

		   ObjectName mBeanName = new ObjectName("java.lang:type=Runtime");
		   String attributeName = "StartTime";

		   String host = "localhost";
		   int port = 9999;  // management-native port

		   String urlString = System.getProperty("jmx.service.url","service:jmx:remoting-jmx://" + host + ":" + port);
		   JMXServiceURL serviceURL = new JMXServiceURL(urlString);
		   JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceURL, null);
		   MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
		 
		   Object attrVal = connection.getAttribute(mBeanName, attributeName);
		   System.err.println("Value via JMX: " + new Date((Long) attrVal));
    }  
	
	public static void main(String argv[]) throws InstanceNotFoundException {
			
		
			//Provider.getServer();
			Provider m=null;
			try {
				m = new Provider();
				//m.addSipConnector("192.168.88.2", 5088, "udp");
			} catch (MalformedObjectNameException | IntrospectionException | ReflectionException | IOException  e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			m.getConnectors();
			
			System.err.println("CPU "+m.getCPULoadAverage());
			System.err.println("MEM "+m.getMemoryUsage());
			
			try {
				m.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	public interface SipConnectorProxy {
		
		void addConnector(  String name,
        				    int	 loadBalancerSipPort,			
        					boolean useStun,
        					String staticServerAddress,
        					int staticServerPort,
        					String socketBinding,
        					String loadBalancerAddress,
        					String scheme,
        					String name1,
        					boolean useStaticAddress,
        					String hostNames,
        					String stunServerAddress,
        					boolean enabled,
        					String protocol,
        					int loadBalancerRMIPort,
        					int stunServerPort,
        					boolean useLoadBalancer);

		void setTransport(String transport);

		void setPort(int port);
		
	}
	static class Interface implements InterfaceMBean {
		String inetAddress;
		String name;

		@Override
		public void setInetAddress(String ip) {
			this.inetAddress = ip;
			
		}

		@Override
		public void setName(String name) {
			this.name=name;
			
		}
		
	}
	
	public static interface InterfaceMBean {
		
		void setInetAddress(  String ip);
        void setName(String name);
		
	}

}
