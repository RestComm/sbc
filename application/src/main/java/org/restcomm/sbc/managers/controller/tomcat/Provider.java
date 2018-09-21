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
package org.restcomm.sbc.managers.controller.tomcat;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
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
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.listener.SipConnectorListener;
import org.restcomm.sbc.bo.Connector;
import org.restcomm.sbc.bo.NetworkPoint;
import org.restcomm.sbc.managers.NetworkManager;
import org.restcomm.sbc.managers.controller.ManagementProvider;
import org.restcomm.sbc.managers.controller.tomcat.Provider;


/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    22 nov. 2016 3:08:21
 * @class   TomcatJMXManager.java
 *
 */
public class Provider implements ManagementProvider,
 NotificationListener, SipConnectorListener {
	private static transient Logger LOG = Logger.getLogger(Provider.class);
	
	private JMXConnector jmxc;
	private MBeanServerConnection mbsc;
	private OperatingSystemMXBean osMBean;
	private ObjectName objectName;

	private ArrayList<Connector> connectors;
	
	
	public Provider() throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException {
		
		String urlString			="service:jmx:rmi://localhost:9999/jndi/rmi://localhost:9999/jmxrmi";
		String objectNamePointer	="Sip-Servlets:type=Service";
		
		
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("\nCreate an RMI connector client on: " +urlString);
				
		}
		
		JMXServiceURL url = new JMXServiceURL(urlString);
		jmxc = JMXConnectorFactory.connect(url, null);
		// Get the Platform MBean Server
				mbsc = jmxc.getMBeanServerConnection();
				
				osMBean = ManagementFactory.newPlatformMXBeanProxy(
						mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
				
				/*
				
				Set<ObjectName> mbeans = mbsc.queryNames(null, null);
				
				for (Object mbean : mbeans)	{
					if(LOG.isDebugEnabled()) {
						LOG.debug("Ontree: "+(ObjectName)mbean);
						readAttributes(mbsc, (ObjectName)mbean);
						readOperations(mbsc, (ObjectName)mbean);
					}
				}
				*/

				objectName = new ObjectName(objectNamePointer);
		
				Set<ObjectName> mbeans = mbsc.queryNames(objectName, null);
		
			for (Object mbean : mbeans)	{
				
				
				if(LOG.isDebugEnabled()) {
					LOG.debug("Ontree: "+(ObjectName)mbean);
					readAttributes(mbsc, (ObjectName)mbean);
					readOperations(mbsc, (ObjectName)mbean);
				}
			}
		
		
		mbsc.addNotificationListener(objectName, this, null, null);
		

	}
	
	
	
	private void readAttributes(final MBeanServerConnection mBeanServer, final ObjectName http)
	        throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException
	{
	    MBeanInfo info = mBeanServer.getMBeanInfo(http);
	    MBeanAttributeInfo[] attrInfo = info.getAttributes();

	    LOG.debug("Attributes for object: " + http +":\n");
	    for (MBeanAttributeInfo attr : attrInfo)
	    {
	        LOG.debug(" -- Attribute " + attr.getName() );
	    }
	}
	
	private void readOperations(final MBeanServerConnection mBeanServer, final ObjectName http)
	        throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException
	{
	    MBeanInfo info = mBeanServer.getMBeanInfo(http);
	    MBeanOperationInfo[] operInfo = info.getOperations();

	    LOG.debug("Operations for object: " + http +":\n");
	    for (MBeanOperationInfo oper : operInfo)
	    {
	        LOG.debug(" -- Operation --- " + oper.getName() );
	    }
	}
	
	public boolean removeSipConnector(String ipAddress, int port, String transport) throws IOException {
		
        Boolean stat;
		try {
			stat = (Boolean) mbsc.invoke(objectName, "removeSipConnector",
					new Object[] {ipAddress , port, transport},
					new String[]{String.class.getCanonicalName(), int.class.getCanonicalName(), String.class.getCanonicalName()});
		} catch (InstanceNotFoundException | MBeanException | ReflectionException e) {
			// TODO Auto-generated catch block
			throw new IOException(e.getMessage());
		}
        return stat;
		
	}
	
	public boolean addSipConnector(String ipAddress, int port, String transport, String interfaceName) throws IOException {
		
		// adding connector
        SipConnector sipConnector = new SipConnector();
        sipConnector.setIpAddress(ipAddress);
        sipConnector.setPort(port);
        sipConnector.setTransport(transport);
       
        Boolean stat;
		try {
			stat = (Boolean) mbsc.invoke(objectName, "addSipConnector",
					new Object[] {sipConnector}, 
					new String[]{SipConnector.class.getCanonicalName()});
		} catch (InstanceNotFoundException | MBeanException | ReflectionException e) {
			// TODO Auto-generated catch block
			throw new IOException(e.getMessage());
		}
		return stat;
	}
	
	public List<Connector> getConnectors() {
		SipConnector[] sipConnectors;
		
		try {
			sipConnectors= (SipConnector[]) mbsc.invoke(objectName, "findSipConnectors", null, null);
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
		}  
		return connectors;
		
		
	}
	
	public void traceSipConnectors() throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		SipConnector[] sipConnectors = (SipConnector[]) mbsc.invoke(objectName, "findSipConnectors", null, null);  
		for (int i = 0; i < sipConnectors.length; i++) {
			System.out.println(sipConnectors[i]);
			LOG.info(sipConnectors[i]);
		}
	}
	public void close() throws IOException {
		jmxc.close();
	}
	
	public int getCPULoadAverage() {
		
		return (int) (osMBean.getSystemCpuLoad()*100);
	}
	
	public int getMemoryUsage() {
		long free  = osMBean.getFreePhysicalMemorySize();
		long total = osMBean.getTotalPhysicalMemorySize();
		int used= (int) (((double)(total-free)/(double)total)*100);
		
		return used;
	}
	
	
	
	public static void main(String argv[]) {
		
		try {
			Provider m=new Provider();
			System.out.println("CPU "+m.getCPULoadAverage());
			System.out.println("MEM "+m.getMemoryUsage());
			
			
			m.close();
			
		} catch (MalformedObjectNameException | InstanceNotFoundException
				| IntrospectionException | ReflectionException 
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



	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public boolean addInterface(String name, String ipAddress) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public void reload() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public int getContext() {
		// TODO Auto-generated method stub
		return 0;
	}



}
