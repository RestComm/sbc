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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.sip.Address;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.ServletParseException;
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



/**
 * @author  ocarriles@eolos.la (Oscar Andres Carriles)
 * @date    3/5/2016 22:48:44
 * @class   B2BUABuilderProcessor.java
 *
 */
public class B2BUABuilderProcessor extends DefaultProcessor implements ProcessorCallBack {
	
	private static transient Logger LOG = Logger.getLogger(B2BUABuilderProcessor.class);
	private ProcessorChain chain;
	
	public B2BUABuilderProcessor(ProcessorChain chain) {
		super(chain);
		this.chain=chain;	
	}
	
	public B2BUABuilderProcessor(String name, ProcessorChain chain) {
		this(chain);
		setName(name);
	}

	
	/**
	 * 
	 *                                   
	 *                                   
	 *                                          |-----TO Militarized Zone-->
	 *                         SBC              |
	 *                         +----------------+-----------------+                  +---------+
	 *  +----+                 | +------+   +---+---+   +---+---+ |                  |         |
	 *  |    +<--DMZ Request-->+ |      +<==+===|   |   |   |   | +<---MZ Request--->+ PROXY   |
	 *  | UA |                 | |  PU  |   | B2BUA |   |TA |PU | |                  | SERVER  |
	 *  |    +<--DMZ Response->+ |      |   |   |   |   |   |   | +<---MZ Response-->+         |
	 *  +----+                 | +------+   +---+---+   +---+---+ |                  |         |
	 *                         +----------------+-----------------+                  +---------+
	 *                                          |
	 *             <--To Demilitarized Zone-----|
	 *             
	 *  UA     User Agent
	 *  PU     Processor Unit
	 *  TA     Transport Adapter
	 *  B2BUA  Back to Back User Agent
	 *  MZ     Miltarized Zone (LAN)
	 *  DMZ    Demilitarized Zone (WAN)
	 *  SBC    Session Border Controller
	 *  
	 * @param dmzRequest
	 * @return
	 */
	
	private SipServletRequest processRequest(SipServletRequest dmzRequest) {
		
		String user = ((SipURI) dmzRequest.getFrom().getURI()).getUser();
		
		//dmzRequest.setHeader(MessageUtil.B2BUA_FINGERPRINT_HEADER, "Made in RequestBuilder to DMZ");
		
		Map<String, List<String>> headers=new HashMap<String, List<String>>();

		B2buaHelper helper = dmzRequest.getB2buaHelper();
		SipServletRequest tomzRequest = null;
		try {
			tomzRequest = helper.createRequest(dmzRequest, true, headers);
			
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage());
		} catch (TooManyHopsException e) {
			LOG.error(e.getMessage());
		} 
		
		/*
		tomzRequest.setHeader(MessageUtil.B2BUA_FINGERPRINT_HEADER, "Made in RequestBuilder to MZ");
		
		
		SipURI sipUri = ConfigurationCache.getSipFactory().createSipURI(user, ConfigurationCache.getRouteMZIPAddress());
		sipUri.setTransportParam(ConfigurationCache.getRouteMZTransport());
		
		sipUri.setPort(ConfigurationCache.getRouteMZPort());
		
		tomzRequest.setRequestURI(sipUri);
		*/
		
		return tomzRequest;
		
		
	}
	
	private SipServletResponse processResponse(SipServletResponse dmzResponse) {
			
			B2buaHelper helper = dmzResponse.getRequest().getB2buaHelper();
			//create and sends OK for the first call leg
			SipSession originalSession =   
			    helper.getLinkedSession(dmzResponse.getSession());					
			SipServletResponse mzResponse = 
				helper.createResponseToOriginalRequest(originalSession, dmzResponse.getStatus(), dmzResponse.getReasonPhrase());
			try {
				if(dmzResponse.getContent() != null) {
					mzResponse.setContent(dmzResponse.getContent(), dmzResponse.getContentType());
				}
				mzResponse.setHeaderForm(dmzResponse.getHeaderForm());
			
				//Address address = originalSession.getLocalParty();
				Address address = mzResponse.getRequest().getAddressHeader("Contact");
				mzResponse.setHeader("Contact", address.toString());
				//mzResponse.setHeader(MessageUtil.B2BUA_FINGERPRINT_HEADER, "Made in ResponseBuilder to DMZ");	
				
			} catch (UnsupportedEncodingException e) {
				LOG.error("ERROR",e);
			} catch (IOException e) {
				LOG.error("ERROR",e);
			} catch (ServletParseException e) {
				LOG.error("ERROR",e);
			}
			
			if (dmzResponse.getStatus() == SipServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED) {
				mzResponse.setHeader("Proxy-Authenticate", dmzResponse.getHeader("Proxy-Authenticate"));
			}
			else if (dmzResponse.getStatus() == SipServletResponse.SC_UNAUTHORIZED) {
				mzResponse.setHeader("WWW-Authenticate", dmzResponse.getHeader("WWW-Authenticate"));
			}
			
			return mzResponse;
		
	}

	
	public String getName() {
		return "B2BUA leg builder Processor";
	}

	public int getId() {
		return this.hashCode();
	}

 
	public SipServletMessage doProcess(SipServletMessage message) throws ProcessorParsingException {
		
		if(LOG.isTraceEnabled()){
	          LOG.trace(">> process()");
	    }
		
		
		if(message instanceof SipServletRequest) {
			message=processRequest((SipServletRequest) message);
		}
		if(message instanceof SipServletResponse) {
			message=processResponse((SipServletResponse) message);
		}
		
		return message;
	}



	@Override
	public void setName(String name) {
		this.name=name;
		
	}


	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}


	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		SIPMutableMessage m=(SIPMutableMessage) message;
		m.setContent(doProcess(m.getContent()));
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}


}
