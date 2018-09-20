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

import java.io.IOException;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;
import org.mobicents.media.server.io.sdp.SdpException;
import org.restcomm.chain.ProcessorChain;
import org.restcomm.chain.processor.Message;
import org.restcomm.chain.processor.ProcessorCallBack;
import org.restcomm.chain.processor.impl.DefaultProcessor;
import org.restcomm.chain.processor.impl.ProcessorParsingException;
import org.restcomm.chain.processor.impl.SIPMutableMessage;
import org.restcomm.sbc.ConfigurationCache;
import org.restcomm.sbc.bo.Connector;
import org.restcomm.sbc.bo.Location;
import org.restcomm.sbc.bo.LocationNotFoundException;
import org.restcomm.sbc.bo.shiro.ShiroResources;
import org.restcomm.sbc.call.CallManager;
import org.restcomm.sbc.managers.LocationManager;
import org.restcomm.sbc.managers.MessageUtil;
import org.restcomm.sbc.managers.ProtocolAdapterFactory;
import org.restcomm.sbc.managers.RouteManager;
import org.restcomm.sbc.media.MediaController.StreamProfile;
import org.restcomm.sbc.media.MediaSession;





/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    28 nov. 2016 9:15:15
 * @class   IncomingDPIProcessor.java
 *
 */
public class IncomingDPIProcessor extends DefaultProcessor implements ProcessorCallBack {

	private static transient Logger LOG = Logger.getLogger(IncomingDPIProcessor.class);
	
	
	public IncomingDPIProcessor(ProcessorChain callback) {
		super(callback);
	}

	public IncomingDPIProcessor(String name, ProcessorChain callback) {
		super(name, callback);
	}

	public String getName() {
		return "Incoming DPI Processor";
	}

	public int getId() {
		return this.hashCode();
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}


	@Override
	public String getVersion() {
		return "1.0.0";
	}

	
	private void processRequest(Message message) throws ProcessorParsingException {
		
		SIPMutableMessage m = (SIPMutableMessage) message;
		SipServletMessage sm=m.getContent();
		MediaSession mediaSession;
		Connector connector;
		
		m.setSourceLocalAddress(sm.getLocalAddr());
		m.setSourceRemoteAddress(sm.getRemoteAddr());
		m.setSourceTransport(sm.getTransport()==null?"UDP":sm.getTransport().toUpperCase());
		
		if (RouteManager.isFromDMZ(m.getContent())) {
			m.setDirection(Message.SOURCE_DMZ);
			try {		
				connector = RouteManager.getRouteManager().getRouteToMZ(sm.getLocalAddr(), sm.getLocalPort(),
						sm.getInitialTransport());	
				m.setTargetLocalAddress(connector.getHost());
				m.setTargetRemoteAddress(ConfigurationCache.getRoutingPolicy().getCandidate().getHost());
				m.setTargetTransport(connector.getTransport().toString());		

			} catch (Exception e) {
					LOG.error("ERROR", e);
			}
		}
		else {
			m.setDirection(Message.SOURCE_MZ);
			SipURI toURI 	= (SipURI) sm.getTo().  getURI();
			m.setTarget(Message.TARGET_DMZ);
			m.setTargetLocalAddress(ConfigurationCache.getIpOfDomain());
			
			if(!sm.getMethod().equals("REGISTER")) {
				// Comes from MZ Must create LEG to DMZ based on Location info
				Location location = null;
				
				try {
					location = LocationManager.getLocationManager().getLocation(toURI.getUser() + "@" + ConfigurationCache.getDomain());
					m.setTargetRemoteAddress(location.getHost());
					m.setTargetTransport(location.getTransport().toUpperCase());
				} catch (LocationNotFoundException e) {
					LOG.warn(toURI.getUser()+" is not a registered user in the domain "+ ConfigurationCache.getDomain());
					LOG.warn(" this UAC may be registered directly in the back-Sip REGISTRAR");
					m.setTargetRemoteAddress(ConfigurationCache.getRoutingPolicy().getCandidate().getHost());
							
				}
			}
		}
		
		m.setTarget(Message.TARGET_B2BUA);
		CallManager callManager = (CallManager) ShiroResources.getInstance().get(CallManager.class);

		
		if(sm.getContentLength()>0 &&
			sm.getContentType().equals("application/sdp")) {
			try {
				mediaSession=callManager.getMediaSession(sm.getSession().getId());
				StreamProfile streamProfile=(m.getSourceTransport().equals(ProtocolAdapterFactory.PROTOCOL_WSS)?StreamProfile.WEBRTC:StreamProfile.AVP);		
				mediaSession.buildOffer(streamProfile, new String(sm.getRawContent()), m.getTargetLocalAddress());	
				m.setMetadata(mediaSession);	
			} catch (IOException | SdpException  e) {
				LOG.error("Invalid MediaMetadata!", e);
			}
			
		}
		
		
	}
	private void processResponse(Message message) {
		SIPMutableMessage m = (SIPMutableMessage) message;
		SipServletMessage sm=m.getContent();
		MediaSession mediaSession;
		Connector connector;
		
		m.setSourceLocalAddress(sm.getLocalAddr());
		m.setSourceRemoteAddress(sm.getRemoteAddr());
		m.setSourceTransport(sm.getTransport()==null?"UDP":sm.getTransport().toUpperCase());
		
		if (RouteManager.isFromDMZ(m.getContent())) {
			m.setDirection(Message.SOURCE_DMZ);
			m.setTarget(Message.TARGET_B2BUA);
			try {		
				connector = RouteManager.getRouteManager().getRouteToMZ(sm.getLocalAddr(), sm.getLocalPort(),
						sm.getInitialTransport());	
				m.setTargetLocalAddress(connector.getHost());
				m.setTargetRemoteAddress(ConfigurationCache.getRoutingPolicy().getCandidate().getHost());
				m.setTargetTransport(connector.getTransport().toString());		

			} catch (Exception e) {
					LOG.error("ERROR", e);
			}
		}
		else {
			m.setDirection(Message.SOURCE_MZ);
			SipURI fromURI 	= (SipURI) sm.getFrom().  getURI();
			m.setTarget(Message.TARGET_DMZ);
			if(!sm.getMethod().equals("REGISTER")) {
				// Comes from MZ Must create LEG to DMZ based on Location info
				Location location = null;
				
				try {
					location = LocationManager.getLocationManager().getLocation(fromURI.getUser() + "@" + ConfigurationCache.getDomain());
					m.setTargetLocalAddress(ConfigurationCache.getIpOfDomain());
					m.setTargetRemoteAddress(location.getHost());
					m.setTargetTransport(location.getTransport().toUpperCase());
				} catch (Exception e) {
					LOG.error("ERROR", e);
				}
			}
		}
		CallManager callManager = (CallManager) ShiroResources.getInstance().get(CallManager.class);

		if(sm.getContentLength()>0 &&
			sm.getContentType().equals("application/sdp")) {
			try {			
				SipServletResponse response=(SipServletResponse) sm;
				String callSessionId=getCallSessionId(response.getRequest());
				mediaSession=callManager.getMediaSession(callSessionId);
				StreamProfile streamProfile=(m.getSourceTransport().equals(ProtocolAdapterFactory.PROTOCOL_WSS)?StreamProfile.WEBRTC:StreamProfile.AVP);
				mediaSession.buildAnswer(streamProfile, new String(sm.getRawContent()), m.getTargetLocalAddress());		
				m.setMetadata(mediaSession);		
			} catch (IOException | SdpException  e) {
				LOG.error("Invalid MediaMetadata!", e);
			}
			
		}
		
	}
	
	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		SIPMutableMessage m = (SIPMutableMessage) message;
		SipServletMessage sm=(SipServletMessage) message.getContent();
		
		
		
		if(sm instanceof SipServletRequest) {
			processRequest(m);
		}
		if(sm instanceof SipServletResponse) {
			processResponse(m);
		}
		
		if (LOG.isTraceEnabled()) {
			LOG.trace(message.toString());
			LOG.trace("-------" + sm.getLocalAddr() + "->" + sm.getRemoteAddr());
			LOG.trace("-------Receiving message: \n" + sm);
		}
		
	}
	
	

	private String getCallSessionId(SipServletRequest currentRequest) {
		SipServletRequest oRequest=(SipServletRequest) currentRequest.getSession().getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
		return oRequest.getSession().getId();
	}

}