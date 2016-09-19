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
package org.restcomm.sbc.managers;

import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.bo.Connector;
import org.restcomm.sbc.bo.Route;
import org.restcomm.sbc.bo.NetworkPoint.Tag;
import org.restcomm.sbc.bo.shiro.ShiroResources;
import org.restcomm.sbc.dao.ConnectorsDao;
import org.restcomm.sbc.dao.DaoManager;
import org.restcomm.sbc.dao.RoutesDao;



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    30 ago. 2016 18:51:58
 * @class   RouteManager.java
 *
 */
public class RouteManager {
	
	private static RouteManager routeManager;
	private HashMap<String, Connector> dmzTable =null;
	
	private static transient Logger LOG = Logger.getLogger(RouteManager.class);
	
	private RouteManager() {
		updateRoutingTable();
		
	}
	
	public static RouteManager getRouteManager() {
		if(routeManager==null) {
			routeManager=new RouteManager();
		}
		return routeManager;
	}
	
	private void updateRoutingTable() {
		dmzTable =new HashMap<String, Connector>();
		DaoManager daos=ShiroResources.getInstance().get(DaoManager.class);		
        RoutesDao rdao = daos.getRoutesDao();
        ConnectorsDao cdao = daos.getConnectorsDao();
       
        final List<Route> dmzRoutes = rdao.getRoutes();
        for(Route dmzRoute:dmzRoutes) {
        	Connector source=cdao.getConnector(dmzRoute.getSourceConnector());
        	Connector target=cdao.getConnector(dmzRoute.getTargetConnector());	
        	
        	dmzTable.put(NetworkManager.getIpAddress(source.getPoint())+":"+source.getTransport()+":"+source.getPort(), target);
        	
        	if(LOG.isInfoEnabled()) {
        		LOG.info("DMZ Route add "+source.toPrint()+" => "+target.toPrint());
        		
        	}
        }
   
	}
	
	public Address getRegistrationContactAddress(SipServletRequest request) throws NoRouteToHostException {
		
		SipURI uri=(SipURI) request.getFrom().getURI();
		SipFactory sipFactory = ConfigurationCache.getSipFactory();
		Connector connector=null;
		connector = getRouteToMZ(uri.getHost(), uri.getPort(), uri.getTransportParam());
		
		SipURI contactUri = sipFactory.createSipURI(uri.getUser(), NetworkManager.getIpAddress(connector.getPoint()));
		contactUri.setPort(connector.getPort());
		contactUri.setTransportParam(connector.getTransport().toString());
		return sipFactory.createAddress(contactUri);
	}
	
	public SipURI getContactAddress(String user, InetSocketAddress address) throws NoRouteToHostException {
		
		SipFactory sipFactory = ConfigurationCache.getSipFactory();
		SipURI contactUri = sipFactory.createSipURI(user, address.getHostString());
		
		contactUri.setPort(address.getPort());
		return contactUri;
	}
	
	public Connector getRouteToMZ(String sourceHost, int sourcePort, String sourceTransport) throws NoRouteToHostException {
		if(sourceTransport==null) {
			// implicit transport
			sourceTransport="UDP";
		}
		if(sourcePort<0) {
			// implicit port
			sourcePort=5060;
		}
		if(LOG.isTraceEnabled()) {
			LOG.trace("oo Getting route to MZ for host="+sourceHost+" transport="+sourceTransport+" port="+sourcePort);
		}
		Connector connector=dmzTable.get(sourceHost+":"+sourceTransport.toUpperCase()+":"+sourcePort);
		if(connector==null)
			throw new NoRouteToHostException("No source Connector for "+sourceHost+":"+sourceTransport+":"+sourcePort);
		if(LOG.isTraceEnabled()) {
			LOG.trace("ooo "+connector.toPrint());
		}
		return connector;
	}
	/*
	public String getTargetTransport(SipServletMessage sourceMessage) throws NoRouteToHostException, LocationNotFoundException {
		String user=sourceMessage.getHeader("To");
		int sourcePort=sourceMessage.getLocalPort();
		String sourceTransport=sourceMessage.getTransport();
		
		if(isFromDMZ(sourceMessage)) {
			// Ought to find a route
			Connector connector=dmzTable.get(sourceTransport.toUpperCase()+":"+sourcePort);
			
			SipURI sipUri = ConfigurationCache.getSipFactory().createSipURI(user, ConfigurationCache.getTargetHost());
			sipUri.setTransportParam(connector.getTransport().toString());
			sipUri.setPort(connector.getPort());
			return connector.getTransport().toString();
		}
		// MZ messages are routed via Location data
		SipURI uri;
		Location location=null;
		try {
			uri = (SipURI) sourceMessage.getAddressHeader("To").getURI();
			location=locationManager.getLocation(user, uri.getHost());
		} catch (ServletParseException e) {
			LOG.error("ERROR", e);
		} 
		
		return location.getTransport();
		
	}
	
	public SipURI getTargetUri(SipServletMessage sourceMessage) throws NoRouteToHostException, LocationNotFoundException {
		
		String user=sourceMessage.getHeader("To");
		int sourcePort=sourceMessage.getLocalPort();
		String sourceTransport=sourceMessage.getTransport();
		
		if(isFromDMZ(sourceMessage)) {
			// Ought to find a route
			Connector connector=dmzTable.get(sourceTransport+":"+sourcePort);
			
			SipURI sipUri = ConfigurationCache.getSipFactory().createSipURI(user, ConfigurationCache.getTargetHost());
			sipUri.setTransportParam(connector.getTransport().toString());
			sipUri.setPort(connector.getPort());
			return sipUri;
		}
		// MZ messages are routed via Location data
		SipURI uri;
		Location location=null;
		try {
			uri = (SipURI) sourceMessage.getAddressHeader("To").getURI();
			location=locationManager.getLocation(user, uri.getHost());
		} catch (ServletParseException e) {
			LOG.error("ERROR", e);
		} 
		SipURI sipUri = ConfigurationCache.getSipFactory().createSipURI(user, location.getHost());
		sipUri.setTransportParam(location.getTransport());
		return sipUri;
		
	}
	*/
	
	
	public static boolean isFromDMZ(SipServletMessage message) {	
		String host =message.getLocalAddr();
		int port =message.getLocalPort();
		String transport=message.getTransport();
		
		if(LOG.isTraceEnabled()) {
			LOG.trace("Message "+message.getMethod()+" comming from "+host+":"+port+"/"+transport);		
		}
		
		return NetworkManager.getTag(host)==Tag.DMZ?true:false;	
		
	}

}
