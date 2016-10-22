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

package org.restcomm.sbc.chain.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.sip.Address;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;

import org.apache.log4j.Logger;
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
import org.restcomm.sbc.managers.LocationManager;
import org.restcomm.sbc.managers.MessageUtil;
import org.restcomm.sbc.managers.RouteManager;


/**
 * @author ocarriles@eolos.la (Oscar Andres Carriles)
 * @date 3/5/2016 22:48:44
 * @class B2BUABuilderProcessor.java
 *
 */
public class B2BUABuilderProcessor extends DefaultProcessor implements ProcessorCallBack {

	private static transient Logger LOG = Logger.getLogger(B2BUABuilderProcessor.class);

	private RouteManager routeManager;
	private LocationManager locationManager;
	private SipFactory sipFactory;
	private SipApplicationSession aSession;
	

	public B2BUABuilderProcessor(ProcessorChain chain) {
		super(chain);
		this.chain = chain;
		routeManager = RouteManager.getRouteManager();
		locationManager = LocationManager.getLocationManager();
		this.sipFactory = ConfigurationCache.getSipFactory();
		
		
		
	}

	public B2BUABuilderProcessor(String name, ProcessorChain chain) {
		this(chain);
		setName(name);
	}
	/*
	 * <------------UPSTREAM -----------------------
    				|        			|
				+---------+         +---------+
	 <--------- |  UA     |         | UA      | <---------
	            |<--------|         |		  |
	 ---------> + @MZ     |-------->|  @DMZ   + --------->      
	  Message   |         |         |         |  Message
				|         |         |         |
				+---------+         +---------+
			MZ Leg  |      			   | DMZ Leg
			
	   -------------DOWNSTREAM---------------------->
     */

	private void processRequest(SIPMutableMessage message) {
		if (LOG.isTraceEnabled()) {
			LOG.trace(">> processRequest()");
			
		}
		SipServletRequest request=(SipServletRequest) message.getContent();
		
		B2buaHelper helper = request.getB2buaHelper();
		SipServletRequest newRequest = null;
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		
		SipURI fromURI 	= (SipURI) request.getFrom().getURI();
		SipURI toURI 	= (SipURI) request.getTo().  getURI();
		SipURI newSipToUri = toURI;
		SipURI contactURI = null;

		
		RouteManager routeManager = RouteManager.getRouteManager();
		Connector connector = null;
		InetSocketAddress outBoundInterface = null;
		String route;
		
		
		if (message.getDirection()==Message.SOURCE_DMZ) {
			
			// Must create Leg to MZ based on router info
			try {
				connector = routeManager.getRouteToMZ(request.getLocalAddr(), request.getLocalPort(),
						request.getTransport());
				outBoundInterface = connector.getOutboundInterface();
				contactURI = routeManager.getContactAddress(fromURI.getUser(), outBoundInterface);
				contactURI.setTransportParam(connector.getTransport().toString());
				

			} catch (NoRouteToHostException e) {
				LOG.error("ERROR", e);
			}

			if (LOG.isTraceEnabled()) {
				headers.put(MessageUtil.B2BUA_FINGERPRINT_HEADER, Arrays.asList("Made in RequestBuilder to MZ"));
			}
			message.setTargetLocalAddress(connector.getHost());
			message.setTargetRemoteAddress(ConfigurationCache.getTargetHost());
			// newSipUri = sipFactory.createSipURI(""/*toURI.getUser()*/,
			// ConfigurationCache.getTargetHost());
			// newSipUri = sipFactory.createSipURI(toURI.getUser(), ConfigurationCache.getTargetHost());
			route="sip:" + ConfigurationCache.getTargetHost();

		} else {
			
			// Comes from MZ Must create LEG to DMZ based on Location info
			Location location = null;
			
			try {
				location = locationManager.getLocation(toURI.getUser() + "@" + ConfigurationCache.getDomain());
				outBoundInterface = new InetSocketAddress(ConfigurationCache.getDomain(), request.getLocalPort());
				contactURI = routeManager.getContactAddress(fromURI.getUser(), outBoundInterface);
				contactURI.setTransportParam(location.getTransport());
				
				message.setTargetLocalAddress(ConfigurationCache.getDomain());
				message.setTargetRemoteAddress(location.getHost());

			} catch (LocationNotFoundException e) {
				SipServletResponse response =
				request.createResponse(SipServletResponse.SC_DOES_NOT_EXIST_ANYWHERE);
				try {
					response.send();
				} catch (IOException e1) {
					LOG.error("ERROR", e);
				}
				message.abort();
				return;
				
			} catch (NoRouteToHostException e) {
				LOG.error("ERROR", e);
			}
			
			newSipToUri = sipFactory.createSipURI(toURI.getUser(), location.getHost());
			newSipToUri.setPort(location.getPort());
			newSipToUri.setTransportParam(location.getTransport());
			route=null; //"sip:" + location.getHost();
			//headers.put("To",	Arrays.asList(newSipToUri.toString()));
			
			

		}
		
		try {
			
			
			if (request.isInitial()) {
				
				headers.put("Contact",	Arrays.asList(contactURI.toString()));
				
				
				
				if (LOG.isTraceEnabled()) {
					
					if(aSession!=null&&aSession.isValid()){
					LOG.trace("LNK "+aSession.getAttribute(request.getSession().getCallId()));
					}
				}
				
				SipServletRequest usedRequest=null;
				
				if(aSession!=null&&aSession.isValid()) {
					usedRequest=(SipServletRequest) aSession.getAttribute(request.getSession().getCallId());
					
					
				}
				if(usedRequest!=null&&usedRequest.getSession().isValid()) {
					LOG.trace("REUSING SESSION REQ "+usedRequest.getCallId()+":"+usedRequest.getHeader("CSeq"));
					newRequest = helper.createRequest(usedRequest.getSession(), request, headers);
					
					
				}
				else {
					newRequest = helper.createRequest(request, true, headers);
					aSession = sipFactory.createApplicationSession();
					aSession.setAttribute(request.getSession().getCallId(), newRequest);
					
				}
			
				
				newRequest.getSession().setOutboundInterface(outBoundInterface);

				//newRequest.getFrom().setURI(sipFactory.createSipURI(fromURI.getUser(),  ConfigurationCache.getTargetHost()));
				//newRequest.getTo().  setURI(newSipToUri);

				if(route!=null)
					newRequest.pushRoute(sipFactory.createAddress(route));
				else
					newRequest.setRequestURI(newSipToUri);
				
				
			} else {
				if (LOG.isTraceEnabled()) {
					LOG.trace("NOT Initial Request " + request.getMethod());
				}

				if (request.getMethod().equals("BYE")) {
					SipSession session = request.getSession();
					SipSession linkedSession = helper.getLinkedSession(session);
					newRequest = linkedSession.createRequest("BYE");
					
				} 
				else if (request.getMethod().equals("ACK")) {
					SipSession session = request.getSession();
					SipSession linkedSession = helper.getLinkedSession(session);
					newRequest = linkedSession.createRequest("ACK");
					
				} 
				else if (request.getMethod().equals("INFO")) {
					SipSession session = request.getSession();
					SipSession linkedSession = helper.getLinkedSession(session);
					newRequest = linkedSession.createRequest("INFO");
					newRequest.setContent(request.getContent(), request.getContentType());
					
				} 
				
				else if (request.getMethod().equals("CANCEL")) {
					
					SipSession session = request.getSession();
					SipSession linkedSession = helper.getLinkedSession(session);
					SipServletRequest originalRequest = (SipServletRequest) linkedSession
							.getAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR);
					newRequest = helper.getLinkedSipServletRequest(originalRequest).createCancel();
					
					
				} 
				else {
					LOG.error(request.getMethod() + " not implemented!");
				}
				

			}

			
			//newRequest.setAddressHeader("Contact", sipFactory.createAddress(contactURI));
			newRequest.getSession().setAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR, request);
	
			
			if (LOG.isTraceEnabled()) {
				LOG.trace("Initial Request " + request.getMethod()+" on session "+newRequest.getSession().getId());
				LOG.trace("Routing thru outboundInterface " + outBoundInterface.toString());
				LOG.trace("Routing To " + route);
				LOG.trace("Contact back " + contactURI.toString());
				LOG.trace("Sending Message: \n " + newRequest.toString());
			}
			
			
		} catch (IllegalArgumentException e) {
			LOG.error("", e);
		} catch (TooManyHopsException e) {
			LOG.error("", e);
		} catch (ServletParseException e) {
			LOG.error("", e);
		} catch (RuntimeException e) {
			LOG.error("", e);
		} catch (UnsupportedEncodingException e) {
			LOG.error("", e);
		} catch (IOException e) {
			LOG.error("", e);
		} 
		
		message.setContent(newRequest);
		
	}

	private void processResponse(SIPMutableMessage message) {
		if (LOG.isTraceEnabled()) {
			LOG.trace(">> processResponse()");
		}
		SipServletResponse dmzResponse=(SipServletResponse) message.getContent();
		
		SipURI toURI = (SipURI) dmzResponse.getTo().getURI();

		B2buaHelper helper = dmzResponse.getRequest().getB2buaHelper();
		SipServletResponse mzResponse;

		int statusResponse = dmzResponse.getStatus();
		String reasonResponse = dmzResponse.getReasonPhrase();
		
		if (dmzResponse.getStatus() == SipServletResponse.SC_RINGING) {
			
			if (LOG.isTraceEnabled()) {
				LOG.trace("180 Detected->183");
			}
			//statusResponse = SipServletResponse.SC_SESSION_PROGRESS;
			//reasonResponse = "Session Progress";
			

		}
		if (dmzResponse.getStatus() == SipServletResponse.SC_OK) {
			if(dmzResponse.getMethod().equals("REGISTER")) {
			
				if (LOG.isTraceEnabled()) {
					LOG.trace("Final Response Discarding dialog");
				}
				aSession=null;
			}
		}
		
		SipServletRequest linked = helper.getLinkedSipServletRequest(dmzResponse.getRequest()); 
		SipSession originalSession = helper.getLinkedSession(dmzResponse.getSession());
		
		if(linked!=null) {
			message.setTargetLocalAddress(linked.getLocalAddr());
			message.setTargetRemoteAddress(linked.getRemoteAddr());
			
			mzResponse = linked.createResponse(statusResponse, reasonResponse); 
			
			if (LOG.isTraceEnabled()) {
				LOG.trace("Reusing linked session");
			}
		}
		else {
			message.setTargetLocalAddress(dmzResponse.getLocalAddr());
			message.setTargetRemoteAddress(dmzResponse.getRemoteAddr());
			mzResponse = helper.createResponseToOriginalRequest(originalSession, statusResponse, reasonResponse);
		}
		
		
		if (LOG.isTraceEnabled()) {
			LOG.trace("mz Response created for session "+originalSession.getId());
		}

		try {
			if (dmzResponse.getContent() != null) {
				mzResponse.setContent(dmzResponse.getContent(), dmzResponse.getContentType());
			}
			mzResponse.setHeaderForm(dmzResponse.getHeaderForm());
			
		} catch (UnsupportedEncodingException e) {
			LOG.error("ERROR", e);
		} catch (IOException e) {
			LOG.error("ERROR", e);
		} catch (IllegalStateException e) {
			LOG.error("ERROR", e);
		} 

		if (dmzResponse.getStatus() == SipServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED) {
			mzResponse.setHeader("Proxy-Authenticate", dmzResponse.getHeader("Proxy-Authenticate"));
			
		}

		else if (dmzResponse.getStatus() == SipServletResponse.SC_UNAUTHORIZED) {	
			mzResponse.setHeader("WWW-Authenticate", dmzResponse.getHeader("WWW-Authenticate"));	
		}
		
		
		mzResponse.getSession().setAttribute(MessageUtil.B2BUA_ORIG_REQUEST_ATTR, dmzResponse.getRequest());
		//mzResponse.setHeader("Contact", "sip:"+dmzResponse.getRequest().getLocalAddr()+":"+dmzResponse.getRequest().getLocalPort());
		message.setContent(mzResponse);
		

	}
	
	

	public String getName() {
		return "B2BUA leg builder Processor";
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
	public void doProcess(Message message) throws ProcessorParsingException {
		SIPMutableMessage m = (SIPMutableMessage) message;
		SipServletMessage sm=(SipServletMessage) message.getContent();
		
		
		if (LOG.isTraceEnabled()) {
			LOG.trace("-------" + sm.getLocalAddr() + "->" + sm.getRemoteAddr());
			LOG.trace("-------Receiving message: \n" + sm);
		}
		if(sm instanceof SipServletRequest) {
			processRequest(m);
		}
		if(sm instanceof SipServletResponse) {
			processResponse(m);
		}
		
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

}
