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

import javax.servlet.sip.Address;
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




/**
 * @author ocarriles@eolos.la (Oscar Andres Carriles)
 * @date 13 sept. 2016 18:10:42
 * @class NATHelperProcessor.java
 *
 */
public class NATHelperProcessor extends DefaultProcessor implements ProcessorCallBack {

	private static transient Logger LOG = Logger.getLogger(NATHelperProcessor.class);

	private Address contactAddress=null;
	
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
		if (LOG.isTraceEnabled()) {
			LOG.trace(">> processResponse()");
		}
	}

	private void processRequest(SIPMutableMessage message) {
		
		SipServletRequest dmzRequest=(SipServletRequest) message.getContent();
		
		if (LOG.isTraceEnabled()) {
			LOG.trace(">> processRequest()");
			LOG.trace(">> message: \n" + dmzRequest.toString());
		}
		if(!dmzRequest.isInitial()) {
			if(LOG.isTraceEnabled()){ 
				LOG.trace("No initial request, NAT does not apply.");
			} // Nothing
			message.setContent(dmzRequest);
			return;
		}

		if (message.getDirection()==Message.SOURCE_MZ) {
			if(LOG.isTraceEnabled()){ 
				LOG.trace("-----> MZ");
			} // Nothing
			message.setContent(dmzRequest);
			return;
		}
		
		try {
			contactAddress = dmzRequest.getAddressHeader("Contact");
			if(LOG.isTraceEnabled()){ 
				LOG.trace("Contact "+contactAddress.toString());
			} 
		} catch (ServletParseException e) {
			LOG.error("Cannot get Contact Address!");
		} catch (IllegalStateException e) {
			LOG.error("",e);
			message.setContent(dmzRequest);
			return;
			
		}
		SipURI uri = (SipURI) contactAddress.getURI();

		
		if(uri.getHost().equals(dmzRequest.getRemoteAddr())) {
			if(LOG.isTraceEnabled()){ 
				LOG.trace(">> ByPassing ...");
			} // Nothing
			message.setContent(dmzRequest);
			return;
		 
		}
	
		/*
		 * Replace Contact address from IP/Port the message is coming from
		 */

		try {
			uri.setHost(message.getSourceLocalAddress());
			uri.setPort(dmzRequest.getLocalPort());
			
		} catch (IllegalStateException e) {
			LOG.error("",e);
			message.setContent(dmzRequest);
			return;
			
		}
		dmzRequest.setAddressHeader("Contact", ConfigurationCache.getSipFactory().createAddress(uri));
		message.setContent(dmzRequest);
		return;

	}

	@Override
	public ProcessorCallBack getCallback() {
		return this;
	}


	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public void doProcess(Message message) throws ProcessorParsingException {
		SIPMutableMessage m = (SIPMutableMessage) message;
		SipServletMessage sm=m.getContent();
		
		
		if( sm instanceof SipServletRequest) {
			processRequest(m);
		}
		if (message instanceof SipServletResponse) {
			processResponse(m);
		}
		
	}

}