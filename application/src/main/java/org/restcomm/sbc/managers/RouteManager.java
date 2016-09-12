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

import java.net.NoRouteToHostException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.bo.Connector;
import org.restcomm.sbc.bo.Location;
import org.restcomm.sbc.bo.LocationNotFoundException;
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
	private LocationManager locationManager;
	private HashMap<String, Connector> dmzTable =null;
	private HashMap<String, Connector> mzTable  =null;
	
	private static transient Logger LOG = Logger.getLogger(RouteManager.class);
	
	private RouteManager() {
		updateRoutingTable();
		locationManager=LocationManager.getLocationManager();
		
	}
	
	public static RouteManager getRouteManager() {
		if(routeManager==null) {
			routeManager=new RouteManager();
		}
		return routeManager;
	}
	
	private void updateRoutingTable() {
		dmzTable =new HashMap<String, Connector>();
		mzTable  =new HashMap<String, Connector>();
		DaoManager daos=ShiroResources.getInstance().get(DaoManager.class);		
        RoutesDao rdao = daos.getRoutesDao();
        ConnectorsDao cdao = daos.getConnectorsDao();
       
        final List<Route> dmzRoutes = rdao.getRoutes();
        for(Route dmzRoute:dmzRoutes) {
        	Connector source=cdao.getConnector(dmzRoute.getSourceConnector());
        	Connector target=cdao.getConnector(dmzRoute.getTargetConnector());	
        	
        	dmzTable.put(source.getTransport()+":"+source.getPort(), target);
        	mzTable. put(target.getTransport()+":"+target.getPort(), source);
        	if(LOG.isInfoEnabled()) {
        		LOG.info("DMZ Route add "+source.toPrint()+" => "+target.toPrint());
        		LOG.info("MZ  Route add "+target.toPrint()+" => "+source.toPrint());
        	}
        }
   
	}
	
	public Connector getRouteToDMZ(int sourcePort, String sourceTransport) throws NoRouteToHostException {
		if(sourceTransport==null) {
			// implicit transport
			sourceTransport="UDP";
		}
		if(sourcePort<0) {
			// implicit port
			sourcePort=5060;
		}
		Connector connector=dmzTable.get(sourceTransport.toUpperCase()+":"+sourcePort);
		if(connector==null)
			throw new NoRouteToHostException("No source Connector for "+sourceTransport+":"+sourcePort);
		if(LOG.isTraceEnabled()) {
			LOG.trace("oo Getting route for transport="+sourceTransport+" port="+sourcePort);
			LOG.trace("ooo "+connector.toPrint());
		}
		return connector;
	}
	
	public Connector getRouteToMZ(int sourcePort, String sourceTransport) throws NoRouteToHostException {
		if(sourceTransport==null) {
			// implicit transport
			sourceTransport="UDP";
		}
		if(sourcePort<0) {
			// implicit port
			sourcePort=5060;
		}
		Connector connector=mzTable.get(sourceTransport.toUpperCase()+":"+sourcePort);
		if(connector==null)
			throw new NoRouteToHostException("No source Connector for "+sourceTransport+":"+sourcePort);
		if(LOG.isTraceEnabled()) {
			LOG.trace("oo Getting route for transport="+sourceTransport+" port="+sourcePort);
			LOG.trace("ooo "+connector.toPrint());
		}
		return connector;
	}
	
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
	
	public static Tag getSourceTag(SipServletMessage message) {	
		String host =message.getLocalAddr();	
		Tag tag=NetworkManager.getTag(host);
		if(tag!=Tag.DMZ&&tag!=Tag.MZ) {
			LOG.warn("Not tagged NetworkPoint for host "+host);
			
		}
		return tag;
		
		
	}
	
	public static boolean isFromDMZ(SipServletMessage message) {	
		String host =message.getLocalAddr();	
		return NetworkManager.getTag(host)==Tag.DMZ?true:false;	
		
	}

}
