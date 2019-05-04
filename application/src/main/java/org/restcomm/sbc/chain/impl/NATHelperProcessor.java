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
package org.restcomm.sbc.chain.impl;


import java.util.List;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import org.apache.log4j.Logger;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DefaultProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.bo.Location;
import org.restcomm.sbc.bo.LocationNotFoundException;
import org.restcomm.sbc.managers.LocationManager;
import org.restcomm.sbc.managers.RouteManager;
import org.apache.commons.net.util.SubnetUtils;



/**
 * @author ocarriles@eolos.la (Oscar Andres Carriles)
 * @date 13 sept. 2016 18:10:42
 * @class NATHelperProcessor.java
 *
 */
public class NATHelperProcessor extends DefaultProcessor implements ProcessorCallBack {

	private static transient Logger LOG = Logger.getLogger(NATHelperProcessor.class);
	private LocationManager locationManager=LocationManager.getLocationManager();
	
	public NATHelperProcessor() {
		// just to notify spi instantiation
		super();
	}

	public NATHelperProcessor(ProcessorChain callback) {
		super(callback);
	}

	public NATHelperProcessor(String name, ProcessorChain callback) {
		super(name, callback);
	}

	public String getName() {
		return "NAT Helper Processor";
	}

	public int getId() {
		return this.hashCode();
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	private void processResponse(SIPMutableMessage message) {
		
		SipServletResponse response=(SipServletResponse) message.getContent();
		SipServletRequest request=response.getRequest();
		//SipServletRequest orequest=(SipServletRequest) request.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
		
		if (LOG.isTraceEnabled()) {
			LOG.trace(">> processResponse()");
			LOG.trace(">> request  came from host/port:"+request.getRemoteHost()+":"+request.getRemotePort());		
			LOG.trace(">> message  coming from host:   "+message.getSourceRemoteAddress());
			LOG.trace(">> response coming from Contact:"+response.getHeader("Contact"));
			
		}
		
		
		SipURI fromURI 	= (SipURI) request.getFrom().getURI();
		SipURI contactURI = null;
		
		if(isRoutedAddress(request.getRemoteHost())){
			if(LOG.isTraceEnabled()) {
				LOG.trace("RouteAddress "+request.getRemoteHost()+" MUST not be fixed "+fromURI.toString());
			}
			return;
		}
		
		contactURI = ConfigurationCache.getSipFactory().createSipURI(fromURI.getUser(), request.getRemoteHost());
		contactURI.setPort(request.getRemotePort());
		
		if(LOG.isTraceEnabled()){ 
			LOG.trace("Patching NATed Contact Address from: "+request.getHeader("Contact")+" to: "+contactURI.toString());
		}
		
		/*
		 * Replace Contact address from IP/Port the message is coming from
		 * Used for Registration, Location subsystem pick NATed data
		 * from here
		 */
		try {
			if(response.getMethod().equals("REGISTER")) {
				if(!response.getAddressHeaders("Contact").hasNext())
					response.setAddressHeader("Contact", ConfigurationCache.getSipFactory().createAddress(contactURI));
				else
					LOG.warn("Contact address exists, CANNOT patch NATed Contact Address to: "+contactURI.toString());
			}
		} catch (ServletParseException e) {
				LOG.error("CANNOT Patch NATed Contact Address to: "+contactURI.toString(), e);
		}
		
		message.setContent(response);
		
	}

	private void processRequest(SIPMutableMessage message) {
		if (LOG.isTraceEnabled()) {
			LOG.trace(message.toString());
		}
		
		SipServletRequest request=(SipServletRequest) message.getContent();
		SipURI toURI 	= (SipURI) request.getTo().getURI();
		
		if (LOG.isTraceEnabled()) {
			LOG.trace(">> processRequest()");
			LOG.trace(">> request  came from host/port:"+request.getRemoteHost()+":"+request.getRemotePort());		
			LOG.trace(">> message  coming from host:   "+message.getSourceRemoteAddress());
			LOG.trace(">> request  coming from Contact:"+request.getHeader("Contact"));
			LOG.trace(">> request  is inital          :"+request.isInitial());
			LOG.trace(">> request  direction          :"+message.getDirection());
			
		}
		/*
		if(!request.isInitial()) {
			//
			// If the request goes to DMZ and it is not inital
			// it means that the original request came from a 
			// previously registered user.
			// has to patch NATed routes to reach the endpoint.
			//
			if(message.getDirection()==Message.SOURCE_MZ) {
				
					Location location = null;
					try {
						location = locationManager.getLocation(toURI.getUser(), ConfigurationCache.getDomain());
					} catch (LocationNotFoundException e) {
						LOG.error("User not found!",e);
						return;
					}
					if(isRoutedAddress(request.getRemoteHost())){
						if(LOG.isTraceEnabled()) {
							LOG.trace("RouteAddress "+location.getHost()+" MUST not be fixed "+location.getHost());
						}
						return;
					}
					toURI.setHost(location.getHost());
					toURI.setPort(location.getPort());
					toURI.setTransportParam(location.getTransport());
					request.setRequestURI(toURI);
					if(LOG.isTraceEnabled()){ 
						LOG.trace("Patching NATed Contact requestURI: "+toURI.toString());
					}
			}
			
		}
	*/
		message.setContent(request);		
	}

	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}


	@Override
	public double getVersion() {
		return 1.0;
	}
	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		SIPMutableMessage m = (SIPMutableMessage) message;
		SipServletMessage sm=m.getContent();
		
		
		if( sm instanceof SipServletRequest) {
			processRequest(m);
		}
		if (sm instanceof SipServletResponse) {
			processResponse(m);
		}
		
	}
	
	private boolean isRoutedAddress(String ipAddress) {
		List<String> localNetworks=ConfigurationCache.getLocalNetworks();
		
		for(String localNetwork:localNetworks) {
			if(LOG.isTraceEnabled()) {
				LOG.trace("Traversing localNetworks "+localNetwork);
			}
			SubnetUtils utils=new SubnetUtils(localNetwork);
			if(utils.getInfo().isInRange(ipAddress)) {
				if(LOG.isTraceEnabled()) {
					LOG.trace("ipAddress "+ipAddress+" Is in network "+localNetwork);
				}
				return true;	
			}
			if(LOG.isTraceEnabled()) {
				LOG.trace("ipAddress "+ipAddress+" Is NOT in network "+localNetwork);
			}
		}
		
		return false;
		
	}

}
