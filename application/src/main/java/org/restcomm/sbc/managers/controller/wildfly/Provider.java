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
package org.restcomm.sbc.managers.controller.wildfly;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.mobicents.servlet.sip.startup.SipProtocolHandler;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;

import javax.management.MBeanException;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
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

import com.sun.management.OperatingSystemMXBean;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;

import org.jboss.dmr.ModelNode;

/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    9 jun. 2017 12:43:54
 * @class   Provider.java
 *
 */
public class Provider implements ManagementProvider,
 NotificationListener, SipConnectorListener {
	
	private static transient Logger LOG = Logger.getLogger(Provider.class);
	
	private JMXConnector jmxc;
	private MBeanServerConnection mbsc;
	private ObjectName osMBeanName;
	private ObjectName objectMBeanName;
	private ObjectName sipMBeanName;
	private OperatingSystemMXBean osMBean;

	private ModelControllerClient client;
	private ArrayList<Connector> connectors;
	
	
	public Provider() throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException {
		
			
		
		if(LOG.isInfoEnabled()) {
			LOG.info("Create an RMI connector client ");
				
		}

		client = ModelControllerClient.Factory.create(
                InetAddress.getByName("127.0.0.1"), 9990);
		
		
	}
	

	
	public boolean removeSipConnector(String ipAddress, int port, String transport) throws IOException {
		
        return true;
		
	}
	
	public boolean addSipConnector(String ipAddress, int port, String transport, String interfaceName) throws IOException {
		
		// adding connector
		transport=transport.toLowerCase();
		//addInterface(interfaceName, ipAddress);
		addSocketBinding(interfaceName+"-"+port+"-sip-"+transport, port, interfaceName);	  
		addConnector("sip-"+transport+"-"+ipAddress+"-"+port, interfaceName+"-"+port+"-sip-"+transport);
        
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
					//System.err.println("SipConnector: "+handler.getSipConnector());
					
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
		client.close();
	}
	
	public int getCPULoadAverage() {
		ModelNode result=null;
		final ModelNode base = new ModelNode();
        base.get("operation").set(ClientConstants.READ_ATTRIBUTE_OPERATION);
        base.get("address").add("core-service", "platform-mbean").add("type", "operating-system");
        
        
        	ModelNode operation = base.clone();
        	operation.get("recursive").set(true);
    		operation.get("attributes").set(true);
    		operation.get("operations").set(false);
    		operation.get(ClientConstants.INCLUDE_RUNTIME).set(true);
    		operation.get("name").set("system-load-average");
    		
        	try {
				result = client.execute(operation);
			} catch (IOException e) {
				LOG.error("Cannot read CPU");
				return 0;
			}
         return (int) result.get("result").asInt();
  		 
	}
	
	public int getMemoryUsage() {
		ModelNode result=null;
		final ModelNode base = new ModelNode();
        base.get("operation").set(ClientConstants.READ_ATTRIBUTE_OPERATION);
        base.get("address").add("core-service", "platform-mbean").add("type", "memory");
        
        
        	ModelNode operation = base.clone();
        	operation.get("recursive").set(true);
    		operation.get("attributes").set(true);
    		operation.get("operations").set(false);
    		operation.get("name").set("heap-memory-usage");
    		
        	try {
				result = client.execute(operation);
			} catch (IOException e) {
				LOG.error("Cannot read memory");
				return 0;
			}
  		  long used=result.get(ClientConstants.RESULT).get("used").asLong();
  		  long max =result.get(ClientConstants.RESULT).get("max").asLong();
  		  return (int) (((double)(max-used)/(double)max)*100);

	}
	
	
	
	private void removeInterface(String name) throws IOException {
		final ModelNode baseRemove = new ModelNode();
        baseRemove.get("operation").set("remove");
        baseRemove.get("address").add("interface", name);
        {
        	final ModelNode operation = baseRemove.clone();
        	execute(operation);
           
           
        }
	}
	
	private void removeConnector(String name) throws IOException {
		final ModelNode baseRemove = new ModelNode();
        baseRemove.get("operation").set("remove");
        baseRemove.get("address").add("subsystem", "sip").add("connector", name);
        {
        	final ModelNode operation = baseRemove.clone();
        	execute(operation);
           
           
        }
	}
	
	private void listConnectors()  {
		ModelNode result=null;
		final ModelNode base = new ModelNode();
        base.get("operation").set("read-attribute");
        base.get("address").add("subsystem", "sip").add("connector", "*");
        
        {
        	ModelNode operation = base.clone();
        	operation.get("recursive").set(true);
    		operation.get("attributes").set(true);
    		operation.get("operations").set(false);
    		operation.get( "name" ).set( "name" );

        	try {
				result = client.execute(operation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	
        	List<ModelNode> conns = result.get(ClientConstants.RESULT).asList();
        		
        	for(ModelNode n:conns) {	
        		//SipTcp conn=getConnector(n.toJSONString(false));
        		try {
        			String connectorName=n.get("result").asString();
        			String socketBinding=readConnectorAttr(connectorName,"name").get("result").asString();
        			String iface=readSocketBindingAttr(socketBinding,"interface").get("result").asString();
        			int port=readSocketBindingAttr(socketBinding,"port").get("result").asInt();

        			String ip=readInterfaceAttr(iface,"inet-address").get("result").asString();

        		System.out.println("---->"+connectorName);
        		System.out.println("====>"+socketBinding+":"+port );
        		System.out.println("....>"+iface );

        		System.out.println("++++>"+ip );
        		} catch(Exception e){System.err.println(e.getMessage());}
                
        	}
           
        }
		
	}
	
	
	
	@Override
	public boolean addInterface(String name, String ip) throws IOException {
		if(LOG.isInfoEnabled()) {
			LOG.info("Adding interface "+name+":"+ip);
		}
		final ModelNode base = new ModelNode();
        base.get("operation").set("add");
        base.get("address").add("interface", name);
        {
            final ModelNode operation = base.clone();
            operation.get("any-address").set(true);
            operation.get("any-ipv4-address").set(true);
           
        }
        {
            final ModelNode operation = base.clone();
            operation.get("inet-address").set("${jboss.bind.address."+name+":"+ip+"}");
            populateCritieria(operation, "${jboss.bind.address."+name+":"+ip+"}");
            ModelNode result = execute(operation);
            if (result.hasDefined("outcome") && "success".equals(result.get("outcome").asString())) {
            	return true;
            }
            return false;
        } 
	}
	
	private void removeSocketBinding(String name) throws IOException {
		final ModelNode baseRemove = new ModelNode();
        baseRemove.get("operation").set("remove");
        baseRemove.get("address").add("socket-binding-group", "standard-sockets").add("socket-binding", name);
        {
        	final ModelNode operation = baseRemove.clone();
        	
        	execute(operation);
           
        }
	}
	
	private void addSocketBinding(String name, int port, String interfaceName) throws IOException {
		if(LOG.isInfoEnabled()) {
			LOG.info("Adding Socket-binding "+name+": "+interfaceName+":"+port);
		}
		final ModelNode base = new ModelNode();
        base.get("operation").set("add");
        base.get("address").add("socket-binding-group", "standard-sockets").add("socket-binding", name);
        {
            final ModelNode operation = base.clone();
            
            operation.get("interface").set(interfaceName);
            operation.get("port").set(port);
           // operation.get("bound").set(false);
            operation.get("fixed-port").set(true);
           
            execute(operation);
            
        } 
       
	}
	
	
	private void addConnector(String name, String binding) throws IOException {
		if(LOG.isInfoEnabled()) {
			LOG.info("Adding SIP Connector "+name+": "+binding);
		}
		final ModelNode base = new ModelNode();
        base.get("operation").set("add");
        base.get("address").add("subsystem", "sip").add("connector", name);
        {
            final ModelNode operation = base.clone();
            operation.get("socket-binding").set(binding);
            operation.get("name").set(name);
            operation.get("scheme").set("sip");
            operation.get("protocol").set("SIP/2.0");
            execute(operation);
            
        } 
       
	}
	
	protected void populateCritieria(final ModelNode model, String ip) {
		ModelNode inet = model.get("inet-address");
        inet.set(ip);
       /* ModelNode any = model.get("any-address");
        any.set(true);
        ModelNode anyip4 = model.get("any-ipv4-address");
        anyip4.set(true);
        */

	}
	
	protected ModelNode execute(final ModelNode operation) {
        try {
            final ModelNode result = client.execute(operation);
            
            if (result.hasDefined("outcome") && "success".equals(result.get("outcome").asString())) {
            	System.err.println("Operation outcome is " + result.toJSONString(true));
                return result.get("result");
            } else {
                System.err.println("Operation outcome is " + result.toJSONString(true));
                return result.get("result");
            }
        } catch (IOException e) {
        	LOG.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
	
	private ModelNode reload(boolean adminOnly) {
		ModelNode result=null;
	    ModelNode operation = new ModelNode();
	    operation.get("address").setEmptyList();
	    operation.get("operation").set("reload");
	    operation.get("admin-only").set(adminOnly);
	    try {
	        result = client.execute(operation);
	        if (!"success".equals(result.get(ClientConstants.OUTCOME).asString())) {
	            System.err.println("Reload operation didn't finished successfully: " + result.asString());
	        }
	    } catch(IOException e) {
	        final Throwable cause = e.getCause();
	        if (!(cause instanceof ExecutionException) && !(cause instanceof CancellationException)) {
	            throw new RuntimeException(e);
	        } // else ignore, this might happen if the channel gets closed before we got the response
	    }
	    return result;
	}
	private ModelNode readConnectorAttr(String name, String attrName) throws IOException {
		
		ModelNode result=null;
		final ModelNode base = new ModelNode();
        base.get("operation").set(ClientConstants.READ_ATTRIBUTE_OPERATION);
        base.get("address").add("subsystem", "sip").add("connector", name);
        
        
        	ModelNode operation = base.clone();
        	operation.get("recursive").set(true);
    		operation.get("attributes").set(true);
    		operation.get("operations").set(false);
    		operation.get("name").set(attrName);
    		
        	try {
				return result = client.execute(operation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
	}
	
	
	
	private ModelNode readSocketBindingAttr(String name, String attrName) throws IOException {
		
		ModelNode result=null;
		final ModelNode base = new ModelNode();
        base.get("operation").set(ClientConstants.READ_ATTRIBUTE_OPERATION);
        base.get("address").add("socket-binding-group", "standard-sockets").add("socket-binding", name);
        
        	ModelNode operation = base.clone();
        	operation.get("recursive").set(true);
    		operation.get("attributes").set(true);
    		operation.get("operations").set(false);
    		operation.get("name").set(attrName);
    		
        	try {
				return result = client.execute(operation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
	}
	
	private ModelNode readInterfaceAttr(String name, String attrName) throws IOException {
		
		ModelNode result=null;
		final ModelNode base = new ModelNode();
        base.get("operation").set(ClientConstants.READ_ATTRIBUTE_OPERATION);
        base.get("address").add("interface", name);        
        
        	ModelNode operation = base.clone();
        	operation.get("recursive").set(true);
    		operation.get("attributes").set(true);
    		operation.get("operations").set(false);
    		operation.get("name").set(attrName);
    		
        	try {
				return result = client.execute(operation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
	}
	
	
	
	public static void main(String argv[]) throws InstanceNotFoundException, MalformedObjectNameException, IntrospectionException, ReflectionException, IOException {
		
		  Provider provider=new Provider();
		 
		 // System.out.println(provider.getMemoryUsage());
		 // System.out.println(provider.getCPULoadAverage());
		 
		//  provider.removeConnector("sip-udp-192.168.88.3-5060");
		//  provider.removeSocketBinding("sip-udp");
		//  provider.removeInterface("eth-C4E98401EAA4-0");
		 
		  
		  
		  
		  
		  
		// ModelNode result=provider.reload(true);
		  
		provider.addInterface("eth-C4E98401EAA4-0", "10.0.0.10");		 
		provider.addSocketBinding("sip-udp", 5060, "eth-C4E98401EAA4-0");		  		  
		provider.addConnector("sip-udp-10.0.0.10-5060","sip-udp");
		  
		 // provider.listConnectors();
		 
		// provider.reload(false);
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
	public void reload() {
		reload(true);
		
	}



	@Override
	public int getContext() {
		// TODO Auto-generated method stub
		return 0;
	}


	
}
